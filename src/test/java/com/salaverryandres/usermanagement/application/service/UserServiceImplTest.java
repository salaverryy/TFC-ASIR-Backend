package com.salaverryandres.usermanagement.application.service;

import com.salaverryandres.usermanagement.application.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.application.dto.UserDto;
import com.salaverryandres.usermanagement.application.exception.BadRequestException;
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
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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

    private UserCreateRequestDto request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = UserCreateRequestDto.builder()
                .name("Johan")
                .email("johan@example.com")
                .phone("+34123456789")
                .lastName("Doe")
                .build();
    }

    @Test
    void createUser_shouldRegisterInCognitoAndSaveToDatabase() {

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

    @Test
    void createUser_shouldThrowBadRequestException_whenUsernameExistsException() {
        // Arrange
        UsernameExistsException ex = mock(UsernameExistsException.class);
        when(ex.awsErrorDetails()).thenReturn(AwsErrorDetails.builder()
                .errorMessage("Username already exists")
                .build());

        when(cognitoService.registerUser(anyString(), anyString(), anyString()))
                .thenThrow(ex);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El usuario ya existe en Cognito");

        verify(cognitoService).registerUser(anyString(), anyString(), anyString());
    }

    @Test
    void createUser_shouldThrowBadRequestException_whenInvalidParameterException() {
        // Arrange
        InvalidParameterException ex = mock(InvalidParameterException.class);
        when(ex.awsErrorDetails()).thenReturn(AwsErrorDetails.builder()
                .errorMessage("Invalid parameter")
                .build());

        when(cognitoService.registerUser(anyString(), anyString(), anyString()))
                .thenThrow(ex);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Parámetro inválido al registrar el usuario en Cognito");

        verify(cognitoService).registerUser(anyString(), anyString(), anyString());
    }

    @Test
    void createUser_shouldThrowRuntimeException_whenCognitoIdentityProviderException() {
        // Arrange
        CognitoIdentityProviderException ex = mock(CognitoIdentityProviderException.class);
        when(ex.awsErrorDetails()).thenReturn(AwsErrorDetails.builder()
                .errorMessage("General error")
                .build());

        when(cognitoService.registerUser(anyString(), anyString(), anyString()))
                .thenThrow(ex);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error al registrar el usuario en Cognito");

        verify(cognitoService).registerUser(anyString(), anyString(), anyString());
    }
}

