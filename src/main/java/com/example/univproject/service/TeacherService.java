package com.example.univproject.service;

import com.example.univproject.dto.TeacherRequest;
import com.example.univproject.dto.TeacherResponse;
import com.example.univproject.entity.Role;
import com.example.univproject.entity.Teacher;
import com.example.univproject.entity.User;
import com.example.univproject.exception.DuplicateResourceException;
import com.example.univproject.exception.ResourceNotFoundException;
import com.example.univproject.repository.TeacherRepository;
import com.example.univproject.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherRepository teacherRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeachers() {
        return teacherRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
        return mapToResponse(teacher);
    }

    public TeacherResponse createTeacher(TeacherRequest request) {
        if (teacherRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        String idNumber = request.resolveIdNumber();
        if (teacherRepository.existsByIdNumber(idNumber)) {
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
                .role(Role.ROLE_TEACHER)
                .enabled(true)
                .build();

        Teacher teacher = Teacher.builder()
                .fullName(request.resolveFullName())
                .designation(request.resolveDesignation())
                .idNumber(idNumber)
                .email(request.getEmail())
                .department(request.resolveDepartment())
                .user(user)
                .build();

        Teacher saved = teacherRepository.save(teacher);
        return mapToResponse(saved);
    }

    public TeacherResponse updateTeacher(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        if (teacherRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        String idNumber = request.resolveIdNumber();
        if (teacherRepository.existsByIdNumberAndIdNot(idNumber, id)) {
            throw new DuplicateResourceException("ID number already exists: " + idNumber);
        }

        teacher.setFullName(request.resolveFullName());
        teacher.setDesignation(request.resolveDesignation());
        teacher.setIdNumber(idNumber);
        teacher.setEmail(request.getEmail());
        teacher.setDepartment(request.resolveDepartment());

        User user = teacher.getUser();
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

        Teacher updated = teacherRepository.save(teacher);
        return mapToResponse(updated);
    }

    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
        teacherRepository.delete(teacher);
    }

    public TeacherResponse mapToResponse(Teacher teacher) {
        return TeacherResponse.builder()
                .id(teacher.getId())
                .fullName(teacher.getFullName())
                .name(teacher.getFullName())
                .designation(teacher.getDesignation())
                .idNumber(teacher.getIdNumber())
                .email(teacher.getEmail())
                .department(teacher.getDepartment())
                .username(teacher.getUser() != null ? teacher.getUser().getUsername() : null)
                .role(teacher.getUser() != null ? teacher.getUser().getRole().name() : null)
                .build();
    }
}
