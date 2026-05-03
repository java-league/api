package com.example.java_league.service;

import com.example.java_league.domain.Bid;
import com.example.java_league.domain.BidOutcome;
import com.example.java_league.domain.Player;
import com.example.java_league.domain.Team;
import com.example.java_league.dto.BidResponseDTO;
import com.example.java_league.dto.PlayerDTO;
import com.example.java_league.event.BidProcessedEvent;
import com.example.java_league.mapper.PlayerMapper;
import com.example.java_league.repository.BidRepository;
import com.example.java_league.repository.PlayerRepository;
import com.example.java_league.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final BidRepository bidRepository;
    private final PlayerMapper playerMapper;
    private final ApplicationEventPublisher eventPublisher;

    public PlayerDTO save(PlayerDTO playerDTO) {
        Player player = playerMapper.toEntity(playerDTO);
        player = playerRepository.save(player);
        return playerMapper.toDto(player);
    }

    public List<PlayerDTO> getAllPlayersWithMaxBid(Long teamId) {
        List<Player> players = playerRepository.findAllByOrderByIdAsc();
        Long effectiveTeamId = teamId != null ? teamId : -1L;

        Map<Long, Object[]> statsMap = bidRepository.findBidStatsGroupedByPlayer(effectiveTeamId)
                .stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> row));

        return players.stream()
                .map(player -> {
                    Object[] stats = statsMap.get(player.getId());
                    Long maxBidValue = stats != null ? (Long) stats[1] : null;
                    boolean hasBidForTeam = stats != null && ((Number) stats[2]).longValue() > 0;
                    return new PlayerDTO(
                            player.getId(), player.getName(), player.getOverall(), player.getPrice(),
                            player.getImageUrl(), player.getTeam() != null ? player.getTeam().getId() : null,
                            maxBidValue, hasBidForTeam
                    );
                })
                .toList();
    }

    public PlayerDTO getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Player not found: " + id));
        return playerMapper.toDto(player);
    }

    @Transactional
    public BidResponseDTO bid(Long bidValue, Long teamId, Long playerId) {
        if (teamId == null) {
            throw new IllegalArgumentException("Team ID is required to place a bid");
        }
        log.info("Bid recebido - playerId={}, teamId={}, bidValue={}", playerId, teamId, bidValue);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        Bid highestBid = bidRepository.findFirstByPlayerIdOrderByValueDesc(playerId);

        BidOutcome outcome;

        if (highestBid == null) {
            team.debitJavalis(player.getPrice());
            bidRepository.save(new Bid(bidValue, teamId, playerId));
            player.setPrice(player.getPrice() + 100);
            player.setTeam(team);
            outcome = new BidOutcome.FirstBid(team.getId(), player.getId(), player.getPrice(), bidValue);

        } else if (bidValue > highestBid.getValue()) {
            Team teamLowestBid = player.getTeam();
            teamLowestBid.creditJavalis(player.getPrice() - 100);
            teamRepository.save(teamLowestBid);
            team.debitJavalis(highestBid.getValue() + 100);
            player.setPrice(highestBid.getValue() + 200);
            bidRepository.save(new Bid(bidValue, teamId, playerId));
            player.setTeam(team);
            outcome = new BidOutcome.HighestBid(team.getId(), teamLowestBid.getId(), player.getId(), player.getPrice(), bidValue);

        } else {
            Team teamHighestBid = player.getTeam();
            teamHighestBid.debitJavalis(bidValue - (player.getPrice() - 100));
            teamRepository.save(teamHighestBid);
            player.setPrice(bidValue + 100);
            outcome = new BidOutcome.LowestBid(teamHighestBid.getId(), team.getId(), player.getId(), player.getPrice());
        }

        playerRepository.save(player);

        ZonedDateTime now = ZonedDateTime.now();
        BidResponseDTO response = switch (outcome) {
            case BidOutcome.FirstBid fb ->
                    BidResponseDTO.firstBid(fb.newPrice(), fb.priceLimit(), fb.playerId(), now, fb.teamId());
            case BidOutcome.HighestBid hb ->
                    BidResponseDTO.highestBid(hb.newPrice(), hb.priceLimit(), hb.playerId(), now, hb.teamIdLoser(), hb.teamIdWinner());
            case BidOutcome.LowestBid lb ->
                    BidResponseDTO.lowestBid(lb.newPrice(), lb.playerId(), now, lb.teamIdBidder(), lb.teamIdOwner());
        };

        log.info("Lance processado: {} - playerId={}, teamId={}, novoPreco={}", response.message(), playerId, teamId, player.getPrice());
        eventPublisher.publishEvent(new BidProcessedEvent(this, response));
        return response;
    }
}
