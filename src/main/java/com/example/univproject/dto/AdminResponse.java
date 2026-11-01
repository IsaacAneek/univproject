package com.example.univproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {
    private Long id;
    private String fullName;
    private String name;
    private String designation;
    private String idNumber;
    private String email;
    private String department;
    private String phone;
    private String username;
    private String role;
}
