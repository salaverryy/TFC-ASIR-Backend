package com.salaverryandres.usermanagement.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String externalId;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String role;
}
