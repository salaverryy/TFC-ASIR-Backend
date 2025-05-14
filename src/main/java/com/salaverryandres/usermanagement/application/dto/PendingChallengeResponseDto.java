package com.salaverryandres.usermanagement.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingChallengeResponseDto {
    private String challenge;
    private String session; // requerido para RespondToAuthChallenge
    private String message;
}
