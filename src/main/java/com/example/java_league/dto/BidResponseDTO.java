package com.example.java_league.dto;

import com.example.java_league.enums.BidType;

import java.time.ZonedDateTime;

public record BidResponseDTO(
        Long newPrice,
        Long priceLimit,
        Long playerId,
        ZonedDateTime date,
        BidType message,
        Long teamIdLowest,
        Long teamIdHighest
) {
    public static BidResponseDTO firstBid(Long newPrice, Long priceLimit, Long playerId, ZonedDateTime date, Long teamIdHighest) {
        return new BidResponseDTO(newPrice, priceLimit, playerId, date, BidType.FIRST_BID, null, teamIdHighest);
    }

    public static BidResponseDTO highestBid(Long newPrice, Long priceLimit, Long playerId, ZonedDateTime date, Long teamIdLowest, Long teamIdHighest) {
        return new BidResponseDTO(newPrice, priceLimit, playerId, date, BidType.HIGHEST_BID, teamIdLowest, teamIdHighest);
    }

    public static BidResponseDTO lowestBid(Long newPrice, Long playerId, ZonedDateTime date, Long teamIdLowest, Long teamIdHighest) {
        return new BidResponseDTO(newPrice, null, playerId, date, BidType.LOWEST_BID, teamIdLowest, teamIdHighest);
    }
}
