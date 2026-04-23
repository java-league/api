package com.example.java_league.mapper;

import com.example.java_league.domain.Player;
import com.example.java_league.dto.PlayerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {TeamMapper.class})
public interface PlayerMapper extends EntityMapper<PlayerDTO, Player> {

    PlayerMapper INSTANCE = Mappers.getMapper(PlayerMapper.class);

    @Mapping(source = "team.id", target = "teamId")
    PlayerDTO toDto(Player player);

    @Mapping(target = "team", ignore = true)
    Player toEntity(PlayerDTO playerDTO);

    default Player map(Long playerId) {
        if (playerId == null) {
            return null;
        }

        Player player = new Player();
        player.setId(playerId);
        return player;
    }
}