package com.salaverryandres.usermanagement.domain.service;

import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

public interface CognitoService {
    /**
     * Registra un usuario en Cognito con el rol USER.
     * @param name nombre del usuario
     * @param email correo electrónico
     * @param phone teléfono (opcional)
     * @return sub del usuario (externalId)
     */
    String registerUser(String name, String email, String phone) throws CognitoIdentityProviderException;
}

