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
public class StudentRequest {

    private String fullName;
    private String name;

    private String designation;
    private String idNumber;
    private String studentId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    private String major;

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
        return "Student";
    }

    public String resolveIdNumber() {
        if (idNumber != null && !idNumber.trim().isEmpty()) {
            return idNumber.trim();
        }
        if (studentId != null && !studentId.trim().isEmpty()) {
            return studentId.trim();
        }
        return "STU-" + System.currentTimeMillis() % 100000;
    }

    public String resolveMajor() {
        if (major != null && !major.trim().isEmpty()) {
            return major.trim();
        }
        return "General Studies";
    }
}
