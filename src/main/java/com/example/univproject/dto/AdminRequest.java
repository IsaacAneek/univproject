package com.example.univproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequest {

    private String fullName;
    private String name;

    private String designation;
    private String idNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    private String department;
    private String phone;

    private String username;
    private String password;

    public String resolveFullName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName.trim();
        }
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        return "";
    }

    public String resolveDesignation() {
        if (designation != null && !designation.trim().isEmpty()) {
            return designation.trim();
        }
        return "Administrator";
    }

    public String resolveIdNumber() {
        if (idNumber != null && !idNumber.trim().isEmpty()) {
            return idNumber.trim();
        }
        return "ADM-" + System.currentTimeMillis() % 100000;
    }
}
