package com.example.java_league.dto;

public record PlayerDTO(
        Long id,
        String name,
        Long overall,
        Long price,
        String imageUrl,
        Long teamId,
        Long priceLimit,
        Boolean hasBidForTeam
) {}
