package com.example.java_league.controllers;

import com.example.java_league.dto.BidDTO;
import com.example.java_league.dto.BidResponseDTO;
import com.example.java_league.security.TokenService;
import com.example.java_league.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class BidController {

    private final PlayerService playerService;
    private final TokenService tokenService;

    @PostMapping("/bid")
    public ResponseEntity<BidResponseDTO> postBid(@RequestBody @Valid BidDTO body) {
        Long teamId = tokenService.getCurrentTeamId().orElse(null);
        BidResponseDTO response = playerService.bid(body.value(), teamId, body.playerId());
        return ResponseEntity.ok(response);
    }

    @MessageMapping("/bid")
    public void receiveMessage(@Payload BidDTO bidDTO) {
        Long teamId = tokenService.getCurrentTeamId().orElse(null);
        playerService.bid(bidDTO.value(), teamId, bidDTO.playerId());
    }
}
