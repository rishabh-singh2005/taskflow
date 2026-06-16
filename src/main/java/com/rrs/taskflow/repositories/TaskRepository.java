package com.rrs.taskflow.repositories;

import com.rrs.taskflow.entities.ProjectEntity;
import com.rrs.taskflow.entities.TaskEntity;
import com.rrs.taskflow.entities.UserEntity;
import com.rrs.taskflow.enums.Priority;
import com.rrs.taskflow.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    Page<TaskEntity> findByAssignedTo(UserEntity assignedTo, Pageable pageable);

    Page<TaskEntity> findByProject(ProjectEntity project, Pageable pageable);

    Page<TaskEntity> findByAssignedToAndStatus(UserEntity assignedTo, TaskStatus status, Pageable pageable);

    Page<TaskEntity> findByAssignedToAndPriority(UserEntity assignedTo, Priority priority, Pageable pageable);

    Page<TaskEntity> findByAssignedToAndStatusAndPriority(UserEntity assignedTo, TaskStatus status, Priority priority, Pageable pageable);

    @Query("SELECT t FROM TaskEntity t WHERE t.title LIKE %:keyword% OR t.description LIKE %:keyword%")
    Page<TaskEntity> searchTasks(@Param("keyword") String keyword, Pageable pageable);

    long countByAssignedTo(UserEntity assignedTo);

    long countByAssignedToAndStatus(UserEntity assignedTo, TaskStatus status);

    long countByStatus(TaskStatus status);
}