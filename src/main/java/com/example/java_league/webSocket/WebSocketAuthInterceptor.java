package com.example.java_league.webSocket;

import com.example.java_league.domain.Team;
import com.example.java_league.domain.User;
import com.example.java_league.repository.TeamRepository;
import com.example.java_league.repository.UserRepository;
import com.example.java_league.security.TokenService;
import com.example.java_league.security.jwt.JWTUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return message;
        }

        String token = authHeader.substring(7);
        String login = tokenService.validateToken(token);
        if (login.isEmpty()) {
            return message;
        }

        User user = userRepository.findByLogin(login);
        if (user == null) {
            return message;
        }

        Team team = teamRepository.findFirstByUserId(user.getId());
        JWTUser jwtUser = new JWTUser(user.getId(), user.getUsername(), "", team != null ? team.getId() : 0);
        var authentication = new UsernamePasswordAuthenticationToken(jwtUser, null, user.getAuthorities());
        accessor.setUser(authentication);

        return message;
    }
}
