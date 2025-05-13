package com.salaverryandres.usermanagement.infrastructure.service;

import com.salaverryandres.usermanagement.application.exception.BadRequestException;
import com.salaverryandres.usermanagement.domain.service.CognitoService;
import com.salaverryandres.usermanagement.domain.service.UserService;
import com.salaverryandres.usermanagement.application.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.application.dto.UserDto;
import com.salaverryandres.usermanagement.domain.entity.UserEntity;
import com.salaverryandres.usermanagement.application.mapper.UserMapper;
import com.salaverryandres.usermanagement.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CognitoService cognitoService;

    @Override
    public UserDto createUser(UserCreateRequestDto request) {
        try {
            // 1. Crear en Cognito y obtener el externalId (sub)
            String externalId = cognitoService.registerUser(
                    request.getName(),
                    request.getEmail(),
                    request.getPhone()
            );

            // 2. Convertir DTO a Entity y setear el externalId
            UserEntity entity = userMapper.toEntity(request);
            entity.setExternalId(externalId);

            // 3. Guardar en la base de datos
            UserEntity saved = userRepository.save(entity);

            // 4. Retornar el resultado
            return userMapper.toDto(saved);
        } catch (UsernameExistsException e) {
            log.error("El usuario ya existe en Cognito: {}", e.awsErrorDetails().errorMessage());
            throw new BadRequestException("El usuario ya existe en Cognito", e);
        } catch (InvalidParameterException e) {
            log.error("Parámetro inválido al crear usuario en Cognito: {}", e.awsErrorDetails().errorMessage());
            throw new BadRequestException("Parámetro inválido al registrar el usuario en Cognito", e);
        } catch (CognitoIdentityProviderException e) {
            log.error("Error al crear usuario en Cognito: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Error al registrar el usuario en Cognito", e);
        }
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

