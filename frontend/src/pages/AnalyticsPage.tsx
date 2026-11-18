import React from 'react';
import {
  BarChart3,
  TrendingUp,
  Clock,
  PieChart as PieIcon,
  AlertTriangle,
  RefreshCw,
  Cpu
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { useAnalytics } from '../hooks/useAnalytics';
import { formatDuration } from '../lib/utils';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  CartesianGrid
} from 'recharts';

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6', '#64748b'];

export const AnalyticsPage: React.FC = () => {
  const { data: analytics, isLoading, refetch } = useAnalytics();

  const latencyData = analytics?.latency
    ? [
        { metric: 'Min', value: analytics.latency.min },
        { metric: 'p50', value: analytics.latency.p50 },
        { metric: 'Avg', value: Math.round(analytics.latency.avg) },
        { metric: 'p90', value: analytics.latency.p90 },
        { metric: 'p99', value: analytics.latency.p99 },
        { metric: 'Max', value: analytics.latency.max },
      ]
    : [];

  const typeData = analytics?.jobTypeDistribution
    ? Object.entries(analytics.jobTypeDistribution).map(([name, value]) => ({
        name,
        value,
      }))
    : [];

  const failureData = analytics?.failureReasonDistribution
    ? Object.entries(analytics.failureReasonDistribution).map(([reason, count]) => ({
        reason,
        count,
      }))
    : [];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white">System Analytics & Performance</h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Deep dive into processing latency percentiles, throughput trends, and error categorizations
          </p>
        </div>
        <Button variant="secondary" size="sm" onClick={() => refetch()}>
          <RefreshCw className="w-3.5 h-3.5 mr-1" />
          <span>Refresh Analytics</span>
        </Button>
      </div>

      {/* Latency Percentile Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <Card className="p-4 bg-[#111827]/90">
          <span className="text-xs text-slate-400">p50 Latency (Median)</span>
          <span className="text-2xl font-bold font-mono text-emerald-400 block mt-1">
            {formatDuration(analytics?.latency.p50)}
          </span>
        </Card>
        <Card className="p-4 bg-[#111827]/90">
          <span className="text-xs text-slate-400">p90 Latency</span>
          <span className="text-2xl font-bold font-mono text-sky-400 block mt-1">
            {formatDuration(analytics?.latency.p90)}
          </span>
        </Card>
        <Card className="p-4 bg-[#111827]/90">
          <span className="text-xs text-slate-400">p99 Latency (Tail)</span>
          <span className="text-2xl font-bold font-mono text-amber-400 block mt-1">
            {formatDuration(analytics?.latency.p99)}
          </span>
        </Card>
        <Card className="p-4 bg-[#111827]/90">
          <span className="text-xs text-slate-400">Average Processing Time</span>
          <span className="text-2xl font-bold font-mono text-blue-400 block mt-1">
            {formatDuration(analytics?.latency.avg)}
          </span>
        </Card>
      </div>

      {/* Charts Grid 1: Throughput and Latency */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Throughput Area Chart */}
        <Card>
          <CardHeader>
            <CardTitle className="text-sm flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-emerald-400" />
              <span>Jobs Processed Per Minute Window</span>
            </CardTitle>
            <CardDescription>Completed vs Failed job executions</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={analytics?.throughputOverTime || []}>
                  <defs>
                    <linearGradient id="compArea" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                    </linearGradient>
                    <linearGradient id="failArea" x1="0" y1="0" x2="0" y2="1">
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
                  <Area type="monotone" dataKey="completed" name="Completed" stroke="#10b981" fill="url(#compArea)" />
                  <Area type="monotone" dataKey="failed" name="Failed" stroke="#f43f5e" fill="url(#failArea)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Latency Percentiles Bar Chart */}
        <Card>
          <CardHeader>
            <CardTitle className="text-sm flex items-center gap-2">
              <Clock className="w-4 h-4 text-amber-400" />
              <span>Latency Distribution (ms)</span>
            </CardTitle>
            <CardDescription>Min, median (p50), avg, p90, and tail p99</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={latencyData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                  <XAxis dataKey="metric" stroke="#64748b" fontSize={11} />
                  <YAxis stroke="#64748b" fontSize={11} />
                  <Tooltip
                    formatter={(val: number) => [`${val} ms`, 'Latency']}
                    contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px', fontSize: '12px' }}
                  />
                  <Bar dataKey="value" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Charts Grid 2: Distribution and Failures */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Job Type Breakdown */}
        <Card>
          <CardHeader>
            <CardTitle className="text-sm flex items-center gap-2">
              <PieIcon className="w-4 h-4 text-sky-400" />
              <span>Job Type Volume Breakdown</span>
            </CardTitle>
            <CardDescription>Proportions of Email, Report, ETL, and Custom jobs</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-64 w-full">
              {typeData.length > 0 && typeData.some((d) => d.value > 0) ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={typeData}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      outerRadius={80}
                      label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                      labelLine={false}
                      fontSize={11}
                    >
                      {typeData.map((_, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px', fontSize: '12px' }}
                    />
                    <Legend verticalAlign="bottom" height={36} wrapperStyle={{ fontSize: '11px', color: '#94a3b8' }} />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="h-full flex items-center justify-center text-xs text-slate-500 font-mono">
                  No job data recorded yet.
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Failure Reasons Bar Chart */}
        <Card>
          <CardHeader>
            <CardTitle className="text-sm flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-rose-400" />
              <span>Failure Root Causes</span>
            </CardTitle>
            <CardDescription>Categorized error classifications from failed executions</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-64 w-full">
              {failureData.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={failureData} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                    <XAxis type="number" stroke="#64748b" fontSize={11} allowDecimals={false} />
                    <YAxis dataKey="reason" type="category" stroke="#64748b" fontSize={11} width={130} />
                    <Tooltip
                      contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px', fontSize: '12px' }}
                    />
                    <Bar dataKey="count" fill="#f43f5e" radius={[0, 4, 4, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div className="h-full flex items-center justify-center text-xs text-slate-500 font-mono">
                  No job failures recorded. System operating at 100% reliability.
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Worker Performance Table */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm flex items-center gap-2">
            <Cpu className="w-4 h-4 text-indigo-400" />
            <span>Worker Node Execution Performance</span>
          </CardTitle>
          <CardDescription>Throughput and success ratios by individual worker instance</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs font-mono">
              <thead className="bg-[#0f172a] border-b border-slate-800 text-slate-400">
                <tr>
                  <th className="py-2.5 px-4">Worker ID</th>
                  <th className="py-2.5 px-4">Jobs Completed</th>
                  <th className="py-2.5 px-4">Jobs Failed</th>
                  <th className="py-2.5 px-4">Avg Duration</th>
                  <th className="py-2.5 px-4 text-right">Success Rate</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {analytics?.workerPerformance && analytics.workerPerformance.length > 0 ? (
                  analytics.workerPerformance.map((wp) => (
                    <tr key={wp.workerId} className="hover:bg-slate-900/50">
                      <td className="py-3 px-4 font-semibold text-blue-400">{wp.workerId}</td>
                      <td className="py-3 px-4 text-emerald-400">{wp.jobsCompleted}</td>
                      <td className="py-3 px-4 text-rose-400">{wp.jobsFailed}</td>
                      <td className="py-3 px-4 text-slate-300">{formatDuration(wp.avgDurationMs)}</td>
                      <td className="py-3 px-4 text-right">
                        <span className="font-bold text-emerald-400">
                          {wp.successRate.toFixed(1)}%
                        </span>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5} className="py-8 text-center text-slate-500 font-sans">
                      No worker telemetry collected yet.
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
