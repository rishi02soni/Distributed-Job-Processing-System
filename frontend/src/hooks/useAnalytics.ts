import { useQuery } from '@tanstack/react-query';
import { api } from '../services/api';

export function useAnalytics(pollInterval: number = 5000) {
  return useQuery({
    queryKey: ['analytics'],
    queryFn: () => api.getAnalytics(),
    refetchInterval: pollInterval,
  });
}
