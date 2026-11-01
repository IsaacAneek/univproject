package com.example.univproject.repository;

import com.example.univproject.entity.Teacher;
import com.example.univproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUser(User user);
    Optional<Teacher> findByUserUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByIdNumber(String idNumber);
    boolean existsByIdNumberAndIdNot(String idNumber, Long id);
}
