package com.example.java_league.controllers;

import com.example.java_league.dto.BidResponseDTO;
import com.example.java_league.dto.PlayerDTO;
import com.example.java_league.security.TokenService;
import com.example.java_league.service.PlayerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final SimpMessagingTemplate template;
    private final TokenService tokenService;

    @PostMapping("/player")
    public ResponseEntity postPlayer(@RequestBody @Valid PlayerDTO body) {
        PlayerDTO playerDTO = playerService.save(body);
        return ResponseEntity.ok(playerDTO);
    }

    @GetMapping("/player")
    public ResponseEntity getAllPlayersWithMaxBid() {
        Long teamId = tokenService.getCurrentTeamId().orElse(null);
        List<PlayerDTO> playerDTOS = playerService.getAllPlayersWithMaxBid(teamId);
        return ResponseEntity.ok(playerDTOS);
    }

    @PostMapping("/player/{id}/bid")
    public ResponseEntity<Void> updateValue(@PathVariable("id") Long id, @RequestParam("bidValue") @Positive Long bidValue) {
        Long teamId = tokenService.getCurrentTeamId().orElse(null);
        BidResponseDTO bidRespondeDTO = playerService.bid(bidValue, teamId, id);
        template.convertAndSend("/topic/bid", bidRespondeDTO);
        return ResponseEntity.ok().build();
    }
}
