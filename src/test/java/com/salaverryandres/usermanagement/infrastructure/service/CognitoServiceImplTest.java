package com.salaverryandres.usermanagement.infrastructure.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CognitoServiceImplTest {

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    @InjectMocks
    private CognitoServiceImpl cognitoService;

    @Test
    void registerUser_shouldCreateUserAndReturnSub_whenPhoneIsProvided() {
        String name = "John";
        String email = "john.doe@example.com";
        String phone = "+1234567890";
        String expectedSub = "abc-123";

        UserType userType = UserType.builder()
                .attributes(AttributeType.builder().name("sub").value(expectedSub).build())
                .build();

        AdminCreateUserResponse createUserResponse = AdminCreateUserResponse.builder()
                .user(userType)
                .build();

        when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class)))
                .thenReturn(createUserResponse);

        // Solución: no uses doNothing() porque este método devuelve un objeto
        when(cognitoClient.adminUpdateUserAttributes(any(AdminUpdateUserAttributesRequest.class)))
                .thenReturn(AdminUpdateUserAttributesResponse.builder().build());

        String sub = cognitoService.registerUser(name, email, phone);

        assertThat(sub).isEqualTo(expectedSub);
        verify(cognitoClient).adminCreateUser(any(AdminCreateUserRequest.class));
        verify(cognitoClient).adminUpdateUserAttributes(any(AdminUpdateUserAttributesRequest.class));
    }

    @Test
    void registerUser_shouldCreateUserAndReturnSub_whenPhoneIsNull() {
        String name = "Jane";
        String email = "jane.doe@example.com";
        String phone = null;
        String expectedSub = "xyz-789";

        UserType userType = UserType.builder()
                .attributes(AttributeType.builder().name("sub").value(expectedSub).build())
                .build();

        AdminCreateUserResponse createUserResponse = AdminCreateUserResponse.builder()
                .user(userType)
                .build();

        when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class)))
                .thenReturn(createUserResponse);

        // El método adminUpdateUserAttributes devuelve un objeto vacío, no es void
        when(cognitoClient.adminUpdateUserAttributes(any(AdminUpdateUserAttributesRequest.class)))
                .thenReturn(AdminUpdateUserAttributesResponse.builder().build());

        String sub = cognitoService.registerUser(name, email, phone);

        assertThat(sub).isEqualTo(expectedSub);
        verify(cognitoClient).adminCreateUser(any(AdminCreateUserRequest.class));
        verify(cognitoClient).adminUpdateUserAttributes(any(AdminUpdateUserAttributesRequest.class));
    }

    @Test
    void registerUser_shouldThrowException_whenSubIsMissing() {
        String name = "Bob";
        String email = "bob@example.com";
        String phone = "+111111111";

        UserType userType = UserType.builder()
                .attributes(AttributeType.builder().name("email").value(email).build()) // No sub
                .build();

        AdminCreateUserResponse createUserResponse = AdminCreateUserResponse.builder()
                .user(userType)
                .build();

        when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class)))
                .thenReturn(createUserResponse);

        assertThatThrownBy(() -> cognitoService.registerUser(name, email, phone))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No se encontró el sub en Cognito");

        verify(cognitoClient).adminCreateUser(any(AdminCreateUserRequest.class));
    }

}
