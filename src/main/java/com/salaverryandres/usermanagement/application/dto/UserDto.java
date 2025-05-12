package com.salaverryandres.usermanagement.application.dto;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String externalId;
    private String name;
    private String email;
    private String phone;
    private Set<RoleDto> roles;
}
