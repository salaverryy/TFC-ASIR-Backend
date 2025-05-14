package com.salaverryandres.usermanagement.infrastructure.service;

import com.salaverryandres.usermanagement.application.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.application.dto.UserDto;
import com.salaverryandres.usermanagement.application.dto.UserPageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

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

            // 2. Agregar el usuario al grupo USER en Cognito
            cognitoService.addUserToGroup(request.getEmail(), "USER");

            // 3. Convertir DTO a Entity y setear el externalId
            UserEntity entity = userMapper.toEntity(request);
            entity.setExternalId(externalId);

            // 4. Guardar en la base de datos
            UserEntity saved = userRepository.save(entity);

            // 5. Retornar el resultado
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
    public UserPageResponse getAllUsers(Pageable pageable) {
        Page<UserEntity> page = userRepository.findAll(pageable);
        return UserPageResponse.builder()
                .users(userMapper.toDtoList(page.getContent()))
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }


    @Override
    public UserDto getUserByExternalId(String externalId) {
        return userRepository.findByExternalId(externalId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
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

