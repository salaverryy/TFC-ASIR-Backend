package com.salaverryandres.usermanagement.mapper;

import com.salaverryandres.usermanagement.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.dto.UserDto;
import com.salaverryandres.usermanagement.entity.UserEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(UserEntity entity);

    UserEntity toEntity(UserCreateRequestDto dto);

    List<UserDto> toDtoList(List<UserEntity> entities);
}

