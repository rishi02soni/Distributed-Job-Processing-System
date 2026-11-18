import { useQuery } from '@tanstack/react-query';
import { api } from '../services/api';

export function useJobStats(pollInterval: number = 3000) {
  return useQuery({
    queryKey: ['job-stats'],
    queryFn: () => api.getJobStats(),
    refetchInterval: pollInterval,
  });
}
