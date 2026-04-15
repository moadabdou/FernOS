package com.doe.manager.persistence.repository;

import com.doe.core.model.WorkflowStatus;
import com.doe.manager.persistence.entity.WorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowRepository extends JpaRepository<WorkflowEntity, UUID> {
    List<WorkflowEntity> findByStatus(WorkflowStatus status);
    List<WorkflowEntity> findAllByOrderByCreatedAtDesc();
}
