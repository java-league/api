package com.example.java_league.dto;

import java.io.Serializable;

public record TeamPlayersDTO(
        Long playerId,
        Long teamId,
        Long position,
        String name,
        String imageUrl
) implements Serializable {

    public static TeamPlayersDTO of(Long playerId, Long teamId, Long position) {
        return new TeamPlayersDTO(playerId, teamId, position, null, null);
    }
}
