package com.salaverryandres.usermanagement.dto;

import lombok.*;

import java.util.Set;
import com.salaverryandres.usermanagement.dto.RoleDto;

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
