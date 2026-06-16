package com.rrs.taskflow.controllers;

import com.rrs.taskflow.dtos.ProjectDto;
import com.rrs.taskflow.dtos.RegisterRequestDto;
import com.rrs.taskflow.dtos.TaskDto;
import com.rrs.taskflow.response.ApiResponse;
import com.rrs.taskflow.services.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    // ==========================================================
    // PROFILE
    // ==========================================================

    // get own profile details
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<RegisterRequestDto>> getProfile(Principal principal) {
        RegisterRequestDto profile = memberService.getProfile(principal.getName());
        ApiResponse<RegisterRequestDto> response = new ApiResponse<>("Profile fetched successfully", profile);
        return ResponseEntity.ok(response);
    }

    // update own name or password
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<RegisterRequestDto>> updateProfile(
            @Valid @RequestBody RegisterRequestDto dto,
            Principal principal
    ) {
        RegisterRequestDto updated = memberService.updateProfile(principal.getName(), dto);
        ApiResponse<RegisterRequestDto> response = new ApiResponse<>("Profile updated successfully", updated);
        return ResponseEntity.ok(response);
    }

    // upload or change profile image
    @PatchMapping(value = "/profile/image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> updateProfileImage(
            @RequestPart("profileImage") MultipartFile image,
            Principal principal
    ) throws Exception {
        memberService.updateProfileImage(principal.getName(), image);
        ApiResponse<String> response = new ApiResponse<>("Profile image updated successfully", null);
        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // MY TASKS
    // ==========================================================

    // get all tasks assigned to me with pagination
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<Page<TaskDto>>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        Page<TaskDto> tasks = memberService.getMyTasks(principal.getName(), page, size);
        ApiResponse<Page<TaskDto>> response = new ApiResponse<>("Tasks fetched successfully", tasks);
        return ResponseEntity.ok(response);
    }

    // get single task assigned to me by id
    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<TaskDto>> getMyTaskById(
            @PathVariable Long id,
            Principal principal
    ) {
        TaskDto task = memberService.getMyTaskById(principal.getName(), id);
        ApiResponse<TaskDto> response = new ApiResponse<>("Task fetched successfully", task);
        return ResponseEntity.ok(response);
    }

    // update status of my task — todo, in_progress, done
    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<ApiResponse<TaskDto>> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Principal principal
    ) {
        TaskDto task = memberService.updateTaskStatus(principal.getName(), id, status);
        ApiResponse<TaskDto> response = new ApiResponse<>("Task status updated successfully", task);
        return ResponseEntity.ok(response);
    }

    // filter my tasks by status and priority
    @GetMapping("/tasks/filter")
    public ResponseEntity<ApiResponse<Page<TaskDto>>> filterMyTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        Page<TaskDto> tasks = memberService.filterMyTasks(principal.getName(), status, priority, page, size);
        ApiResponse<Page<TaskDto>> response = new ApiResponse<>("Tasks filtered successfully", tasks);
        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // MY PROJECTS
    // ==========================================================

    // get all projects i am part of with pagination
    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<Page<ProjectDto>>> getMyProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        Page<ProjectDto> projects = memberService.getMyProjects(principal.getName(), page, size);
        ApiResponse<Page<ProjectDto>> response = new ApiResponse<>("Projects fetched successfully", projects);
        return ResponseEntity.ok(response);
    }

    // get single project by id
    @GetMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> getMyProjectById(
            @PathVariable Long id,
            Principal principal
    ) {
        ProjectDto project = memberService.getMyProjectById(principal.getName(), id);
        ApiResponse<ProjectDto> response = new ApiResponse<>("Project fetched successfully", project);
        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // DASHBOARD
    // ==========================================================

    // get my task stats — total, completed, pending, in progress
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Object>> getMyDashboard(Principal principal) {
        Object stats = memberService.getMyDashboard(principal.getName());
        ApiResponse<Object> response = new ApiResponse<>("Dashboard fetched successfully", stats);
        return ResponseEntity.ok(response);
    }
}