package com.rrs.taskflow.mapper;

import com.rrs.taskflow.dtos.TaskDto;
import com.rrs.taskflow.entities.TaskEntity;

public class TaskMapper {

    // ================= TaskDto → TaskEntity =================
    public static TaskEntity toEntity(TaskDto dto) {
        TaskEntity task = new TaskEntity();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setDueDate(dto.getDueDate());
        return task;
        // Note: project, assignedTo, createdBy set separately in service layer
    }

    // ================= TaskEntity → TaskDto =================
    public static TaskDto toDto(TaskEntity task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setAttachmentUrl(task.getAttachmentUrl());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        // Safely map project id
        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
        }

        // Safely map assignedTo email
        if (task.getAssignedTo() != null) {
            dto.setAssignedToEmail(task.getAssignedTo().getEmail());
        }

        // Safely map createdBy email
        if (task.getCreatedBy() != null) {
            dto.setCreatedByEmail(task.getCreatedBy().getEmail());
        }

        return dto;
    }
}