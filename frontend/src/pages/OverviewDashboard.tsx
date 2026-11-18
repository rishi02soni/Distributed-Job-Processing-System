import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Activity,
  CheckCircle2,
  XCircle,
  Clock,
  Layers,
  Cpu,
  RefreshCw,
  Plus,
  ArrowUpRight,
  TrendingUp,
  AlertTriangle
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { StatusBadge } from '../components/ui/Badge';
import { useJobStats } from '../hooks/useJobStats';
import { useJobs } from '../hooks/useJobs';
import { useWorkers } from '../hooks/useWorkers';
import { useAnalytics } from '../hooks/useAnalytics';
import { formatDuration, formatDate } from '../lib/utils';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid
} from 'recharts';

export const OverviewDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { data: stats, isLoading: isStatsLoading, refetch: refetchStats } = useJobStats();
  const { data: recentJobs, isLoading: isJobsLoading, refetch: refetchJobs } = useJobs({ size: 6 });
  const { data: workers, refetch: refetchWorkers } = useWorkers();
  const { data: analytics, refetch: refetchAnalytics } = useAnalytics();

  const handleRefresh = () => {
    refetchStats();
    refetchJobs();
    refetchWorkers();
    refetchAnalytics();
  };

  const statCards = [
    {
      title: 'Total Jobs',
      value: stats?.totalJobs ?? 0,
      icon: Layers,
      color: 'text-blue-400',
      bg: 'bg-blue-950/40 border-blue-900/50',
    },
    {
      title: 'Active / Running',
      value: stats?.runningJobs ?? 0,
      icon: Activity,
      color: 'text-sky-400',
      bg: 'bg-sky-950/40 border-sky-900/50',
    },
    {
      title: 'Completed',
      value: stats?.completedJobs ?? 0,
      icon: CheckCircle2,
      color: 'text-emerald-400',
      bg: 'bg-emerald-950/40 border-emerald-900/50',
    },
    {
      title: 'Failed',
      value: stats?.failedJobs ?? 0,
      icon: XCircle,
      color: 'text-rose-400',
      bg: 'bg-rose-950/40 border-rose-900/50',
    },
    {
      title: 'Success Rate',
      value: `${(stats?.successRate ?? 100).toFixed(1)}%`,
      icon: TrendingUp,
      color: 'text-emerald-400',
      bg: 'bg-emerald-950/30 border-emerald-900/40',
    },
    {
      title: 'Avg Latency',
      value: formatDuration(stats?.avgProcessingTimeMs),
      icon: Clock,
      color: 'text-amber-400',
      bg: 'bg-amber-950/30 border-amber-900/40',
    },
    {
      title: 'Queue Depth',
      value: stats?.queueDepth ?? 0,
      icon: Layers,
      color: 'text-indigo-400',
      bg: 'bg-indigo-950/30 border-indigo-900/40',
    },
    {
      title: 'Active Workers',
      value: stats?.activeWorkers ?? 0,
      icon: Cpu,
      color: 'text-teal-400',
      bg: 'bg-teal-950/30 border-teal-900/40',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Top Banner & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white">System Overview</h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time telemetry from Redis Streams and distributed worker nodes
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm" onClick={handleRefresh}>
            <RefreshCw className="w-3.5 h-3.5 mr-1" />
            <span>Refresh</span>
          </Button>
          <Button size="sm" onClick={() => navigate('/jobs/new')}>
            <Plus className="w-3.5 h-3.5 mr-1" />
            <span>Submit Job</span>
          </Button>
        </div>
      </div>

      {/* 8 Stat Cards Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
        {statCards.map((card, idx) => {
          const Icon = card.icon;
          return (
            <Card key={idx} className="p-4 bg-[#111827]/90 border-slate-800/90 relative overflow-hidden group hover:border-slate-700 transition-all">
              <div className="flex items-center justify-between">
                <span className="text-xs text-slate-400 font-medium">{card.title}</span>
                <div className={`p-1.5 rounded-lg border ${card.bg}`}>
                  <Icon className={`w-3.5 h-3.5 ${card.color}`} />
                </div>
              </div>
              <div className="mt-2">
                <span className="text-xl font-bold font-mono tracking-tight text-white block">
                  {card.value}
                </span>
              </div>
            </Card>
          );
        })}
      </div>

      {/* Main Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Throughput Area Chart */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Throughput (Jobs / Window)</CardTitle>
                <CardDescription>Processed jobs per minute interval</CardDescription>
              </div>
              <span className="text-[11px] font-mono text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-800/40">
                Live Polling
              </span>
            </div>
          </CardHeader>
          <CardContent>
            <div className="h-64 w-full">
              {analytics?.throughputOverTime && analytics.throughputOverTime.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={analytics.throughputOverTime}>
                    <defs>
                      <linearGradient id="compGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                        <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                      </linearGradient>
                      <linearGradient id="failGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#f43f5e" stopOpacity={0.4} />
                        <stop offset="95%" stopColor="#f43f5e" stopOpacity={0.0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                    <XAxis dataKey="timestamp" stroke="#64748b" fontSize={11} />
                    <YAxis stroke="#64748b" fontSize={11} allowDecimals={false} />
                    <Tooltip
                      contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px', fontSize: '12px' }}
                    />
                    <Area type="monotone" dataKey="completed" name="Completed" stroke="#10b981" fillOpacity={1} fill="url(#compGrad)" />
                    <Area type="monotone" dataKey="failed" name="Failed" stroke="#f43f5e" fillOpacity={1} fill="url(#failGrad)" />
                  </AreaChart>
                </ResponsiveContainer>
              ) : (
                <div className="h-full flex items-center justify-center text-xs text-slate-500 font-mono">
                  Awaiting throughput metrics...
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Worker Health Overview Card */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Worker Fleet</CardTitle>
              <Button variant="ghost" size="sm" onClick={() => navigate('/workers')}>
                <span className="text-xs">All</span>
                <ArrowUpRight className="w-3.5 h-3.5 ml-1" />
              </Button>
            </div>
            <CardDescription>Active processing nodes</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {workers && workers.length > 0 ? (
              workers.slice(0, 4).map((worker) => (
                <div key={worker.workerId} className="p-3 rounded-lg bg-slate-900/80 border border-slate-800/80 space-y-2">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className={`w-2 h-2 rounded-full ${worker.healthy ? 'bg-emerald-400' : 'bg-rose-500'} animate-pulse`} />
                      <span className="text-xs font-mono font-medium text-slate-200">{worker.workerId}</span>
                    </div>
                    <span className="text-[11px] font-mono text-slate-400">
                      {worker.activeJobs} / {worker.concurrency} active
                    </span>
                  </div>
                  <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                    <div
                      className="bg-blue-500 h-1.5 rounded-full transition-all"
                      style={{ width: `${Math.min(100, (worker.activeJobs / worker.concurrency) * 100)}%` }}
                    />
                  </div>
                  <div className="flex justify-between text-[10px] text-slate-400 font-mono">
                    <span>CPU: {worker.cpuUsage}%</span>
                    <span>Done: {worker.completedJobs}</span>
                  </div>
                </div>
              ))
            ) : (
              <div className="py-8 text-center text-xs text-slate-500">
                No active workers reported. Check worker service.
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Recent Activity Table */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Recent Job Activity</CardTitle>
              <CardDescription>Real-time execution updates across all job types</CardDescription>
            </div>
            <Button variant="outline" size="sm" onClick={() => navigate('/jobs')}>
              <span>View All Jobs</span>
              <ArrowUpRight className="w-3.5 h-3.5 ml-1" />
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-900/60 border-b border-slate-800 text-slate-400 font-mono">
                <tr>
                  <th className="py-2.5 px-3">Job ID</th>
                  <th className="py-2.5 px-3">Name</th>
                  <th className="py-2.5 px-3">Type</th>
                  <th className="py-2.5 px-3">Status</th>
                  <th className="py-2.5 px-3">Duration</th>
                  <th className="py-2.5 px-3">Attempts</th>
                  <th className="py-2.5 px-3">Submitted</th>
                  <th className="py-2.5 px-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {recentJobs?.content && recentJobs.content.length > 0 ? (
                  recentJobs.content.map((job) => (
                    <tr key={job.id} className="hover:bg-slate-900/40 transition-colors">
                      <td className="py-3 px-3 font-semibold text-blue-400">
                        <button
                          onClick={() => navigate(`/jobs/${job.id}`)}
                          className="hover:underline text-left cursor-pointer"
                        >
                          {job.id}
                        </button>
                      </td>
                      <td className="py-3 px-3 text-slate-200 font-sans">{job.name}</td>
                      <td className="py-3 px-3 text-slate-400">{job.type}</td>
                      <td className="py-3 px-3">
                        <StatusBadge status={job.status} />
                      </td>
                      <td className="py-3 px-3 text-slate-400">{formatDuration(job.durationMs)}</td>
                      <td className="py-3 px-3 text-slate-400">{job.attemptCount}/{job.maxRetries}</td>
                      <td className="py-3 px-3 text-slate-400">{formatDate(job.createdAt)}</td>
                      <td className="py-3 px-3 text-right">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => navigate(`/jobs/${job.id}`)}
                        >
                          Details
                        </Button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={8} className="py-8 text-center text-slate-500 font-sans">
                      No jobs have been submitted yet. Click "Submit Job" to queue your first asynchronous task.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
