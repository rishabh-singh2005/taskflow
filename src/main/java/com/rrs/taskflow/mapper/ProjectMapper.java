package com.rrs.taskflow.mapper;

import com.rrs.taskflow.dtos.ProjectDto;
import com.rrs.taskflow.entities.ProjectEntity;

public class ProjectMapper {

    // ================= ProjectDto → ProjectEntity =================
    public static ProjectEntity toEntity(ProjectDto dto) {
        ProjectEntity project = new ProjectEntity();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setProjectStatus(dto.getProjectStatus());
        return project;
        // Note: createdBy is set separately in service layer
    }

    // ================= ProjectEntity → ProjectDto =================
    public static ProjectDto toDto(ProjectEntity project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setProjectStatus(project.getProjectStatus());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        // Safely map createdBy email
        if (project.getCreatedBy() != null) {
            dto.setCreatedByEmail(project.getCreatedBy().getEmail());
        }

        return dto;
    }
}