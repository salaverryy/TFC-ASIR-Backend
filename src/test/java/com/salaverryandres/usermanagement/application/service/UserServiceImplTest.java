package com.salaverryandres.usermanagement.application.service;

import com.salaverryandres.usermanagement.application.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.application.dto.UserDto;
import com.salaverryandres.usermanagement.application.mapper.UserMapper;
import com.salaverryandres.usermanagement.domain.entity.UserEntity;
import com.salaverryandres.usermanagement.domain.repository.UserRepository;
import com.salaverryandres.usermanagement.domain.service.CognitoService;
import com.salaverryandres.usermanagement.infrastructure.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private CognitoService cognitoService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_shouldRegisterInCognitoAndSaveToDatabase() {
        // Arrange
        UserCreateRequestDto request = UserCreateRequestDto.builder()
                .name("Johan")
                .email("johan@example.com")
                .phone("+34123456789")
                .build();

        UserEntity entity = new UserEntity();
        UserEntity savedEntity = new UserEntity();
        savedEntity.setExternalId("sub-123");

        UserDto expectedDto = UserDto.builder()
                .externalId("sub-123")
                .name("Johan")
                .email("johan@example.com")
                .build();

        when(cognitoService.registerUser("Johan", "johan@example.com", "+34123456789"))
                .thenReturn("sub-123");

        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(savedEntity);
        when(userMapper.toDto(savedEntity)).thenReturn(expectedDto);

        // Act
        UserDto result = userService.createUser(request);

        // Assert
        assertThat(result).isEqualTo(expectedDto);
        verify(cognitoService).registerUser("Johan", "johan@example.com", "+34123456789");
        verify(userRepository).save(entity);
        verify(userMapper).toDto(savedEntity);
    }
}