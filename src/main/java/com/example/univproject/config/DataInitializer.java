package com.example.univproject.config;

import com.example.univproject.entity.Admin;
import com.example.univproject.entity.Role;
import com.example.univproject.entity.Student;
import com.example.univproject.entity.Teacher;
import com.example.univproject.entity.User;
import com.example.univproject.repository.AdminRepository;
import com.example.univproject.repository.StudentRepository;
import com.example.univproject.repository.TeacherRepository;
import com.example.univproject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           AdminRepository adminRepository,
                           TeacherRepository teacherRepository,
                           StudentRepository studentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initAdmin();
        initTeacher();
        initStudent();
        backfillExistingRecords();
    }

    private void initAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            log.info("Creating default ADMIN user: admin / admin123");
            User user = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();

            Admin admin = Admin.builder()
                    .fullName("System Administrator")
                    .name("System Administrator")
                    .designation("Chief Systems Administrator")
                    .idNumber("ADM-001")
                    .email("admin@univ.edu")
                    .department("Administration")
                    .phone("+1-555-0100")
                    .user(user)
                    .build();

            adminRepository.save(admin);
        }
    }

    private void initTeacher() {
        if (!userRepository.existsByUsername("teacher")) {
            log.info("Creating default TEACHER user: teacher / teacher123");
            User user = User.builder()
                    .username("teacher")
                    .password(passwordEncoder.encode("teacher123"))
                    .role(Role.ROLE_TEACHER)
                    .enabled(true)
                    .build();

            Teacher teacher = Teacher.builder()
                    .fullName("Dr. Sarah Connor")
                    .name("Dr. Sarah Connor")
                    .designation("Associate Professor")
                    .idNumber("TCH-001")
                    .email("sarah.connor@univ.edu")
                    .department("Computer Science")
                    .user(user)
                    .build();

            teacherRepository.save(teacher);
        }
    }

    private void initStudent() {
        if (!userRepository.existsByUsername("student")) {
            log.info("Creating default STUDENT user: student / student123");
            User user = User.builder()
                    .username("student")
                    .password(passwordEncoder.encode("student123"))
                    .role(Role.ROLE_STUDENT)
                    .enabled(true)
                    .build();

            Student student = Student.builder()
                    .fullName("John Doe")
                    .name("John Doe")
                    .designation("Undergraduate Student")
                    .idNumber("STU-001")
                    .studentId("STU-001")
                    .email("john.doe@univ.edu")
                    .major("Software Engineering")
                    .user(user)
                    .build();

            studentRepository.save(student);
        }
    }

    private void backfillExistingRecords() {
        try {
            adminRepository.findAll().forEach(admin -> {
                boolean modified = false;
                if (admin.getIdNumber() == null || admin.getIdNumber().trim().isEmpty()) {
                    admin.setIdNumber("ADM-" + String.format("%03d", admin.getId()));
                    modified = true;
                }
                if (admin.getFullName() == null || admin.getFullName().trim().isEmpty()) {
                    admin.setFullName(admin.getName() != null ? admin.getName() : "Administrator");
                    modified = true;
                }
                if (admin.getDesignation() == null || admin.getDesignation().trim().isEmpty()) {
                    admin.setDesignation("Administrator");
                    modified = true;
                }
                if (modified) {
                    adminRepository.save(admin);
                }
            });

            teacherRepository.findAll().forEach(teacher -> {
                boolean modified = false;
                if (teacher.getIdNumber() == null || teacher.getIdNumber().trim().isEmpty()) {
                    teacher.setIdNumber("TCH-" + String.format("%03d", teacher.getId()));
                    modified = true;
                }
                if (teacher.getFullName() == null || teacher.getFullName().trim().isEmpty()) {
                    teacher.setFullName(teacher.getName() != null ? teacher.getName() : "Faculty Teacher");
                    modified = true;
                }
                if (teacher.getDesignation() == null || teacher.getDesignation().trim().isEmpty()) {
                    teacher.setDesignation("Teacher");
                    modified = true;
                }
                if (modified) {
                    teacherRepository.save(teacher);
                }
            });

            studentRepository.findAll().forEach(student -> {
                boolean modified = false;
                if (student.getIdNumber() == null || student.getIdNumber().trim().isEmpty()) {
                    String existingId = student.getStudentId();
                    student.setIdNumber(existingId != null && !existingId.trim().isEmpty() ? existingId : "STU-" + String.format("%03d", student.getId()));
                    modified = true;
                }
                if (student.getFullName() == null || student.getFullName().trim().isEmpty()) {
                    student.setFullName(student.getName() != null ? student.getName() : "Student");
                    modified = true;
                }
                if (student.getDesignation() == null || student.getDesignation().trim().isEmpty()) {
                    student.setDesignation("Student");
                    modified = true;
                }
                if (modified) {
                    studentRepository.save(student);
                }
            });
        } catch (Exception e) {
            log.warn("Backfill warning: {}", e.getMessage());
        }
    }
}
