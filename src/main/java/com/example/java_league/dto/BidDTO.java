package com.example.java_league.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

public record BidDTO(
        Long id,
        Long value,
        ZonedDateTime date,
        Long teamId,
        Long playerId
) implements Serializable {}
