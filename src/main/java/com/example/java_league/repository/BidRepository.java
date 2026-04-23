package com.example.java_league.repository;

import com.example.java_league.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    @Query("SELECT b.playerId, MAX(b.value), SUM(CASE WHEN b.teamId = :teamId THEN 1 ELSE 0 END) FROM Bid b GROUP BY b.playerId")
    List<Object[]> findBidStatsGroupedByPlayer(@Param("teamId") Long teamId);

    Bid findFirstByPlayerIdOrderByValueDesc(Long playerId);
}
