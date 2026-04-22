package com.example.java_league.service;

import com.example.java_league.domain.Team;
import com.example.java_league.domain.TeamPlayers;
import com.example.java_league.dto.TeamDTO;
import com.example.java_league.dto.TeamPlayersDTO;
import com.example.java_league.mapper.TeamMapper;
import com.example.java_league.mapper.TeamPlayersMapper;
import com.example.java_league.repository.TeamPlayersRepository;
import com.example.java_league.repository.TeamRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamPlayersRepository teamPlayersRepository;
    private final TeamMapper teamMapper;
    private final TeamPlayersMapper teamPlayersMapper;


    public List<TeamDTO> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream().map(teamMapper::toDto).collect(Collectors.toList());
    }

    public List<TeamDTO> getAllTeamsAvailable() {
        List<Team> teams = teamRepository.findAllByUserIdIsNull();
        return teams.stream().map(teamMapper::toDto).collect(Collectors.toList());
    }

    public TeamDTO getCurrentTeam(Long teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) return null;

        TeamDTO teamDTO = teamMapper.toDto(team);
        List<TeamPlayers> teamPlayers = teamPlayersRepository.findAllByTeamId(team.getId());
        teamDTO.setTeamPlayers(teamPlayersMapper.toDto(teamPlayers));
        return teamDTO;
    }

    public TeamDTO saveCurrentTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) return null;

        TeamDTO teamDTO = teamMapper.toDto(team);
        teamDTO.setUserId(userId);
        teamRepository.save(teamMapper.toEntity(teamDTO));
        return teamDTO;
    }

    public void saveTeamPlayer(Long teamId, Long playerId, Long position) {
        TeamPlayers teamPlayers = teamPlayersMapper.toEntity(new TeamPlayersDTO(playerId, teamId, position));
        teamPlayersRepository.save(teamPlayers);
    }
}
