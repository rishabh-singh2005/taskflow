package com.rrs.taskflow.services;

import com.rrs.taskflow.dtos.ProjectDto;
import com.rrs.taskflow.dtos.TaskDto;
import com.rrs.taskflow.dtos.RegisterRequestDto;
import com.rrs.taskflow.enums.Role;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminService {

    // ================= USER MANAGEMENT =================
    // Get all users with pagination
    Page<RegisterRequestDto> getAllUsers(int page, int size);

    // Get single user by id
    RegisterRequestDto getUserById(Long id);

    // Delete user by id
    void deleteUser(Long id);

    // Change user role
    void updateUserRole(Long id, Role role);

    // ================= PROJECT MANAGEMENT =================
    // Create new project
    ProjectDto createProject(ProjectDto dto, String adminEmail);

    // Get all projects with pagination and search
    Page<ProjectDto> getAllProjects(int page, int size, String keyword);

    // Get single project by id
    ProjectDto getProjectById(Long id);

    // Update project
    ProjectDto updateProject(Long id, ProjectDto dto);

    // Delete project
    void deleteProject(Long id);

    // ================= TASK MANAGEMENT =================
    // Create task and assign to member
    TaskDto createTask(TaskDto dto, String adminEmail);

    // Get all tasks with pagination, search and filter
    Page<TaskDto> getAllTasks(int page, int size, String keyword);

    // Get single task by id
    TaskDto getTaskById(Long id);

    // Update task
    TaskDto updateTask(Long id, TaskDto dto);

    // Delete task
    void deleteTask(Long id);

    // Assign task to member
    TaskDto assignTask(Long taskId, String memberEmail);

    // ================= DASHBOARD =================
    // Get overall stats
    // Returns map with totalUsers, totalProjects, totalTasks, completedTasks
    Object getDashboardStats();
}