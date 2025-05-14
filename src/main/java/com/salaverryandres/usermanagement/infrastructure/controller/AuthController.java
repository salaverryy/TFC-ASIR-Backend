package com.salaverryandres.usermanagement.infrastructure.controller;

import com.salaverryandres.usermanagement.application.dto.ChangePasswordRequestDto;
import com.salaverryandres.usermanagement.application.dto.LoginRequestDto;
import com.salaverryandres.usermanagement.application.dto.LoginResponseDto;
import com.salaverryandres.usermanagement.domain.service.CognitoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CognitoService cognitoService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(cognitoService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<LoginResponseDto> changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
        return ResponseEntity.ok(
                cognitoService.respondToNewPasswordChallenge(
                        request.getEmail(),
                        request.getNewPassword(),
                        request.getSession()
                )
        );
    }

}
