package com.salaverryandres.usermanagement.infrastructure.service;

import com.salaverryandres.usermanagement.application.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.application.dto.UserDto;
import com.salaverryandres.usermanagement.application.exception.BadRequestException;
import com.salaverryandres.usermanagement.application.exception.NotFoundException;
import com.salaverryandres.usermanagement.application.mapper.UserMapper;
import com.salaverryandres.usermanagement.domain.entity.UserEntity;
import com.salaverryandres.usermanagement.domain.repository.UserRepository;
import com.salaverryandres.usermanagement.domain.service.CognitoService;
import com.salaverryandres.usermanagement.domain.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

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
        UserEntity user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        try {
            cognitoService.updateUserAttributes(externalId, request.getName(), request.getEmail(), request.getPhone());
        } catch (UserNotFoundException e) {
            throw new NotFoundException("Usuario no encontrado en Cognito");
        } catch (AliasExistsException e) {
            log.error("El alias ya existe en Cognito: {}", e.awsErrorDetails().errorMessage());
            throw new BadRequestException("El alias ya existe en Cognito", e);
        } catch (InvalidParameterException e) {
            log.error("Parámetro inválido al actualizar usuario en Cognito: {}", e.awsErrorDetails().errorMessage());
            throw new BadRequestException("Parámetro inválido al actualizar el usuario en Cognito", e);
        } catch (CognitoIdentityProviderException e) {
            log.error("Error al actualizar Cognito: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("No se pudo actualizar el usuario en Cognito", e);
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        return userMapper.toDto(userRepository.save(user));
    }


    @Override
    public void deleteUser(String externalId) {
        UserEntity user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        try {
            cognitoService.deleteUser(user.getEmail()); // usamos email como username en Cognito
        } catch (UserNotFoundException e) {
            throw new NotFoundException("Usuario no encontrado en Cognito");
        } catch (CognitoIdentityProviderException e) {
            log.error("Error al eliminar usuario en Cognito: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("No se pudo eliminar el usuario en Cognito", e);
        }

        userRepository.delete(user);
    }

}

