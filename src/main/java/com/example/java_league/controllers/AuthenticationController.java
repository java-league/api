package com.example.java_league.controllers;

import com.example.java_league.domain.Team;
import com.example.java_league.domain.User;
import com.example.java_league.record.AuthenticationRecord;
import com.example.java_league.record.LoginResponseRecord;
import com.example.java_league.record.RegisterRecord;
import com.example.java_league.repository.TeamRepository;
import com.example.java_league.repository.UserRepository;
import com.example.java_league.security.TokenService;
import com.example.java_league.security.jwt.JWTUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationRecord data) {


        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByLogin(data.login());
        Team team = teamRepository.findFirstByUserId(user.getId());
        JWTUser principal = new JWTUser(user.getId(), user.getUsername(), user.getPassword(), team != null ? team.getId() : 0);
        String token = tokenService.generateToken(principal);

        return ResponseEntity.ok(new LoginResponseRecord(token, team != null ? team.getId() : 0));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterRecord data) throws URISyntaxException {
        if (this.userRepository.findByLogin(data.login()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(data.login(), encryptedPassword, data.role());

        this.userRepository.save(newUser);

        return ResponseEntity.created(new URI("/api/users/" + newUser.getLogin())).body(newUser);
    }
}
