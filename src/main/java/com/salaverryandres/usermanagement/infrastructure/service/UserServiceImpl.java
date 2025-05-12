package com.salaverryandres.usermanagement.infrastructure.service;

import com.salaverryandres.usermanagement.domain.service.UserService;
import com.salaverryandres.usermanagement.application.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.application.dto.UserDto;
import com.salaverryandres.usermanagement.domain.entity.UserEntity;
import com.salaverryandres.usermanagement.application.mapper.UserMapper;
import com.salaverryandres.usermanagement.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserCreateRequestDto request) {
        UserEntity entity = userMapper.toEntity(request);
        UserEntity saved = userRepository.save(entity);
        return userMapper.toDto(saved);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Override
    public UserDto getUserByExternalId(String externalId) {
        return userRepository.findByExternalId(externalId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public UserDto updateUser(String externalId, UserCreateRequestDto request) {
        UserEntity existing = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());

        return userMapper.toDto(userRepository.save(existing));
    }

    @Override
    public void deleteUser(String externalId) {
        userRepository.findByExternalId(externalId)
                .ifPresent(userRepository::delete);
    }
}

