package com.example.java_league.domain;

public sealed interface BidOutcome permits BidOutcome.FirstBid, BidOutcome.HighestBid, BidOutcome.LowestBid {

    record FirstBid(Long teamId, Long playerId, Long newPrice, Long priceLimit) implements BidOutcome {}

    record HighestBid(Long teamIdWinner, Long teamIdLoser, Long playerId, Long newPrice, Long priceLimit) implements BidOutcome {}

    record LowestBid(Long teamIdOwner, Long teamIdBidder, Long playerId, Long newPrice) implements BidOutcome {}
}
