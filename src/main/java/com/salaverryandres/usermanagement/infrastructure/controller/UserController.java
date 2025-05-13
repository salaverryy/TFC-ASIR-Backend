package com.salaverryandres.usermanagement.infrastructure.controller;

import com.salaverryandres.usermanagement.application.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.application.dto.UserDto;
import com.salaverryandres.usermanagement.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequestDto request) {
        UserDto user = userService.createUser(request);
        return ResponseEntity
                .created(URI.create("/api/users/" + user.getExternalId()))
                .body(user);
    }

    @PutMapping("/{externalId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String externalId,
            @Valid @RequestBody UserCreateRequestDto request) {
        UserDto updated = userService.updateUser(externalId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{externalId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String externalId) {
        userService.deleteUser(externalId);
        return ResponseEntity.noContent().build();
    }

}
