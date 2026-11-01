package com.example.univproject.service;

import com.example.univproject.dto.StudentRequest;
import com.example.univproject.dto.StudentResponse;
import com.example.univproject.entity.Role;
import com.example.univproject.entity.Student;
import com.example.univproject.entity.User;
import com.example.univproject.exception.DuplicateResourceException;
import com.example.univproject.exception.ResourceNotFoundException;
import com.example.univproject.repository.StudentRepository;
import com.example.univproject.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return mapToResponse(student);
    }

    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        String idNumber = request.resolveIdNumber();
        if (studentRepository.existsByIdNumber(idNumber)) {
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
                .role(Role.ROLE_STUDENT)
                .enabled(true)
                .build();

        Student student = Student.builder()
                .fullName(request.resolveFullName())
                .designation(request.resolveDesignation())
                .idNumber(idNumber)
                .email(request.getEmail())
                .major(request.resolveMajor())
                .user(user)
                .build();

        Student saved = studentRepository.save(student);
        return mapToResponse(saved);
    }

    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (studentRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        String idNumber = request.resolveIdNumber();
        if (studentRepository.existsByIdNumberAndIdNot(idNumber, id)) {
            throw new DuplicateResourceException("ID number already exists: " + idNumber);
        }

        student.setFullName(request.resolveFullName());
        student.setDesignation(request.resolveDesignation());
        student.setIdNumber(idNumber);
        student.setEmail(request.getEmail());
        student.setMajor(request.resolveMajor());

        User user = student.getUser();
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

        Student updated = studentRepository.save(student);
        return mapToResponse(updated);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        studentRepository.delete(student);
    }

    public StudentResponse mapToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .fullName(student.getFullName())
                .name(student.getFullName())
                .designation(student.getDesignation())
                .idNumber(student.getIdNumber())
                .studentId(student.getIdNumber())
                .email(student.getEmail())
                .major(student.getMajor())
                .username(student.getUser() != null ? student.getUser().getUsername() : null)
                .role(student.getUser() != null ? student.getUser().getRole().name() : null)
                .build();
    }
}
