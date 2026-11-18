import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Search,
  Filter,
  RefreshCw,
  Plus,
  RotateCcw,
  Ban,
  ArrowUpDown,
  ChevronLeft,
  ChevronRight,
  ExternalLink
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { StatusBadge, PriorityBadge } from '../components/ui/Badge';
import { useJobs } from '../hooks/useJobs';
import { api } from '../services/api';
import { formatDuration, formatDate } from '../lib/utils';
import type { JobStatus, JobType, JobPriority } from '../types/job';

export const JobsManagement: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // Filters state from query parameters or default
  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [status, setStatus] = useState<JobStatus | ''>((searchParams.get('status') as JobStatus) || '');
  const [type, setType] = useState<JobType | ''>((searchParams.get('type') as JobType) || '');
  const [priority, setPriority] = useState<JobPriority | ''>((searchParams.get('priority') as JobPriority) || '');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState<'ASC' | 'DESC'>('DESC');
  const [actionFeedback, setActionFeedback] = useState<string | null>(null);

  const { data, isLoading, refetch } = useJobs({
    search: search.trim() ? search.trim() : undefined,
    status: status ? status : undefined,
    type: type ? type : undefined,
    priority: priority ? priority : undefined,
    page,
    size,
    sortBy,
    sortDir,
  });

  const handleRetry = async (jobId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await api.retryJob(jobId);
      setActionFeedback(`Job ${jobId} successfully re-queued for retry.`);
      setTimeout(() => setActionFeedback(null), 3500);
      refetch();
    } catch (err: any) {
      alert(`Retry failed: ${err.message}`);
    }
  };

  const handleCancel = async (jobId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!confirm(`Are you sure you want to cancel job ${jobId}?`)) return;
    try {
      await api.cancelJob(jobId);
      setActionFeedback(`Job ${jobId} cancelled.`);
      setTimeout(() => setActionFeedback(null), 3500);
      refetch();
    } catch (err: any) {
      alert(`Cancellation failed: ${err.message}`);
    }
  };

  const toggleSort = (field: string) => {
    if (sortBy === field) {
      setSortDir(sortDir === 'ASC' ? 'DESC' : 'ASC');
    } else {
      setSortBy(field);
      setSortDir('DESC');
    }
  };

  const statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'QUEUED', label: 'QUEUED' },
    { value: 'RUNNING', label: 'RUNNING' },
    { value: 'COMPLETED', label: 'COMPLETED' },
    { value: 'FAILED', label: 'FAILED' },
    { value: 'RETRYING', label: 'RETRYING' },
    { value: 'CANCELLED', label: 'CANCELLED' },
  ];

  const typeOptions = [
    { value: '', label: 'All Types' },
    { value: 'EMAIL', label: 'EMAIL' },
    { value: 'REPORT', label: 'REPORT' },
    { value: 'DATA_PROCESSING', label: 'DATA_PROCESSING' },
    { value: 'CUSTOM', label: 'CUSTOM' },
  ];

  const priorityOptions = [
    { value: '', label: 'All Priorities' },
    { value: 'LOW', label: 'LOW' },
    { value: 'NORMAL', label: 'NORMAL' },
    { value: 'HIGH', label: 'HIGH' },
    { value: 'CRITICAL', label: 'CRITICAL' },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white">Jobs Management</h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Search, filter, inspect, and trigger manual retry or cancellation on distributed jobs
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm" onClick={() => refetch()}>
            <RefreshCw className="w-3.5 h-3.5 mr-1" />
            <span>Refresh</span>
          </Button>
          <Button size="sm" onClick={() => navigate('/jobs/new')}>
            <Plus className="w-3.5 h-3.5 mr-1" />
            <span>Create Job</span>
          </Button>
        </div>
      </div>

      {actionFeedback && (
        <div className="p-3 bg-blue-950/60 border border-blue-800 rounded-lg text-xs text-blue-300 font-mono flex items-center justify-between">
          <span>{actionFeedback}</span>
          <button onClick={() => setActionFeedback(null)} className="text-slate-400 hover:text-white cursor-pointer">×</button>
        </div>
      )}

      {/* Filter Toolbar */}
      <Card className="p-4 bg-[#111827]/70">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
          <div className="lg:col-span-2">
            <Input
              placeholder="Search by Job ID or Name..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
            />
          </div>
          <Select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value as JobStatus);
              setPage(0);
            }}
            options={statusOptions}
          />
          <Select
            value={type}
            onChange={(e) => {
              setType(e.target.value as JobType);
              setPage(0);
            }}
            options={typeOptions}
          />
          <Select
            value={priority}
            onChange={(e) => {
              setPriority(e.target.value as JobPriority);
              setPage(0);
            }}
            options={priorityOptions}
          />
        </div>
      </Card>

      {/* Table */}
      <Card className="p-0 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#0f172a] border-b border-slate-800 text-slate-400 font-mono">
              <tr>
                <th className="py-3 px-4">Job ID</th>
                <th className="py-3 px-4">Name</th>
                <th className="py-3 px-4">Type</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Priority</th>
                <th
                  className="py-3 px-4 cursor-pointer hover:text-slate-200 select-none"
                  onClick={() => toggleSort('durationMs')}
                >
                  <div className="flex items-center gap-1">
                    <span>Duration</span>
                    <ArrowUpDown className="w-3 h-3 text-slate-500" />
                  </div>
                </th>
                <th className="py-3 px-4">Attempts</th>
                <th className="py-3 px-4">Worker ID</th>
                <th
                  className="py-3 px-4 cursor-pointer hover:text-slate-200 select-none"
                  onClick={() => toggleSort('createdAt')}
                >
                  <div className="flex items-center gap-1">
                    <span>Created</span>
                    <ArrowUpDown className="w-3 h-3 text-slate-500" />
                  </div>
                </th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {isLoading ? (
                <tr>
                  <td colSpan={10} className="py-12 text-center text-slate-400 font-sans">
                    Loading jobs from cluster...
                  </td>
                </tr>
              ) : data?.content && data.content.length > 0 ? (
                data.content.map((job) => (
                  <tr
                    key={job.id}
                    onClick={() => navigate(`/jobs/${job.id}`)}
                    className="hover:bg-slate-900/60 transition-colors cursor-pointer group"
                  >
                    <td className="py-3.5 px-4 font-semibold text-blue-400 group-hover:underline">
                      {job.id}
                    </td>
                    <td className="py-3.5 px-4 text-slate-200 font-sans max-w-[200px] truncate">
                      {job.name}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">{job.type}</td>
                    <td className="py-3.5 px-4">
                      <StatusBadge status={job.status} />
                    </td>
                    <td className="py-3.5 px-4">
                      <PriorityBadge priority={job.priority} />
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">{formatDuration(job.durationMs)}</td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {job.attemptCount}/{job.maxRetries}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400 max-w-[120px] truncate">
                      {job.workerId || '—'}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">{formatDate(job.createdAt)}</td>
                    <td className="py-3.5 px-4 text-right" onClick={(e) => e.stopPropagation()}>
                      <div className="flex items-center justify-end gap-1.5">
                        {(job.status === 'FAILED' || job.status === 'CANCELLED') && (
                          <Button
                            variant="secondary"
                            size="sm"
                            onClick={(e) => handleRetry(job.id, e)}
                            title="Retry Job"
                          >
                            <RotateCcw className="w-3.5 h-3.5 mr-1" />
                            Retry
                          </Button>
                        )}
                        {(job.status === 'QUEUED' || job.status === 'RUNNING' || job.status === 'RETRYING') && (
                          <Button
                            variant="danger"
                            size="sm"
                            onClick={(e) => handleCancel(job.id, e)}
                            title="Cancel Job"
                          >
                            <Ban className="w-3.5 h-3.5 mr-1" />
                            Cancel
                          </Button>
                        )}
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => navigate(`/jobs/${job.id}`)}
                        >
                          <ExternalLink className="w-3.5 h-3.5" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={10} className="py-12 text-center text-slate-500 font-sans">
                    No matching jobs found. Try adjusting your search query or filters.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        {data && data.totalPages > 0 && (
          <div className="p-4 border-t border-slate-800 bg-[#0f172a]/60 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs font-mono text-slate-400">
            <div>
              Showing {data.content.length} of {data.totalElements} jobs (Page {data.page + 1} of {data.totalPages})
            </div>
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-1.5">
                <span>Rows:</span>
                <select
                  value={size}
                  onChange={(e) => {
                    setSize(Number(e.target.value));
                    setPage(0);
                  }}
                  className="bg-slate-900 border border-slate-700 rounded px-2 py-1 text-slate-200"
                >
                  <option value={10}>10</option>
                  <option value={25}>25</option>
                  <option value={50}>50</option>
                </select>
              </div>
              <div className="flex items-center gap-1">
                <Button
                  variant="secondary"
                  size="sm"
                  disabled={page === 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                >
                  <ChevronLeft className="w-4 h-4" />
                </Button>
                <Button
                  variant="secondary"
                  size="sm"
                  disabled={data.last}
                  onClick={() => setPage((p) => p + 1)}
                >
                  <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
};
