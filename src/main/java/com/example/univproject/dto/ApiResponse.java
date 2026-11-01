package com.example.univproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private boolean success;
    private String message;
}

