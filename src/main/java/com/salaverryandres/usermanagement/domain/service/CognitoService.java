package com.salaverryandres.usermanagement.domain.service;

public interface CognitoService {
    /**
     * Registra un usuario en Cognito con el rol USER.
     * @param name nombre del usuario
     * @param email correo electrónico
     * @param phone teléfono (opcional)
     * @return sub del usuario (externalId)
     */
    String registerUser(String name, String email, String phone);
}

