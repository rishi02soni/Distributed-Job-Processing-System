import { useQuery } from '@tanstack/react-query';
import { api } from '../services/api';

export function useWorkers(pollInterval: number = 3000) {
  return useQuery({
    queryKey: ['workers'],
    queryFn: () => api.getWorkers(),
    refetchInterval: pollInterval,
  });
}
