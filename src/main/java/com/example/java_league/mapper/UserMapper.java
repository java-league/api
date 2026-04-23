package com.example.java_league.mapper;

import com.example.java_league.domain.User;
import com.example.java_league.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper extends EntityMapper<UserDTO, User> {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "token", ignore = true)
    @Mapping(target = "bids", ignore = true)
    UserDTO toDto(User user);

    @Mapping(target = "bids", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserDTO userDTO);

    default User map(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }
}
