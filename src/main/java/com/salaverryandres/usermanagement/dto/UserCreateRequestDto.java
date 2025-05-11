package com.salaverryandres.usermanagement.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {
    private String externalId;
    private String name;
    private String email;
    private String phone;
}
