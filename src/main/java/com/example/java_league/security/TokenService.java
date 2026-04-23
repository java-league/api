package com.example.java_league.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.java_league.security.jwt.JWTUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(JWTUser user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("java_league-api")
                    .withSubject(user.getUsername())
                    .withClaim("teamId", user.getTeamId())
                    .withClaim("userId", user.getId())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("java_league-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return "";
        }
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now(ZoneOffset.UTC).plusHours(2).toInstant(ZoneOffset.UTC);
    }

    public Optional<Long> getCurrentTeamId() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractTeamId(securityContext.getAuthentication()));
    }

    public Long extractTeamId(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof JWTUser) {
            JWTUser springSecurityUser = (JWTUser) authentication.getPrincipal();
            if (springSecurityUser.getTeamId() == 0L) {
                return null;
            }
            return springSecurityUser.getTeamId();
        }
        return null;
    }

    public Optional<Long> getCurrentUserId() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractUserId(securityContext.getAuthentication()));
    }

    public Long extractUserId(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof JWTUser) {
            JWTUser springSecurityUser = (JWTUser) authentication.getPrincipal();
            if (springSecurityUser.getId() == 0L) {
                return null;
            }
            return springSecurityUser.getId();
        }
        return null;
    }
}
