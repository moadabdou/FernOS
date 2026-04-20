package com.doe.manager.workflow;

import com.doe.manager.persistence.entity.JobEntity;
import com.doe.manager.persistence.entity.WorkflowEntity;
import com.doe.manager.persistence.entity.XComEntity;
import com.doe.manager.persistence.repository.JobRepository;
import com.doe.manager.persistence.repository.WorkflowRepository;
import com.doe.manager.persistence.repository.XComRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing cross-job communication (XComs).
 * Implements a cache to avoid frequent DB lookups within the same workflow.
 */
@Service
public class XComService {

    private static final Logger LOG = LoggerFactory.getLogger(XComService.class);

    private final XComRepository xComRepository;
    private final WorkflowRepository workflowRepository;
    private final JobRepository jobRepository;

    /**
     * Cache structure: workflowId -> (xcomKey -> xcomValue)
     * For simplicity, this cache lives in-memory. In a distributed multi-manager setup, 
     * a distributed cache like Redis would be preferred.
     */
    private final Map<UUID, Map<String, String>> xcomCache = new ConcurrentHashMap<>();

    public XComService(XComRepository xComRepository, 
                      WorkflowRepository workflowRepository, 
                      JobRepository jobRepository) {
        this.xComRepository = xComRepository;
        this.workflowRepository = workflowRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional
    public void push(UUID workflowId, UUID jobId, String key, String value, String type) {
        LOG.info("Pushing XCom: workflow={}, job={}, key={}, type={}", workflowId, jobId, key, type);

        WorkflowEntity workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + workflowId));
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        XComEntity entity = new XComEntity(
                UUID.randomUUID(),
                workflow,
                job,
                key,
                value,
                type,
                Instant.now()
        );

        xComRepository.save(entity);

        // Update cache
        xcomCache.computeIfAbsent(workflowId, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    public Optional<String> pull(UUID workflowId, String key) {
        // Try cache first
        Map<String, String> workflowCache = xcomCache.get(workflowId);
        if (workflowCache != null && workflowCache.containsKey(key)) {
            LOG.debug("XCom cache hit: workflow={}, key={}", workflowId, key);
            return Optional.of(workflowCache.get(key));
        }

        // Try DB
        LOG.debug("XCom cache miss: workflow={}, key={}", workflowId, key);
        return xComRepository.findFirstByWorkflowIdAndKeyOrderByCreatedAtDesc(workflowId, key)
                .map(entity -> {
                    // Update cache for next time
                    xcomCache.computeIfAbsent(workflowId, k -> new ConcurrentHashMap<>()).put(key, entity.getValue());
                    return entity.getValue();
                });
    }

    /**
     * Clears cache for a specific workflow. Should be called when a workflow run finishes.
     */
    public void clearCache(UUID workflowId) {
        xcomCache.remove(workflowId);
    }
}
