package com.rrs.taskflow.repositories;

import com.rrs.taskflow.entities.ProjectEntity;
import com.rrs.taskflow.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    Page<ProjectEntity> findByCreatedBy(UserEntity createdBy, Pageable pageable);

    @Query("SELECT p FROM ProjectEntity p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<ProjectEntity> searchProjects(@Param("keyword") String keyword, Pageable pageable);

    long countByCreatedBy(UserEntity createdBy);
}