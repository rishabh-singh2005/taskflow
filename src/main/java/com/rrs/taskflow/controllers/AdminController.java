package com.rrs.taskflow.controllers;

import com.rrs.taskflow.dtos.ProjectDto;
import com.rrs.taskflow.dtos.RegisterRequestDto;
import com.rrs.taskflow.dtos.TaskDto;
import com.rrs.taskflow.enums.Role;
import com.rrs.taskflow.response.ApiResponse;
import com.rrs.taskflow.services.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ==========================================================
    // USER MANAGEMENT
    // ==========================================================

    // get all users with pagination
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<RegisterRequestDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<RegisterRequestDto> users = adminService.getAllUsers(page, size);
        ApiResponse<Page<RegisterRequestDto>> response = new ApiResponse<>("Users fetched successfully", users);
        return ResponseEntity.ok(response);
    }

    // get single user by id
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<RegisterRequestDto>> getUserById(@PathVariable Long id) {
        RegisterRequestDto user = adminService.getUserById(id);
        ApiResponse<RegisterRequestDto> response = new ApiResponse<>("User fetched successfully", user);
        return ResponseEntity.ok(response);
    }

    // delete user by id
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        ApiResponse<String> response = new ApiResponse<>("User deleted successfully", null);
        return ResponseEntity.ok(response);
    }

    // change role of a user — admin or member
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<String>> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role
    ) {
        adminService.updateUserRole(id, role);
        ApiResponse<String> response = new ApiResponse<>("User role updated successfully", null);
        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // PROJECT MANAGEMENT
    // ==========================================================

    // create new project — logged in admin becomes creator
    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(
            @Valid @RequestBody ProjectDto dto,
            Principal principal
    ) {
        ProjectDto project = adminService.createProject(dto, principal.getName());
        ApiResponse<ProjectDto> response = new ApiResponse<>("Project created successfully", project);
        return ResponseEntity.ok(response);
    }

    // get all projects — supports keyword search and pagination
    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<Page<ProjectDto>>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        Page<ProjectDto> projects = adminService.getAllProjects(page, size, keyword);
        ApiResponse<Page<ProjectDto>> response = new ApiResponse<>("Projects fetched successfully", projects);
        return ResponseEntity.ok(response);
    }

    // get single project by id
    @GetMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> getProjectById(@PathVariable Long id) {
        ProjectDto project = adminService.getProjectById(id);
        ApiResponse<ProjectDto> response = new ApiResponse<>("Project fetched successfully", project);
        return ResponseEntity.ok(response);
    }

    // update project details
    @PutMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDto dto
    ) {
        ProjectDto project = adminService.updateProject(id, dto);
        ApiResponse<ProjectDto> response = new ApiResponse<>("Project updated successfully", project);
        return ResponseEntity.ok(response);
    }

    // delete project by id
    @DeleteMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProject(@PathVariable Long id) {
        adminService.deleteProject(id);
        ApiResponse<String> response = new ApiResponse<>("Project deleted successfully", null);
        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // TASK MANAGEMENT
    // ==========================================================

    // create task and optionally assign to a member
    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<TaskDto>> createTask(
            @Valid @RequestBody TaskDto dto,
            Principal principal
    ) {
        TaskDto task = adminService.createTask(dto, principal.getName());
        ApiResponse<TaskDto> response = new ApiResponse<>("Task created successfully", task);
        return ResponseEntity.ok(response);
    }

    // get all tasks — supports keyword search and pagination
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<Page<TaskDto>>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        Page<TaskDto> tasks = adminService.getAllTasks(page, size, keyword);
        ApiResponse<Page<TaskDto>> response = new ApiResponse<>("Tasks fetched successfully", tasks);
        return ResponseEntity.ok(response);
    }

    // get single task by id
    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<TaskDto>> getTaskById(@PathVariable Long id) {
        TaskDto task = adminService.getTaskById(id);
        ApiResponse<TaskDto> response = new ApiResponse<>("Task fetched successfully", task);
        return ResponseEntity.ok(response);
    }

    // update task details
    @PutMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<TaskDto>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskDto dto
    ) {
        TaskDto task = adminService.updateTask(id, dto);
        ApiResponse<TaskDto> response = new ApiResponse<>("Task updated successfully", task);
        return ResponseEntity.ok(response);
    }

    // delete task by id
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable Long id) {
        adminService.deleteTask(id);
        ApiResponse<String> response = new ApiResponse<>("Task deleted successfully", null);
        return ResponseEntity.ok(response);
    }

    // assign or reassign task to a member by their email
    @PatchMapping("/tasks/{id}/assign")
    public ResponseEntity<ApiResponse<TaskDto>> assignTask(
            @PathVariable Long id,
            @RequestParam String memberEmail
    ) {
        TaskDto task = adminService.assignTask(id, memberEmail);
        ApiResponse<TaskDto> response = new ApiResponse<>("Task assigned successfully", task);
        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // DASHBOARD
    // ==========================================================

    // get overall stats — total users, projects, tasks, completed tasks
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Object>> getDashboardStats() {
        Object stats = adminService.getDashboardStats();
        ApiResponse<Object> response = new ApiResponse<>("Dashboard stats fetched successfully", stats);
        return ResponseEntity.ok(response);
    }
}