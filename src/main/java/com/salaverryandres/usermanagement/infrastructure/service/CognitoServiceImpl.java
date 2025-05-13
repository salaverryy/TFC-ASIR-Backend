package com.salaverryandres.usermanagement.infrastructure.service;

import com.salaverryandres.usermanagement.application.exception.BadRequestException;
import com.salaverryandres.usermanagement.domain.service.CognitoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CognitoServiceImpl implements CognitoService {

    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.cognito.client-id}")
    private String clientId;

    @Override
    public String registerUser(String name, String email, String phone) throws CognitoIdentityProviderException {

        // 1. Construir la solicitud de creación
        AdminCreateUserRequest.Builder requestBuilder = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .userAttributes(
                        AttributeType.builder().name("name").value(name).build(),
                        AttributeType.builder().name("email").value(email).build()
                );

        if (phone != null && !phone.isBlank()) {
            requestBuilder.userAttributes(
                    AttributeType.builder().name("phone_number").value(phone).build()
            );
        }

        // 2. Crear el usuario
        AdminCreateUserResponse response = cognitoClient.adminCreateUser(requestBuilder.build());

        // 3. Obtener el identificador único (sub)
        String sub = response.user().attributes().stream()
                .filter(attr -> "sub".equals(attr.name()))
                .findFirst()
                .map(AttributeType::value)
                .orElseThrow(() -> new IllegalStateException("No se encontró el sub en Cognito"));

        // 4. Actualizar atributos verificados
        List<AttributeType> verifiedAttributes = new ArrayList<>();
        verifiedAttributes.add(AttributeType.builder().name("email_verified").value("true").build());

        if (phone != null && !phone.isBlank()) {
            verifiedAttributes.add(AttributeType.builder().name("phone_number_verified").value("true").build());
        }

        AdminUpdateUserAttributesRequest updateRequest = AdminUpdateUserAttributesRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .userAttributes(verifiedAttributes)
                .build();

        cognitoClient.adminUpdateUserAttributes(updateRequest);

        return sub;
    }
}
