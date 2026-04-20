package com.doe.manager.workflow;

import com.doe.manager.persistence.entity.JobEntity;
import com.doe.manager.persistence.entity.WorkflowEntity;
import com.doe.manager.persistence.entity.XComEntity;
import com.doe.manager.persistence.repository.JobRepository;
import com.doe.manager.persistence.repository.WorkflowRepository;
import com.doe.manager.persistence.repository.XComRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class XComServiceTest {

    private XComRepository xComRepository;
    private WorkflowRepository workflowRepository;
    private JobRepository jobRepository;
    private XComService service;

    @BeforeEach
    void setUp() {
        xComRepository = mock(XComRepository.class);
        workflowRepository = mock(WorkflowRepository.class);
        jobRepository = mock(JobRepository.class);
        service = new XComService(xComRepository, workflowRepository, jobRepository);
    }

    @Test
    void testPushAndPullFromCache() {
        UUID workflowId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        
        WorkflowEntity workflow = mock(WorkflowEntity.class);
        JobEntity job = mock(JobEntity.class);
        
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        
        service.push(workflowId, jobId, "key1", "val1", "message");
        
        // Verify repository interaction
        ArgumentCaptor<XComEntity> entityCaptor = ArgumentCaptor.forClass(XComEntity.class);
        verify(xComRepository).save(entityCaptor.capture());
        XComEntity saved = entityCaptor.getValue();
        assertEquals("key1", saved.getKey());
        assertEquals("val1", saved.getValue());

        // Pull should come from cache (no repository interaction)
        reset(xComRepository);
        Optional<String> result = service.pull(workflowId, "key1");
        assertTrue(result.isPresent());
        assertEquals("val1", result.get());
        verify(xComRepository, never()).findFirstByWorkflowIdAndKeyOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void testPullFromRepositoryIfMissingInCache() {
        UUID workflowId = UUID.randomUUID();
        String key = "missing_in_cache";
        
        XComEntity entity = mock(XComEntity.class);
        when(entity.getValue()).thenReturn("db_val");
        when(xComRepository.findFirstByWorkflowIdAndKeyOrderByCreatedAtDesc(workflowId, key))
                .thenReturn(Optional.of(entity));

        Optional<String> result = service.pull(workflowId, key);
        
        assertTrue(result.isPresent());
        assertEquals("db_val", result.get());
        verify(xComRepository).findFirstByWorkflowIdAndKeyOrderByCreatedAtDesc(workflowId, key);
        
        // Subsequent pull should now be in cache
        reset(xComRepository);
        result = service.pull(workflowId, key);
        assertTrue(result.isPresent());
        assertEquals("db_val", result.get());
        verify(xComRepository, never()).findFirstByWorkflowIdAndKeyOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void testPullReturnsEmptyIfNotFound() {
        UUID workflowId = UUID.randomUUID();
        when(xComRepository.findFirstByWorkflowIdAndKeyOrderByCreatedAtDesc(workflowId, "none"))
                .thenReturn(Optional.empty());

        assertTrue(service.pull(workflowId, "none").isEmpty());
    }
}
