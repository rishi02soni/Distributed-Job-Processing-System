import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../services/api';
import type { JobStatus, JobType, JobPriority, CreateJobInput } from '../types/job';

export function useJobs(params?: {
  status?: JobStatus;
  type?: JobType;
  priority?: JobPriority;
  search?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
  pollInterval?: number;
}) {
  const query = useQuery({
    queryKey: ['jobs', params],
    queryFn: () => api.getJobs(params),
    refetchInterval: params?.pollInterval ?? 4000,
  });

  return query;
}

export function useCreateJob() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: CreateJobInput) => api.createJob(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
      queryClient.invalidateQueries({ queryKey: ['job-stats'] });
      queryClient.invalidateQueries({ queryKey: ['queue-stats'] });
    },
  });
}
