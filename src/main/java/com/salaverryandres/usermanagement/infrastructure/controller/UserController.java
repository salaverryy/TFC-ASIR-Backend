package com.salaverryandres.usermanagement.infrastructure.controller;

import com.salaverryandres.usermanagement.application.dto.UserCreateRequestDto;
import com.salaverryandres.usermanagement.application.dto.UserDto;
import com.salaverryandres.usermanagement.application.dto.UserPageResponse;
import com.salaverryandres.usermanagement.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequestDto request) {
        UserDto user = userService.createUser(request);
        return ResponseEntity
                .created(URI.create("/api/users/" + user.getExternalId()))
                .body(user);
    }

    @PreAuthorize("hasRole('ADMIN') or #externalId == authentication.name")
    @PutMapping("/{externalId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String externalId,
            @Valid @RequestBody UserCreateRequestDto request) {
        UserDto updated = userService.updateUser(externalId, request);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{externalId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String externalId) {
        userService.deleteUser(externalId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{externalId}")
    public ResponseEntity<UserDto> getUserByExternalId(@PathVariable String externalId) {
        UserDto user = userService.getUserByExternalId(externalId);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<UserPageResponse> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

}
