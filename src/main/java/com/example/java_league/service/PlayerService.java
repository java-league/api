package com.example.java_league.service;

import com.example.java_league.domain.Bid;
import com.example.java_league.domain.Player;
import com.example.java_league.domain.Team;
import com.example.java_league.dto.BidResponseDTO;
import com.example.java_league.dto.PlayerDTO;
import com.example.java_league.mapper.PlayerMapper;
import com.example.java_league.repository.BidRepository;
import com.example.java_league.repository.PlayerRepository;
import com.example.java_league.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final BidRepository bidRepository;
    private final PlayerMapper playerMapper;

    public PlayerDTO save(PlayerDTO playerDTO) {
        Player player = playerMapper.toEntity(playerDTO);
        player = playerRepository.save(player);
        return playerMapper.toDto(player);
    }

    public List<PlayerDTO> getAllPlayersWithMaxBid(Long teamId) {
        List<Player> player = playerRepository.findAllByOrderByIdAsc();
        return player.stream()
                .map(player1 -> {
                    Long maxBidValue = bidRepository.findMaxBidValueForPlayer(player1.getId());
                    boolean hasBidForTeam = bidRepository.existsBidForPlayerAndTeam(player1.getId(), teamId);

                    PlayerDTO playerDTO = playerMapper.toDto(player1);
                    playerDTO.setPriceLimit(maxBidValue);
                    playerDTO.setHasBidForTeam(hasBidForTeam);
                    return playerDTO;
                }).collect(Collectors.toList());
    }

    public PlayerDTO getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Player not found: " + id));
        return playerMapper.toDto(player);
    }

    public BidResponseDTO bid(Long bidValue, Long teamId, Long playerId) {
        log.info("Bid recebido - playerId={}, teamId={}, bidValue={}", playerId, teamId, bidValue);
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new EntityNotFoundException("Player not found"));
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("Team not found"));
        Bid highestBid = bidRepository.findFirstByPlayerIdOrderByValueDesc(playerId);
        BidResponseDTO bidResponseDTO = new BidResponseDTO();

        if (highestBid == null) {
            team.debitJavalis(player.getPrice());
            Bid bid = new Bid(bidValue, teamId, playerId);
            bidRepository.save(bid);
            player.setPrice(player.getPrice() + 100);
            player.setTeam(team);
            bidResponseDTO.setPriceLimit(bidValue);
            bidResponseDTO.setTeamIdHighest(team.getId());
            bidResponseDTO.setMessage("FIRST_BID");
        } else if (bidValue > highestBid.getValue()) {
            Team teamLowestBid = player.getTeam();
            teamLowestBid.creditJavalis(player.getPrice() - 100);
            bidResponseDTO.setTeamIdLowest(teamLowestBid.getId());
            teamRepository.save(teamLowestBid);
            team.debitJavalis(highestBid.getValue() + 100);
            player.setPrice(highestBid.getValue() + 200);
            Bid bid = new Bid(bidValue, teamId, playerId);
            bidRepository.save(bid);
            player.setTeam(team);
            bidResponseDTO.setPriceLimit(bidValue);
            bidResponseDTO.setTeamIdHighest(team.getId());
            bidResponseDTO.setMessage("HIGHEST_BID");
        } else {
            Team teamHighestBid = player.getTeam();
            teamHighestBid.debitJavalis(bidValue - (player.getPrice() - 100));
            bidResponseDTO.setTeamIdHighest(teamHighestBid.getId());
            teamRepository.save(teamHighestBid);
            player.setPrice(bidValue + 100);
            bidResponseDTO.setTeamIdLowest(team.getId());
            bidResponseDTO.setMessage("LOWEST_BID");
        }

        ZonedDateTime now = ZonedDateTime.now();

        bidResponseDTO.setPlayerId(player.getId());
        bidResponseDTO.setNewPrice(player.getPrice());
        bidResponseDTO.setDate(now);
        playerRepository.save(player);
        log.info("Lance processado: {} - playerId={}, teamId={}, novoPreco={}", bidResponseDTO.getMessage(), playerId, teamId, player.getPrice());
        return bidResponseDTO;
    }

}
