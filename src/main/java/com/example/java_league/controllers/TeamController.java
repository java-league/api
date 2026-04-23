package com.example.java_league.controllers;

import com.example.java_league.dto.TeamDTO;
import com.example.java_league.security.TokenService;
import com.example.java_league.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final TokenService tokenService;

    @GetMapping("team/current")
    public ResponseEntity<TeamDTO> getCurrentTeam() {
        Long teamId = tokenService.getCurrentTeamId().orElse(null);
        TeamDTO teamDTO = teamService.getCurrentTeam(teamId);
        return ResponseEntity.ok(teamDTO);
    }

    @PostMapping("team/current")
    public ResponseEntity<TeamDTO> saveCurrentTeam(@RequestParam("teamId") Long teamId) {
        Long userId = tokenService.getCurrentUserId().orElse(null);
        TeamDTO teamDTO = teamService.saveCurrentTeam(teamId, userId);
        return ResponseEntity.ok(teamDTO);
    }

    @GetMapping("team")
    public ResponseEntity<List<TeamDTO>> getAllTeam() {
        List<TeamDTO> teamDTO = teamService.getAllTeams();
        return ResponseEntity.ok(teamDTO);
    }

    @GetMapping("team/available")
    public ResponseEntity<List<TeamDTO>> getAllTeamsAvailable() {
        List<TeamDTO> teamDTO = teamService.getAllTeamsAvailable();
        return ResponseEntity.ok(teamDTO);
    }

    @PostMapping("/team/{playerId}/player")
    public ResponseEntity<Void> updateValue(@PathVariable("playerId") Long playerId, @RequestParam("position") Long position) {
        Long teamId = tokenService.getCurrentTeamId().orElse(null);
        teamService.saveTeamPlayer(teamId, playerId, position);
        return ResponseEntity.ok().build();
    }
}
