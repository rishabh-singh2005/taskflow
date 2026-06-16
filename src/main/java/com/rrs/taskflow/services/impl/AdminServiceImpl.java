package com.rrs.taskflow.services.impl;

import com.rrs.taskflow.dtos.ProjectDto;
import com.rrs.taskflow.dtos.RegisterRequestDto;
import com.rrs.taskflow.dtos.TaskDto;
import com.rrs.taskflow.entities.ProjectEntity;
import com.rrs.taskflow.entities.TaskEntity;
import com.rrs.taskflow.entities.UserEntity;
import com.rrs.taskflow.enums.Priority;
import com.rrs.taskflow.enums.Role;
import com.rrs.taskflow.enums.TaskStatus;
import com.rrs.taskflow.exceptions.ResourceNotFoundException;
import com.rrs.taskflow.mapper.ProjectMapper;
import com.rrs.taskflow.mapper.TaskMapper;
import com.rrs.taskflow.mapper.UserMapper;
import com.rrs.taskflow.repositories.ProjectRepository;
import com.rrs.taskflow.repositories.TaskRepository;
import com.rrs.taskflow.repositories.UserRepository;
import com.rrs.taskflow.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    // ==========================================================
    // USER MANAGEMENT
    // ==========================================================

    // get all users with pagination
    @Override
    @Transactional(readOnly = true)
    public Page<RegisterRequestDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserEntity> users = userRepository.findAll(pageable);
        return users.map(UserMapper::toDto);
    }

    // get single user by id — throws error if not found
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public RegisterRequestDto getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserMapper.toDto(user);
    }

    // delete user by id — also clears from cache
    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    // update role of user — admin or member
    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void updateUserRole(Long id, Role role) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setRole(role);
        userRepository.save(user);
    }

    // ==========================================================
    // PROJECT MANAGEMENT
    // ==========================================================

    // create new project — sets logged in admin as creator
    @Override
    @Transactional
    public ProjectDto createProject(ProjectDto dto, String adminEmail) {
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));

        // convert dto to entity
        ProjectEntity project = ProjectMapper.toEntity(dto);

        // set admin as project creator
        project.setCreatedBy(admin);

        ProjectEntity savedProject = projectRepository.save(project);
        return ProjectMapper.toDto(savedProject);
    }

    // get all projects — supports keyword search and pagination
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> getAllProjects(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        // if keyword present — search by name or description
        if (keyword != null && !keyword.isEmpty()) {
            return projectRepository.searchProjects(keyword, pageable)
                    .map(ProjectMapper::toDto);
        }

        return projectRepository.findAll(pageable)
                .map(ProjectMapper::toDto);
    }

    // get single project by id
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#id")
    public ProjectDto getProjectById(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return ProjectMapper.toDto(project);
    }

    // update project details — refreshes cache after update
    @Override
    @Transactional
    @CachePut(value = "projects", key = "#id")
    public ProjectDto updateProject(Long id, ProjectDto dto) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setProjectStatus(dto.getProjectStatus());

        ProjectEntity updatedProject = projectRepository.save(project);
        return ProjectMapper.toDto(updatedProject);
    }

    // delete project by id — removes from cache too
    @Override
    @Transactional
    @CacheEvict(value = "projects", key = "#id")
    public void deleteProject(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
    }

    // ==========================================================
    // TASK MANAGEMENT
    // ==========================================================

    // create task — links it to project and sets creator
    @Override
    @Transactional
    public TaskDto createTask(TaskDto dto, String adminEmail) {
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));

        ProjectEntity project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + dto.getProjectId()));

        // convert dto to entity
        TaskEntity task = TaskMapper.toEntity(dto);

        // set who created the task
        task.setCreatedBy(admin);

        // link task to project
        task.setProject(project);

        // assign to member if email provided
        if (dto.getAssignedToEmail() != null && !dto.getAssignedToEmail().isEmpty()) {
            UserEntity member = userRepository.findByEmail(dto.getAssignedToEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found with email: " + dto.getAssignedToEmail()));
            task.setAssignedTo(member);
        }

        TaskEntity savedTask = taskRepository.save(task);
        return TaskMapper.toDto(savedTask);
    }

    // get all tasks — supports keyword search and pagination
    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> getAllTasks(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        // if keyword present — search by title or description
        if (keyword != null && !keyword.isEmpty()) {
            return taskRepository.searchTasks(keyword, pageable)
                    .map(TaskMapper::toDto);
        }

        return taskRepository.findAll(pageable)
                .map(TaskMapper::toDto);
    }

    // get single task by id
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#id")
    public TaskDto getTaskById(Long id) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return TaskMapper.toDto(task);
    }

    // update task details — refreshes cache after update
    @Override
    @Transactional
    @CachePut(value = "tasks", key = "#id")
    public TaskDto updateTask(Long id, TaskDto dto) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setDueDate(dto.getDueDate());

        TaskEntity updatedTask = taskRepository.save(task);
        return TaskMapper.toDto(updatedTask);
    }

    // delete task by id — removes from cache too
    @Override
    @Transactional
    @CacheEvict(value = "tasks", key = "#id")
    public void deleteTask(Long id) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        taskRepository.delete(task);
    }

    // assign or reassign task to a member
    @Override
    @Transactional
    @CachePut(value = "tasks", key = "#taskId")
    public TaskDto assignTask(Long taskId, String memberEmail) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        UserEntity member = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with email: " + memberEmail));

        task.setAssignedTo(member);

        TaskEntity updatedTask = taskRepository.save(task);
        return TaskMapper.toDto(updatedTask);
    }

    // ==========================================================
    // DASHBOARD
    // ==========================================================

    // returns overall stats — total users, projects, tasks, completed tasks
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard", key = "'admin-stats'")
    public Object getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalProjects", projectRepository.count());
        stats.put("totalTasks", taskRepository.count());
        stats.put("completedTasks", taskRepository.countByStatus(TaskStatus.DONE));
        stats.put("inProgressTasks", taskRepository.countByStatus(TaskStatus.IN_PROGRESS));
        stats.put("pendingTasks", taskRepository.countByStatus(TaskStatus.TODO));

        return stats;
    }
}