import React from 'react';
import {
  Cpu,
  Activity,
  CheckCircle2,
  XCircle,
  RefreshCw,
  HardDrive,
  Clock,
  Server
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { useWorkers } from '../hooks/useWorkers';
import { formatDate } from '../lib/utils';
import type { WorkerNode } from '../types/worker';

export const WorkersPage: React.FC = () => {
  const { data: workers, isLoading, refetch } = useWorkers();

  const totalWorkers = workers?.length ?? 0;
  const activeWorkers = workers?.filter((w) => w.healthy && w.status !== 'DEAD').length ?? 0;
  const totalCapacity = workers?.reduce((acc, w) => acc + (w.concurrency || 0), 0) ?? 0;
  const activeJobs = workers?.reduce((acc, w) => acc + (w.activeJobs || 0), 0) ?? 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white">Worker Fleet & Health</h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Monitor real-time heartbeat, capacity utilization, and telemetry for distributed workers
          </p>
        </div>
        <Button variant="secondary" size="sm" onClick={() => refetch()}>
          <RefreshCw className="w-3.5 h-3.5 mr-1" />
          <span>Refresh Fleet</span>
        </Button>
      </div>

      {/* Top Stat KPI Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <Card className="p-4">
          <span className="text-xs text-slate-400">Total Registered</span>
          <span className="text-2xl font-bold font-mono text-white block mt-1">{totalWorkers}</span>
        </Card>
        <Card className="p-4">
          <span className="text-xs text-slate-400">Healthy & Active</span>
          <span className="text-2xl font-bold font-mono text-emerald-400 block mt-1">{activeWorkers}</span>
        </Card>
        <Card className="p-4">
          <span className="text-xs text-slate-400">Cluster Concurrency Pool</span>
          <span className="text-2xl font-bold font-mono text-blue-400 block mt-1">{totalCapacity} threads</span>
        </Card>
        <Card className="p-4">
          <span className="text-xs text-slate-400">Active Workload</span>
          <span className="text-2xl font-bold font-mono text-amber-400 block mt-1">{activeJobs} tasks</span>
        </Card>
      </div>

      {/* Workers Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {isLoading ? (
          <div className="col-span-full py-16 text-center text-xs font-mono text-slate-500">
            Scanning cluster for worker nodes...
          </div>
        ) : workers && workers.length > 0 ? (
          workers.map((worker: WorkerNode) => {
            const isHealthy = worker.healthy && worker.status !== 'DEAD';
            const loadPercent = Math.min(100, (worker.activeJobs / (worker.concurrency || 1)) * 100);

            return (
              <Card
                key={worker.workerId}
                className={`p-5 relative overflow-hidden transition-all ${
                  isHealthy ? 'border-slate-800' : 'border-rose-900/60 bg-rose-950/10'
                }`}
              >
                {/* Header */}
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-2.5">
                    <div className="p-2 rounded-lg bg-slate-900 border border-slate-800 text-slate-300">
                      <Server className="w-4 h-4 text-blue-400" />
                    </div>
                    <div>
                      <span className="text-xs font-bold font-mono text-white block truncate max-w-[170px]">
                        {worker.workerId}
                      </span>
                      <span className="text-[11px] text-slate-500 font-mono block">
                        host: {worker.hostname}
                      </span>
                    </div>
                  </div>

                  {/* Status Indicator */}
                  <span
                    className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-[10px] font-mono font-medium border ${
                      isHealthy
                        ? worker.status === 'BUSY'
                          ? 'bg-amber-950/60 text-amber-400 border-amber-800/40'
                          : 'bg-emerald-950/60 text-emerald-400 border-emerald-800/40'
                        : 'bg-rose-950/60 text-rose-400 border-rose-800/40'
                    }`}
                  >
                    <span
                      className={`w-1.5 h-1.5 rounded-full ${
                        isHealthy ? 'bg-emerald-400 animate-pulse' : 'bg-rose-400'
                      }`}
                    />
                    {isHealthy ? worker.status : 'DEAD'}
                  </span>
                </div>

                {/* Capacity Progress Bar */}
                <div className="mt-4 space-y-1.5">
                  <div className="flex justify-between text-xs font-mono">
                    <span className="text-slate-400">Concurrency Load:</span>
                    <span className="text-slate-200">
                      {worker.activeJobs} / {worker.concurrency} ({loadPercent.toFixed(0)}%)
                    </span>
                  </div>
                  <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
                    <div
                      className={`h-2 rounded-full transition-all ${
                        loadPercent > 80 ? 'bg-amber-500' : 'bg-blue-500'
                      }`}
                      style={{ width: `${loadPercent}%` }}
                    />
                  </div>
                </div>

                {/* Metrics Breakdown */}
                <div className="mt-4 grid grid-cols-2 gap-2 text-xs font-mono">
                  <div className="p-2.5 bg-slate-900/60 rounded-lg border border-slate-800/50">
                    <span className="text-[10px] text-slate-500 block">CPU LOAD</span>
                    <span className="text-slate-200 font-semibold">{worker.cpuUsage}%</span>
                  </div>
                  <div className="p-2.5 bg-slate-900/60 rounded-lg border border-slate-800/50">
                    <span className="text-[10px] text-slate-500 block">MEMORY</span>
                    <span className="text-slate-200 font-semibold">{worker.memoryUsage}%</span>
                  </div>
                  <div className="p-2.5 bg-slate-900/60 rounded-lg border border-slate-800/50">
                    <span className="text-[10px] text-slate-500 block">COMPLETED</span>
                    <span className="text-emerald-400 font-semibold">{worker.completedJobs}</span>
                  </div>
                  <div className="p-2.5 bg-slate-900/60 rounded-lg border border-slate-800/50">
                    <span className="text-[10px] text-slate-500 block">FAILED</span>
                    <span className="text-rose-400 font-semibold">{worker.failedJobs}</span>
                  </div>
                </div>

                {/* Footer Heartbeat */}
                <div className="mt-4 pt-3 border-t border-slate-800/60 flex items-center justify-between text-[11px] font-mono text-slate-500">
                  <div className="flex items-center gap-1">
                    <Clock className="w-3 h-3 text-slate-500" />
                    <span>Last heartbeat:</span>
                  </div>
                  <span className={worker.secondsSinceHeartbeat > 15 ? 'text-rose-400' : 'text-slate-400'}>
                    {worker.secondsSinceHeartbeat}s ago
                  </span>
                </div>
              </Card>
            );
          })
        ) : (
          <div className="col-span-full py-16 text-center text-xs font-mono text-slate-500">
            No worker nodes currently registered. Start the backend worker service to register instances.
          </div>
        )}
      </div>
    </div>
  );
};
