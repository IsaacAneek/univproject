package com.example.univproject.service;

import com.example.univproject.dto.AdminRequest;
import com.example.univproject.dto.AdminResponse;
import com.example.univproject.entity.Admin;
import com.example.univproject.entity.Role;
import com.example.univproject.entity.User;
import com.example.univproject.exception.DuplicateResourceException;
import com.example.univproject.exception.ResourceNotFoundException;
import com.example.univproject.repository.AdminRepository;
import com.example.univproject.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminResponse getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        return mapToResponse(admin);
    }

    public AdminResponse createAdmin(AdminRequest request) {
        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        String idNumber = request.resolveIdNumber();
        if (adminRepository.existsByIdNumber(idNumber)) {
            throw new DuplicateResourceException("ID number already exists: " + idNumber);
        }

        String username = request.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = idNumber.toLowerCase();
        }

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists: " + username);
        }

        String password = request.getPassword();
        if (password == null || password.trim().isEmpty()) {
            password = "password123";
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build();

        Admin admin = Admin.builder()
                .fullName(request.resolveFullName())
                .designation(request.resolveDesignation())
                .idNumber(idNumber)
                .email(request.getEmail())
                .department(request.getDepartment())
                .phone(request.getPhone())
                .user(user)
                .build();

        Admin saved = adminRepository.save(admin);
        return mapToResponse(saved);
    }

    public AdminResponse updateAdmin(Long id, AdminRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        if (adminRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        String idNumber = request.resolveIdNumber();
        if (adminRepository.existsByIdNumberAndIdNot(idNumber, id)) {
            throw new DuplicateResourceException("ID number already exists: " + idNumber);
        }

        admin.setFullName(request.resolveFullName());
        admin.setDesignation(request.resolveDesignation());
        admin.setIdNumber(idNumber);
        admin.setEmail(request.getEmail());
        admin.setDepartment(request.getDepartment());
        admin.setPhone(request.getPhone());

        User user = admin.getUser();
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            String newUsername = request.getUsername().trim();
            if (userRepository.existsByUsernameAndIdNot(newUsername, user.getId())) {
                throw new DuplicateResourceException("Username already exists: " + newUsername);
            }
            user.setUsername(newUsername);
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Admin updated = adminRepository.save(admin);
        return mapToResponse(updated);
    }

    public void deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        adminRepository.delete(admin);
    }

    public AdminResponse mapToResponse(Admin admin) {
        return AdminResponse.builder()
                .id(admin.getId())
                .fullName(admin.getFullName())
                .name(admin.getFullName())
                .designation(admin.getDesignation())
                .idNumber(admin.getIdNumber())
                .email(admin.getEmail())
                .department(admin.getDepartment())
                .phone(admin.getPhone())
                .username(admin.getUser() != null ? admin.getUser().getUsername() : null)
                .role(admin.getUser() != null ? admin.getUser().getRole().name() : null)
                .build();
    }
}
