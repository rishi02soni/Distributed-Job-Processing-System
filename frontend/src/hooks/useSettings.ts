import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../services/api';
import type { SystemSettings } from '../types/settings';

export function useSettings() {
  const queryClient = useQueryClient();

  const settingsQuery = useQuery({
    queryKey: ['settings'],
    queryFn: () => api.getSettings(),
  });

  const mutation = useMutation({
    mutationFn: (newSettings: SystemSettings) => api.updateSettings(newSettings),
    onSuccess: (updated) => {
      queryClient.setQueryData(['settings'], updated);
      queryClient.invalidateQueries({ queryKey: ['settings'] });
    },
  });

  return {
    settings: settingsQuery.data,
    isLoading: settingsQuery.isLoading,
    isError: settingsQuery.isError,
    updateSettings: mutation.mutateAsync,
    isUpdating: mutation.isPending,
  };
}
