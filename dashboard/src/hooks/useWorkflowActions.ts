import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  executeWorkflow,
  pauseWorkflow,
  resumeWorkflow,
  resetWorkflow,
  deleteWorkflow,
} from '../api/workflows';

export const useWorkflowActions = (workflowId: string) => {
  const queryClient = useQueryClient();

  const invalidateQueries = () => {
    queryClient.invalidateQueries({ queryKey: ['workflows'] });
    queryClient.invalidateQueries({ queryKey: ['workflowDag', workflowId] });
    queryClient.invalidateQueries({ queryKey: ['workflow', workflowId] });
  };

  const executeMutation = useMutation({
    mutationFn: () => executeWorkflow(workflowId),
    onSuccess: invalidateQueries,
  });

  const pauseMutation = useMutation({
    mutationFn: () => pauseWorkflow(workflowId),
    onSuccess: invalidateQueries,
  });

  const resumeMutation = useMutation({
    mutationFn: () => resumeWorkflow(workflowId),
    onSuccess: invalidateQueries,
  });

  const resetMutation = useMutation({
    mutationFn: () => resetWorkflow(workflowId),
    onSuccess: invalidateQueries,
  });

  const removeMutation = useMutation({
    mutationFn: () => deleteWorkflow(workflowId),
    onSuccess: invalidateQueries,
  });

  return {
    execute: executeMutation.mutateAsync,
    pause: pauseMutation.mutateAsync,
    resume: resumeMutation.mutateAsync,
    reset: resetMutation.mutateAsync,
    remove: removeMutation.mutateAsync,
    
    isExecuting: executeMutation.isPending,
    isPausing: pauseMutation.isPending,
    isResuming: resumeMutation.isPending,
    isResetting: resetMutation.isPending,
    isRemoving: removeMutation.isPending,
    
    executeError: executeMutation.error,
    pauseError: pauseMutation.error,
    resumeError: resumeMutation.error,
    resetError: resetMutation.error,
    removeError: removeMutation.error,
  };
};