package com.rrs.taskflow.services;

import com.rrs.taskflow.dtos.ProjectDto;
import com.rrs.taskflow.dtos.TaskDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.rrs.taskflow.dtos.RegisterRequestDto;

public interface MemberService {

    // ================= PROFILE =================
    // Get own profile
    RegisterRequestDto getProfile(String email);

    // Update own name and password
    RegisterRequestDto updateProfile(String email, RegisterRequestDto dto);

    // Update profile image
    void updateProfileImage(String email, MultipartFile image) throws Exception;

    // ================= MY TASKS =================
    // Get all tasks assigned to me with pagination
    Page<TaskDto> getMyTasks(String email, int page, int size);

    // Get single task assigned to me
    TaskDto getMyTaskById(String email, Long taskId);

    // Update task status
    TaskDto updateTaskStatus(String email, Long taskId, String status);

    // Filter my tasks by status, priority
    Page<TaskDto> filterMyTasks(String email, String status, String priority, int page, int size);

    // ================= PROJECTS =================
    // Get all projects I am part of
    Page<ProjectDto> getMyProjects(String email, int page, int size);

    // Get single project
    ProjectDto getMyProjectById(String email, Long projectId);

    // ================= DASHBOARD =================
    // Get my task stats
    Object getMyDashboard(String email);
}