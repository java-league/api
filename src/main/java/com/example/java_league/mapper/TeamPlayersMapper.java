package com.example.java_league.mapper;

import com.example.java_league.domain.TeamPlayers;
import com.example.java_league.dto.TeamPlayersDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PlayerMapper.class, TeamMapper.class})
public interface TeamPlayersMapper extends EntityMapper<TeamPlayersDTO, TeamPlayers> {

    default List<TeamPlayersDTO> toDto(List<TeamPlayers> empresas) {
        return empresas.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Mapping(source = "player.id", target = "playerId")
    @Mapping(source = "player.name", target = "name")
    @Mapping(source = "player.imageUrl", target = "imageUrl")
    TeamPlayersDTO toDto(TeamPlayers empresa);

    @Mapping(source = "playerId", target = "player")
    @Mapping(source = "teamId", target = "team")
    TeamPlayers toEntity(TeamPlayersDTO empresaPlanoDTO);
}