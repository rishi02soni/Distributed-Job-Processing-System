import React from 'react';
import {
  Layers,
  Database,
  Clock,
  AlertTriangle,
  RefreshCw,
  Activity,
  ArrowRight,
  ShieldAlert,
  Server
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { useQueueStats } from '../hooks/useQueue';
import { formatDuration } from '../lib/utils';

export const QueueMonitoring: React.FC = () => {
  const { data: queue, isLoading, refetch } = useQueueStats();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white">Queue Telemetry & Redis Streams</h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time inspection of stream backlogs, consumer group lag, and Dead Letter Queue (DLQ)
          </p>
        </div>
        <Button variant="secondary" size="sm" onClick={() => refetch()}>
          <RefreshCw className="w-3.5 h-3.5 mr-1" />
          <span>Refresh Telemetry</span>
        </Button>
      </div>

      {/* Main KPI Row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <Card className="p-4 border-blue-900/40 bg-blue-950/20">
          <span className="text-xs text-slate-400">Stream Depth</span>
          <span className="text-2xl font-bold font-mono text-blue-400 block mt-1">
            {queue?.queueDepth ?? 0}
          </span>
          <span className="text-[11px] text-slate-500 font-mono mt-0.5 block truncate">
            {queue?.streamName || 'job:stream'}
          </span>
        </Card>

        <Card className="p-4 border-amber-900/40 bg-amber-950/20">
          <span className="text-xs text-slate-400">Pending Messages (PEL)</span>
          <span className="text-2xl font-bold font-mono text-amber-400 block mt-1">
            {queue?.pendingMessages ?? 0}
          </span>
          <span className="text-[11px] text-slate-500 font-mono mt-0.5 block">
            Awaiting worker ACKs
          </span>
        </Card>

        <Card className="p-4 border-rose-900/40 bg-rose-950/20">
          <span className="text-xs text-slate-400">Dead Letter Queue</span>
          <span className="text-2xl font-bold font-mono text-rose-400 block mt-1">
            {queue?.dlqSize ?? 0}
          </span>
          <span className="text-[11px] text-slate-500 font-mono mt-0.5 block">
            Retries exhausted
          </span>
        </Card>

        <Card className="p-4 border-emerald-900/40 bg-emerald-950/20">
          <span className="text-xs text-slate-400">Delayed / Retrying</span>
          <span className="text-2xl font-bold font-mono text-emerald-400 block mt-1">
            {queue?.delayedQueueSize ?? 0}
          </span>
          <span className="text-[11px] text-slate-500 font-mono mt-0.5 block">
            ZSET Backoff timers
          </span>
        </Card>
      </div>

      {/* Consumer Groups & Stream Architecture */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Consumer Groups */}
        <Card>
          <CardHeader>
            <CardTitle className="text-sm flex items-center gap-2">
              <Server className="w-4 h-4 text-blue-400" />
              <span>Consumer Groups</span>
            </CardTitle>
            <CardDescription>Redis Stream group members and cursor checkpoints</CardDescription>
          </CardHeader>
          <CardContent>
            {queue?.consumerGroups && queue.consumerGroups.length > 0 ? (
              <div className="space-y-3 font-mono text-xs">
                {queue.consumerGroups.map((group) => (
                  <div key={group.name} className="p-3.5 bg-slate-900/80 rounded-lg border border-slate-800 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-slate-100">{group.name}</span>
                      <span className="text-[10px] px-2 py-0.5 rounded bg-blue-950 text-blue-400 border border-blue-800">
                        Active Group
                      </span>
                    </div>
                    <div className="grid grid-cols-3 gap-2 text-[11px] text-slate-400 pt-1">
                      <div>
                        <span className="text-slate-500 block text-[10px]">CONSUMERS</span>
                        <span className="text-slate-200 font-semibold">{group.consumers}</span>
                      </div>
                      <div>
                        <span className="text-slate-500 block text-[10px]">PENDING PEL</span>
                        <span className="text-amber-400 font-semibold">{group.pending}</span>
                      </div>
                      <div>
                        <span className="text-slate-500 block text-[10px]">LAST DELIVERED</span>
                        <span className="text-slate-200 truncate block">{group.lastDeliveredId}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-8 text-center text-xs font-mono text-slate-500">
                No active consumer groups detected on this stream.
              </div>
            )}
          </CardContent>
        </Card>

        {/* Redis Stream Diagnostics */}
        <Card>
          <CardHeader>
            <CardTitle className="text-sm flex items-center gap-2">
              <Database className="w-4 h-4 text-emerald-400" />
              <span>Queue Performance & Architecture</span>
            </CardTitle>
            <CardDescription>Throughput metrics and stream retention details</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4 text-xs font-mono">
            <div className="p-3 bg-slate-900/80 rounded-lg border border-slate-800 space-y-2">
              <div className="flex justify-between">
                <span className="text-slate-400">Throughput Target:</span>
                <span className="text-emerald-400 font-semibold">{queue?.throughputPerMinute ?? 120} jobs/min</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Estimated Latency:</span>
                <span className="text-blue-400 font-semibold">
                  {((queue?.estimatedLatencySeconds ?? 0.05) * 1000).toFixed(0)} ms
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Stream Name:</span>
                <span className="text-slate-200">{queue?.streamName || 'job:stream'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Dead Letter Queue:</span>
                <span className="text-rose-400">job:stream:dlq</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Delayed Retry ZSet:</span>
                <span className="text-amber-400">job:delayed</span>
              </div>
            </div>

            <div className="p-3 bg-blue-950/30 border border-blue-900/40 rounded-lg text-slate-300 space-y-1 font-sans text-xs">
              <span className="font-semibold text-blue-400 block font-mono">Delivery Guarantees</span>
              <p className="text-slate-400 leading-relaxed text-[11px]">
                At-least-once message delivery is enforced via Redis consumer groups. Unacknowledged
                messages remain in the Pending Entries List (PEL) and are safely claimed by active workers
                upon node failure or execution timeout.
              </p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Pending Entries List (PEL) Table */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm flex items-center gap-2">
            <Clock className="w-4 h-4 text-amber-400" />
            <span>Pending Entries List (PEL)</span>
          </CardTitle>
          <CardDescription>In-flight unacknowledged messages currently claimed by workers</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs font-mono">
              <thead className="bg-[#0f172a] border-b border-slate-800 text-slate-400">
                <tr>
                  <th className="py-2.5 px-4">Message ID</th>
                  <th className="py-2.5 px-4">Claimed By Consumer</th>
                  <th className="py-2.5 px-4">Idle Time</th>
                  <th className="py-2.5 px-4">Delivery Count</th>
                  <th className="py-2.5 px-4 text-right">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {queue?.recentPending && queue.recentPending.length > 0 ? (
                  queue.recentPending.map((item) => (
                    <tr key={item.id} className="hover:bg-slate-900/50">
                      <td className="py-3 px-4 font-semibold text-amber-400">{item.id}</td>
                      <td className="py-3 px-4 text-slate-200">{item.consumerName}</td>
                      <td className="py-3 px-4 text-slate-400">{formatDuration(item.idleTimeMs)}</td>
                      <td className="py-3 px-4 text-slate-400">{item.deliveryCount}</td>
                      <td className="py-3 px-4 text-right">
                        <span className="inline-flex px-2 py-0.5 rounded text-[10px] bg-amber-950/80 text-amber-400 border border-amber-800/60">
                          PROCESSING
                        </span>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5} className="py-8 text-center text-slate-500 font-sans">
                      All consumed messages have been acknowledged. No pending backlog in PEL.
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
