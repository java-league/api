package com.example.java_league.dto;

import com.example.java_league.enums.UserRole;

import java.util.List;

public record UserDTO(
        Long id,
        String login,
        UserRole role,
        String token,
        List<BidDTO> bids
) {}
