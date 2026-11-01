package com.example.univproject.controller;

import com.example.univproject.dto.ApiResponse;
import com.example.univproject.dto.JwtResponse;
import com.example.univproject.dto.LoginRequest;
import com.example.univproject.dto.UserProfileResponse;
import com.example.univproject.entity.Admin;
import com.example.univproject.entity.Student;
import com.example.univproject.entity.Teacher;
import com.example.univproject.entity.User;
import com.example.univproject.repository.AdminRepository;
import com.example.univproject.repository.StudentRepository;
import com.example.univproject.repository.TeacherRepository;
import com.example.univproject.repository.UserRepository;
import com.example.univproject.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          AdminRepository adminRepository,
                          TeacherRepository teacherRepository,
                          StudentRepository studentRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).build();
        }

        UserProfileResponse.UserProfileResponseBuilder profile = UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name());

        switch (user.getRole()) {
            case ROLE_ADMIN:
                Optional<Admin> admin = adminRepository.findByUser(user);
                admin.ifPresent(a -> {
                    profile.name(a.getFullName());
                    profile.email(a.getEmail());
                    profile.details("Designation: " + a.getDesignation() + ", ID: " + a.getIdNumber() + ", Dept: " + a.getDepartment());
                });
                break;
            case ROLE_TEACHER:
                Optional<Teacher> teacher = teacherRepository.findByUser(user);
                teacher.ifPresent(t -> {
                    profile.name(t.getFullName());
                    profile.email(t.getEmail());
                    profile.details("Designation: " + t.getDesignation() + ", ID: " + t.getIdNumber() + ", Dept: " + t.getDepartment());
                });
                break;
            case ROLE_STUDENT:
                Optional<Student> student = studentRepository.findByUser(user);
                student.ifPresent(s -> {
                    profile.name(s.getFullName());
                    profile.email(s.getEmail());
                    profile.details("Designation: " + s.getDesignation() + ", ID: " + s.getIdNumber() + ", Major: " + s.getMajor());
                });
                break;
        }

        return ResponseEntity.ok(profile.build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Logged out successfully")
                .build());
    }
}
