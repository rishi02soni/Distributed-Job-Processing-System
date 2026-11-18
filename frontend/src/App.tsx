import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AppLayout } from './components/layout/AppLayout';
import { OverviewDashboard } from './pages/OverviewDashboard';
import { JobsManagement } from './pages/JobsManagement';
import { CreateJob } from './pages/CreateJob';
import { JobDetails } from './pages/JobDetails';
import { WorkersPage } from './pages/WorkersPage';
import { QueueMonitoring } from './pages/QueueMonitoring';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { SettingsPage } from './pages/SettingsPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 2000,
      retry: 1,
      refetchOnWindowFocus: true,
    },
  },
});

export const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<AppLayout />}>
            <Route index element={<OverviewDashboard />} />
            <Route path="jobs" element={<JobsManagement />} />
            <Route path="jobs/new" element={<CreateJob />} />
            <Route path="jobs/:id" element={<JobDetails />} />
            <Route path="workers" element={<WorkersPage />} />
            <Route path="queue" element={<QueueMonitoring />} />
            <Route path="analytics" element={<AnalyticsPage />} />
            <Route path="settings" element={<SettingsPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

export default App;
