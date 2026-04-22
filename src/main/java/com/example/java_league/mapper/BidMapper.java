package com.example.java_league.mapper;

import com.example.java_league.domain.Bid;
import com.example.java_league.domain.Player;
import com.example.java_league.dto.BidDTO;
import com.example.java_league.dto.PlayerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BidMapper extends EntityMapper<PlayerDTO, Player> {

    BidMapper INSTANCE = Mappers.getMapper(BidMapper.class);

    BidDTO toDto(Bid bid);

    Bid toEntity(BidDTO bidDTO);
}