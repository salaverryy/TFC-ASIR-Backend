package com.salaverryandres.usermanagement.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaverryandres.usermanagement.application.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.application.dto.UserDto;
import com.salaverryandres.usermanagement.application.dto.UserPageResponse;
import com.salaverryandres.usermanagement.domain.service.UserService;
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

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@Import({UserControllerTest.TestConfig.class, UserControllerTest.TestSecurityConfig.class})
@TestPropertySource(properties = {
        "COGNITO_REGION=us-east-1",
        "COGNITO_USER_POOL_ID=test-pool"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService; // El mock creado manualmente

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserCreateRequestDto requestDto = UserCreateRequestDto.builder()
                .name("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();

        UserDto responseDto = UserDto.builder()
                .externalId("abc-123")
                .name("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role("USER")
                .build();

        when(userService.createUser(any(UserCreateRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value("abc-123"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        String externalId = "abc-123";

        UserCreateRequestDto requestDto = UserCreateRequestDto.builder()
                .name("Updated")
                .lastName("User")
                .email("updated.user@example.com")
                .phone("987654321")
                .build();

        UserDto updatedDto = UserDto.builder()
                .externalId(externalId)
                .name("Updated")
                .lastName("User")
                .email("updated.user@example.com")
                .phone("987654321")
                .role("USER")
                .build();

        when(userService.updateUser(eq(externalId), any(UserCreateRequestDto.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/users/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(externalId))
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        String externalId = "abc-123";

        doNothing().when(userService).deleteUser(externalId);

        mockMvc.perform(delete("/api/users/{externalId}", externalId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(externalId);
    }

    @Test
    void getUserByExternalId_shouldReturnUserForSelf() throws Exception {
        String externalId = "abc-123";
        UserDto userDto = UserDto.builder()
                .externalId(externalId)
                .name("John")
                .build();

        when(userService.getUserByExternalId(externalId)).thenReturn(userDto);

        mockMvc.perform(get("/api/users/{externalId}", externalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(externalId))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void getAllUsers_shouldReturnPage() throws Exception {
        UserDto user = UserDto.builder()
                .externalId("abc-123")
                .name("John")
                .build();

        UserPageResponse pageResponse = new UserPageResponse();
        pageResponse.setUsers(List.of(user));
        pageResponse.setTotalElements(1);

        when(userService.getAllUsers(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.users[0].externalId").value("abc-123"));
    }

    // Mockeamos UserService manualmente
    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }
    }

    // Configuración de seguridad de prueba: permite todos los accesos
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