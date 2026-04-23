package com.example.java_league.service;

import com.example.java_league.domain.Bid;
import com.example.java_league.dto.BidDTO;
import com.example.java_league.mapper.BidMapper;
import com.example.java_league.repository.BidRepository;
import com.example.java_league.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final PlayerRepository playerRepository;
    private final BidMapper bidMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public BidDTO save(BidDTO bidDTO) {
        Bid bid = bidMapper.toEntity(bidDTO);
        bid.setDate(ZonedDateTime.now());
        bid = bidRepository.save(bid);
        log.info("Lance salvo - playerId={}, teamId={}, value={}", bid.getPlayerId(), bid.getTeamId(), bid.getValue());
        simpMessagingTemplate.convertAndSend("/topic/bids", bid);
        return bidMapper.toDto(bid);
    }
}
