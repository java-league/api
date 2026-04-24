package com.example.java_league.mapper;

import com.example.java_league.domain.Team;
import com.example.java_league.dto.TeamDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TeamMapper extends EntityMapper<TeamDTO, Team> {

    TeamMapper INSTANCE = Mappers.getMapper(TeamMapper.class);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "teamPlayers", ignore = true)
    TeamDTO toDto(Team team);

    @Mapping(source = "userId", target = "user")
    Team toEntity(TeamDTO teamDTO);

    default Team map(Long teamId) {
        if (teamId == null) {
            return null;
        }
        Team team = new Team();
        team.setId(teamId);
        return team;
    }
}
