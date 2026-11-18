import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../services/api';

export function useJobDetails(id: string) {
  const queryClient = useQueryClient();

  const jobQuery = useQuery({
    queryKey: ['job', id],
    queryFn: () => api.getJob(id),
    enabled: !!id,
    refetchInterval: (query) => {
      const data = query.state.data;
      if (data && (data.status === 'COMPLETED' || data.status === 'FAILED' || data.status === 'CANCELLED')) {
        return false;
      }
      return 2000;
    },
  });

  const logsQuery = useQuery({
    queryKey: ['job-logs', id],
    queryFn: () => api.getJobLogs(id),
    enabled: !!id,
    refetchInterval: (query) => {
      const jobData = jobQuery.data;
      if (jobData && (jobData.status === 'COMPLETED' || jobData.status === 'FAILED' || jobData.status === 'CANCELLED')) {
        return false;
      }
      return 2000;
    },
  });

  const retryMutation = useMutation({
    mutationFn: () => api.retryJob(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['job', id] });
      queryClient.invalidateQueries({ queryKey: ['job-logs', id] });
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
      queryClient.invalidateQueries({ queryKey: ['job-stats'] });
    },
  });

  const cancelMutation = useMutation({
    mutationFn: () => api.cancelJob(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['job', id] });
      queryClient.invalidateQueries({ queryKey: ['job-logs', id] });
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
      queryClient.invalidateQueries({ queryKey: ['job-stats'] });
    },
  });

  return {
    job: jobQuery.data,
    isLoading: jobQuery.isLoading,
    isError: jobQuery.isError,
    error: jobQuery.error,
    logs: logsQuery.data ?? [],
    isLogsLoading: logsQuery.isLoading,
    retry: retryMutation.mutateAsync,
    isRetrying: retryMutation.isPending,
    cancel: cancelMutation.mutateAsync,
    isCancelling: cancelMutation.isPending,
    refetch: () => {
      jobQuery.refetch();
      logsQuery.refetch();
    },
  };
}
