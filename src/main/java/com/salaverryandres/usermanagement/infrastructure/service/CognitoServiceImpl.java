package com.salaverryandres.usermanagement.infrastructure.service;

import com.salaverryandres.usermanagement.application.dto.LoginResponseDto;
import com.salaverryandres.usermanagement.application.exception.BadRequestException;
import com.salaverryandres.usermanagement.application.exception.ChallengeRequiredException;
import com.salaverryandres.usermanagement.application.exception.NotFoundException;
import com.salaverryandres.usermanagement.domain.service.CognitoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Override
    public void updateUserAttributes(String username, String name, String email, String phone) {
        List<AttributeType> attributes = new ArrayList<>();
        attributes.add(AttributeType.builder().name("name").value(name).build());
        attributes.add(AttributeType.builder().name("email").value(email).build());

        if (phone != null && !phone.isBlank()) {
            attributes.add(AttributeType.builder().name("phone_number").value(phone).build());
        }

        AdminUpdateUserAttributesRequest request = AdminUpdateUserAttributesRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .userAttributes(attributes)
                .build();

        cognitoClient.adminUpdateUserAttributes(request);
    }

    @Override
    public void deleteUser(String username) {
        AdminDeleteUserRequest request = AdminDeleteUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        cognitoClient.adminDeleteUser(request);
    }

    @Override
    public void addUserToGroup(String username, String groupName) {
        AdminAddUserToGroupRequest request = AdminAddUserToGroupRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .groupName(groupName)
                .build();

        cognitoClient.adminAddUserToGroup(request);
    }

    @Override
    public LoginResponseDto login(String email, String password) {
        Map<String, String> authParams = Map.of(
                "USERNAME", email,
                "PASSWORD", password
        );

        InitiateAuthRequest request = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .authParameters(authParams)
                .clientId(clientId)
                .build();

        try {
            InitiateAuthResponse response = cognitoClient.initiateAuth(request);

            if (response.challengeName() == ChallengeNameType.NEW_PASSWORD_REQUIRED) {
                // Devolvemos info útil para el frontend/backend
                throw new ChallengeRequiredException(
                        ChallengeNameType.NEW_PASSWORD_REQUIRED.name(),
                        response.session(),
                        "Se requiere cambiar la contraseña"
                );
            }

            AuthenticationResultType result = response.authenticationResult();
            if (result == null) {
                throw new RuntimeException("Error inesperado: no se recibió token de autenticación");
            }

            return LoginResponseDto.builder()
                    .accessToken(result.accessToken())
                    .idToken(result.idToken())
                    .refreshToken(result.refreshToken())
                    .expiresIn(result.expiresIn())
                    .tokenType(result.tokenType())
                    .build();

        } catch (NotAuthorizedException e) {
            throw new BadRequestException("Credenciales incorrectas");
        } catch (UserNotFoundException e) {
            throw new NotFoundException("Usuario no encontrado");
        } catch (Exception e) {
            throw new RuntimeException("Error al iniciar sesión", e);
        }
    }

    @Override
    public LoginResponseDto respondToNewPasswordChallenge(String email, String newPassword, String session) {
        try {
            Map<String, String> challengeResponses = Map.of(
                    "USERNAME", email,
                    "NEW_PASSWORD", newPassword
            );

            RespondToAuthChallengeRequest challengeRequest = RespondToAuthChallengeRequest.builder()
                    .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                    .clientId(clientId)
                    .challengeResponses(challengeResponses)
                    .session(session)
                    .build();

            RespondToAuthChallengeResponse response = cognitoClient.respondToAuthChallenge(challengeRequest);
            AuthenticationResultType result = response.authenticationResult();

            return LoginResponseDto.builder()
                    .accessToken(result.accessToken())
                    .idToken(result.idToken())
                    .refreshToken(result.refreshToken())
                    .expiresIn(result.expiresIn())
                    .tokenType(result.tokenType())
                    .build();

        } catch (NotAuthorizedException e) {
            throw new BadRequestException("Credenciales inválidas");
        } catch (InvalidPasswordException e) {
            throw new BadRequestException("La nueva contraseña no cumple con los requisitos de seguridad");
        } catch (Exception e) {
            throw new RuntimeException("Error al cambiar la contraseña", e);
        }
    }

    @Override
    public void logout(String username) {
        AdminUserGlobalSignOutRequest request = AdminUserGlobalSignOutRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        try {
            cognitoClient.adminUserGlobalSignOut(request);
        } catch (UserNotFoundException e) {
            throw new NotFoundException("Usuario no encontrado");
        } catch (Exception e) {
            throw new RuntimeException("Error al cerrar sesión", e);
        }
    }

}
