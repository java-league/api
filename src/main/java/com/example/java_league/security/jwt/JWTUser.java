package com.example.java_league.security.jwt;

import com.example.java_league.domain.User;
import lombok.Getter;

@Getter
public class JWTUser extends User {

    private Long teamId;

    public JWTUser(Long id, String username, String password, Long teamId) {
        this.setId(id);
        this.setLogin(username);
        this.setPassword(password);
        this.teamId = teamId;
    }
}
