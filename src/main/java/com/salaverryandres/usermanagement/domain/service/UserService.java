package com.salaverryandres.usermanagement.domain.service;

import com.salaverryandres.usermanagement.application.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.application.dto.UserDto;
import com.salaverryandres.usermanagement.application.dto.UserPageResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDto createUser(UserCreateRequestDto request);

    UserPageResponse getAllUsers(Pageable pageable);

    UserDto getUserByExternalId(String externalId);

    UserDto updateUser(String externalId, UserCreateRequestDto request);

    void deleteUser(String externalId);
}

