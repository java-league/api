package com.example.java_league.dto;

import java.util.List;

public record TeamDTO(
        Long id,
        Long javalis,
        Long userId,
        String name,
        String uniform1,
        String uniform2,
        String emblem,
        String formation,
        List<TeamPlayersDTO> teamPlayers
) {}
