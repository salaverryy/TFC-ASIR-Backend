package com.salaverryandres.usermanagement.domain.service;

import com.salaverryandres.usermanagement.application.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.application.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(UserCreateRequestDto request);

    List<UserDto> getAllUsers();

    UserDto getUserByExternalId(String externalId);

    UserDto updateUser(String externalId, UserCreateRequestDto request);

    void deleteUser(String externalId);
}

