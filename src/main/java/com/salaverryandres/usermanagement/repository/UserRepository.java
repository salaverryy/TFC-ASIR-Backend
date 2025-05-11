package com.salaverryandres.usermanagement.repository;

import com.salaverryandres.usermanagement.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    // Buscar por externalId (usado como referencia al sub de Cognito)
    Optional<UserEntity> findByExternalId(String externalId);

    boolean existsByEmail(String email);
}
