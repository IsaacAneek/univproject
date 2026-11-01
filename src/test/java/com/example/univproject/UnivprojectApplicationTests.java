package com.example.univproject;

import com.example.univproject.dto.AdminRequest;
import com.example.univproject.dto.StudentRequest;
import com.example.univproject.dto.TeacherRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UnivprojectApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- Authentication & Access Control Tests ---

    @Test
    @DisplayName("Unauthenticated request to /api/admins returns 401 Unauthorized")
    void unauthenticatedRequestToAdminsReturns401() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin role can access Admin, Teacher, and Student endpoints")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanAccessAllThreeResources() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Teacher role can access Teacher and Student endpoints, but forbidden from Admin endpoints")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void teacherCanManageTeachersAndStudents() throws Exception {
        // Permitted
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());

        // Forbidden
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student role can access Student endpoints, but forbidden from Admin and Teacher endpoints")
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void studentCanOnlyManageStudents() throws Exception {
        // Permitted
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());

        // Forbidden
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HTTP Basic Authentication works for seeded admin user")
    void httpBasicAuthWithSeededAdmin() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("JWT login generates valid token and permits access to protected endpoints")
    void jwtLoginAndAccessProtectedEndpoints() throws Exception {
        // 1. Login via /api/auth/login
        String loginJson = "{\"username\":\"teacher\",\"password\":\"teacher123\"}";
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("teacher"))
                .andExpect(jsonPath("$.role").value("ROLE_TEACHER"))
                .andReturn().getResponse().getContentAsString();

        String jwtToken = objectMapper.readTree(response).get("token").asText();

        // 2. Access /api/auth/me using Bearer token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("teacher"))
                .andExpect(jsonPath("$.role").value("ROLE_TEACHER"));

        // 3. Teacher can access /api/teachers with JWT
        mockMvc.perform(get("/api/teachers")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // 4. Teacher cannot access /api/admins with JWT (Forbidden)
        mockMvc.perform(get("/api/admins")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Invalid JWT token returns 401 Unauthorized")
    void invalidJwtTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    // --- CRUD Operation Tests ---

    @Test
    @DisplayName("Admin can create, get, update, and delete an Admin with full name, designation, and ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCrudOperations() throws Exception {
        AdminRequest newAdmin = AdminRequest.builder()
                .fullName("Assistant Admin")
                .designation("Senior IT Director")
                .idNumber("ADM-TEST-001")
                .email("asst.admin@univ.edu")
                .department("IT")
                .phone("1234567890")
                .username("asstadmin")
                .password("adminpass")
                .build();

        String createResponse = mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAdmin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Assistant Admin"))
                .andExpect(jsonPath("$.designation").value("Senior IT Director"))
                .andExpect(jsonPath("$.idNumber").value("ADM-TEST-001"))
                .andExpect(jsonPath("$.email").value("asst.admin@univ.edu"))
                .andReturn().getResponse().getContentAsString();

        Long createdId = objectMapper.readTree(createResponse).get("id").asLong();

        // Get by ID
        mockMvc.perform(get("/api/admins/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId));

        // Update
        newAdmin.setFullName("Updated Assistant Admin");
        newAdmin.setDesignation("Executive IT Director");
        mockMvc.perform(put("/api/admins/" + createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Assistant Admin"))
                .andExpect(jsonPath("$.designation").value("Executive IT Director"));

        // Delete
        mockMvc.perform(delete("/api/admins/" + createdId))
                .andExpect(status().isNoContent());

        // Verify Not Found
        mockMvc.perform(get("/api/admins/" + createdId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Teacher can create, get, update, and delete a Teacher")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void teacherCrudOperations() throws Exception {
        TeacherRequest newTeacher = TeacherRequest.builder()
                .fullName("Dr. Grace Hopper")
                .designation("Senior Lecturer")
                .idNumber("TCH-TEST-001")
                .email("grace.hopper@univ.edu")
                .department("Computer Science")
                .username("gracehopper")
                .password("grace123")
                .build();

        String createResponse = mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTeacher)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Dr. Grace Hopper"))
                .andExpect(jsonPath("$.designation").value("Senior Lecturer"))
                .andExpect(jsonPath("$.idNumber").value("TCH-TEST-001"))
                .andReturn().getResponse().getContentAsString();

        Long createdId = objectMapper.readTree(createResponse).get("id").asLong();

        // Update
        newTeacher.setDesignation("Full Professor");
        mockMvc.perform(put("/api/teachers/" + createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTeacher)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designation").value("Full Professor"));

        // Delete
        mockMvc.perform(delete("/api/teachers/" + createdId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Teacher can add, edit, and remove a Student")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void teacherCanManageStudentsCrud() throws Exception {
        StudentRequest newStudent = StudentRequest.builder()
                .fullName("Jane Smith")
                .designation("Graduate Student")
                .idNumber("STU-TEST-001")
                .email("jane.smith@univ.edu")
                .major("Data Science")
                .username("janesmith")
                .password("jane123")
                .build();

        String createResponse = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Jane Smith"))
                .andExpect(jsonPath("$.designation").value("Graduate Student"))
                .andExpect(jsonPath("$.idNumber").value("STU-TEST-001"))
                .andReturn().getResponse().getContentAsString();

        Long createdId = objectMapper.readTree(createResponse).get("id").asLong();

        // Update
        newStudent.setMajor("Artificial Intelligence");
        mockMvc.perform(put("/api/students/" + createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.major").value("Artificial Intelligence"));

        // Delete
        mockMvc.perform(delete("/api/students/" + createdId))
                .andExpect(status().isNoContent());
    }
}
