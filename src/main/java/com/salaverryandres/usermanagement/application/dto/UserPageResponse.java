package com.salaverryandres.usermanagement.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPageResponse {
    private List<UserDto> users;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}

