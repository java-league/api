package com.example.java_league.security;

import com.example.java_league.domain.Team;
import com.example.java_league.domain.User;
import com.example.java_league.repository.TeamRepository;
import com.example.java_league.repository.UserRepository;
import com.example.java_league.security.jwt.JWTUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        if (token != null) {
            var login = tokenService.validateToken(token);
            if (!login.isBlank()) {
                User user = userRepository.findByLogin(login);
                if (user != null) {
                    Team team = teamRepository.findFirstByUserId(user.getId());
                    JWTUser jwtUser = new JWTUser(user.getId(), user.getUsername(), "", team != null ? team.getId() : 0);
                    var authentication = new UsernamePasswordAuthenticationToken(jwtUser, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}
