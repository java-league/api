package com.example.java_league.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TeamPlayersDTO implements Serializable {
    private Long playerId;
    private Long teamId;
    private Long position;
    private String name;
    private String imageUrl;

    public TeamPlayersDTO(Long playerId, Long teamId, Long position) {
        this.playerId = playerId;
        this.teamId = teamId;
        this.position = position;
    }
}
