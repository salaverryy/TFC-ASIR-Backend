package com.salaverryandres.usermanagement.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaverryandres.usermanagement.application.dto.ChangePasswordRequestDto;
import com.salaverryandres.usermanagement.application.dto.LoginRequestDto;
import com.salaverryandres.usermanagement.application.dto.LoginResponseDto;
import com.salaverryandres.usermanagement.domain.service.CognitoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
@Import({AuthControllerTest.TestConfig.class, AuthControllerTest.TestSecurityConfig.class})
@TestPropertySource(properties = {
        "COGNITO_REGION=us-east-1",
        "COGNITO_USER_POOL_ID=test-pool"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CognitoService cognitoService; // el mock manual

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_shouldReturnToken() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .email("john.doe@example.com")
                .password("password123")
                .build();

        LoginResponseDto response = LoginResponseDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(cognitoService.login(request.getEmail(), request.getPassword()))
                .thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void changePassword_shouldReturnToken() throws Exception {
        ChangePasswordRequestDto request = ChangePasswordRequestDto.builder()
                .email("john.doe@example.com")
                .newPassword("newPassword123")
                .session("session-token")
                .build();

        LoginResponseDto response = LoginResponseDto.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(cognitoService.respondToNewPasswordChallenge(
                request.getEmail(), request.getNewPassword(), request.getSession()))
                .thenReturn(response);

        mockMvc.perform(post("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CognitoService cognitoService() {
            return mock(CognitoService.class);
        }
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public org.springframework.security.web.SecurityFilterChain testSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }
}