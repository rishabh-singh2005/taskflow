package com.rrs.taskflow.services.impl;

import com.rrs.taskflow.dtos.ProjectDto;
import com.rrs.taskflow.dtos.RegisterRequestDto;
import com.rrs.taskflow.dtos.TaskDto;
import com.rrs.taskflow.entities.ProjectEntity;
import com.rrs.taskflow.entities.TaskEntity;
import com.rrs.taskflow.entities.UserEntity;
import com.rrs.taskflow.enums.Priority;
import com.rrs.taskflow.enums.TaskStatus;
import com.rrs.taskflow.exceptions.ResourceNotFoundException;
import com.rrs.taskflow.file.FileStorageService;
import com.rrs.taskflow.mapper.ProjectMapper;
import com.rrs.taskflow.mapper.TaskMapper;
import com.rrs.taskflow.mapper.UserMapper;
import com.rrs.taskflow.repositories.ProjectRepository;
import com.rrs.taskflow.repositories.TaskRepository;
import com.rrs.taskflow.repositories.UserRepository;
import com.rrs.taskflow.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================================
    // PROFILE
    // ==========================================================

    // get own profile by email
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#email")
    public RegisterRequestDto getProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserMapper.toDto(user);
    }

    // update own name and password — clears cache after update
    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#email")
    public RegisterRequestDto updateProfile(String email, RegisterRequestDto dto) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // update name if provided
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            user.setName(dto.getName());
        }

        // update password if provided — encode before saving
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        UserEntity updatedUser = userRepository.save(user);
        return UserMapper.toDto(updatedUser);
    }

    // update profile image — deletes old image and saves new one
    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#email")
    public void updateProfileImage(String email, MultipartFile image) throws Exception {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // delete old image from storage if exists
        if (user.getProfileImage() != null) {
            fileStorageService.deleteProfileImage(user.getProfileImage());
        }

        // save new image and update path in db
        String newImagePath = fileStorageService.saveProfileImage(image);
        user.setProfileImage(newImagePath);

        userRepository.save(user);
    }

    // ==========================================================
    // MY TASKS
    // ==========================================================

    // get all tasks assigned to logged in member with pagination
    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> getMyTasks(String email, int page, int size) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByAssignedTo(user, pageable)
                .map(TaskMapper::toDto);
    }

    // get single task by id — only if task belongs to logged in member
    @Override
    @Transactional(readOnly = true)
    public TaskDto getMyTaskById(String email, Long taskId) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // make sure this task actually belongs to the logged in member
        if (task.getAssignedTo() == null || !task.getAssignedTo().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Task not assigned to you");
        }

        return TaskMapper.toDto(task);
    }

    // update task status — member can only change status of their own task
    @Override
    @Transactional
    @CachePut(value = "tasks", key = "#taskId")
    public TaskDto updateTaskStatus(String email, Long taskId, String status) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // make sure task belongs to logged in member
        if (task.getAssignedTo() == null || !task.getAssignedTo().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Task not assigned to you");
        }

        // convert string status to enum
        task.setStatus(TaskStatus.valueOf(status.toUpperCase()));

        TaskEntity updatedTask = taskRepository.save(task);
        return TaskMapper.toDto(updatedTask);
    }

    // filter my tasks by status and priority
    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> filterMyTasks(String email, String status, String priority, int page, int size) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Pageable pageable = PageRequest.of(page, size);

        boolean hasStatus = status != null && !status.isEmpty();
        boolean hasPriority = priority != null && !priority.isEmpty();

        // filter by both status and priority
        if (hasStatus && hasPriority) {
            return taskRepository.findByAssignedToAndStatusAndPriority(
                    user,
                    TaskStatus.valueOf(status.toUpperCase()),
                    Priority.valueOf(priority.toUpperCase()),
                    pageable
            ).map(TaskMapper::toDto);
        }

        // filter by status only
        if (hasStatus) {
            return taskRepository.findByAssignedToAndStatus(
                    user,
                    TaskStatus.valueOf(status.toUpperCase()),
                    pageable
            ).map(TaskMapper::toDto);
        }

        // filter by priority only
        if (hasPriority) {
            return taskRepository.findByAssignedToAndPriority(
                    user,
                    Priority.valueOf(priority.toUpperCase()),
                    pageable
            ).map(TaskMapper::toDto);
        }

        // no filter — return all my tasks
        return taskRepository.findByAssignedTo(user, pageable)
                .map(TaskMapper::toDto);
    }

    // ==========================================================
    // MY PROJECTS
    // ==========================================================

    // get all projects where logged in member is creator
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> getMyProjects(String email, int page, int size) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Pageable pageable = PageRequest.of(page, size);
        return projectRepository.findByCreatedBy(user, pageable)
                .map(ProjectMapper::toDto);
    }

    // get single project by id — only if member is creator
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectDto getMyProjectById(String email, Long projectId) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // make sure project belongs to logged in member
        if (!project.getCreatedBy().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Project not found for this user");
        }

        return ProjectMapper.toDto(project);
    }

    // ==========================================================
    // DASHBOARD
    // ==========================================================

    // returns logged in member's task stats
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard", key = "#email")
    public Object getMyDashboard(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Map<String, Long> stats = new HashMap<>();

        // total tasks assigned to me
        stats.put("totalTasks", taskRepository.countByAssignedTo(user));

        // my tasks by status
        stats.put("completedTasks", taskRepository.countByAssignedToAndStatus(user, TaskStatus.DONE));
        stats.put("inProgressTasks", taskRepository.countByAssignedToAndStatus(user, TaskStatus.IN_PROGRESS));
        stats.put("pendingTasks", taskRepository.countByAssignedToAndStatus(user, TaskStatus.TODO));

        // total projects i am part of
        stats.put("totalProjects", projectRepository.countByCreatedBy(user));

        return stats;
    }
}