package com.example.univproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String fullName;
    private String name;
    private String designation;
    private String idNumber;
    private String studentId;
    private String email;
    private String major;
    private String username;
    private String role;
}
