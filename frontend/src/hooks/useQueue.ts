import { useQuery } from '@tanstack/react-query';
import { api } from '../services/api';

export function useQueueStats(pollInterval: number = 3000) {
  return useQuery({
    queryKey: ['queue-stats'],
    queryFn: () => api.getQueueStats(),
    refetchInterval: pollInterval,
  });
}
