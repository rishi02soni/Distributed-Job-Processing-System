import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Copy,
  Check,
  RotateCcw,
  Ban,
  Clock,
  Calendar,
  Cpu,
  Layers,
  Terminal,
  AlertOctagon,
  FileCode,
  ShieldCheck,
  RefreshCw
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { StatusBadge, PriorityBadge } from '../components/ui/Badge';
import { useJobDetails } from '../hooks/useJobDetails';
import { formatDuration, formatDate } from '../lib/utils';
import type { JobLog } from '../types/job';

export const JobDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [copiedId, setCopiedId] = useState(false);
  const [logFilter, setLogFilter] = useState<'ALL' | 'INFO' | 'WARN' | 'ERROR'>('ALL');

  const {
    job,
    isLoading,
    isError,
    error,
    logs,
    retry,
    isRetrying,
    cancel,
    isCancelling,
    refetch
  } = useJobDetails(id || '');

  const copyJobId = () => {
    if (job?.id) {
      navigator.clipboard.writeText(job.id);
      setCopiedId(true);
      setTimeout(() => setCopiedId(false), 2000);
    }
  };

  const handleRetry = async () => {
    if (!job) return;
    try {
      await retry();
    } catch (err: any) {
      alert(`Retry failed: ${err.message}`);
    }
  };

  const handleCancel = async () => {
    if (!job) return;
    if (!confirm('Are you sure you want to cancel this job?')) return;
    try {
      await cancel();
    } catch (err: any) {
      alert(`Cancellation failed: ${err.message}`);
    }
  };

  if (isLoading) {
    return (
      <div className="py-20 text-center space-y-3">
        <RefreshCw className="w-6 h-6 animate-spin text-blue-500 mx-auto" />
        <p className="text-sm font-mono text-slate-400">Loading job details from cluster...</p>
      </div>
    );
  }

  if (isError || !job) {
    return (
      <div className="max-w-2xl mx-auto py-16 text-center space-y-4">
        <AlertOctagon className="w-10 h-10 text-rose-500 mx-auto" />
        <h2 className="text-lg font-bold text-white">Job Not Found</h2>
        <p className="text-xs text-slate-400 font-mono">
          {error?.message || 'No job found matching the specified ID.'}
        </p>
        <Button variant="secondary" onClick={() => navigate('/jobs')}>
          <ArrowLeft className="w-4 h-4 mr-1.5" />
          Back to Jobs
        </Button>
      </div>
    );
  }

  const filteredLogs = logs.filter((log: JobLog) => {
    if (logFilter === 'ALL') return true;
    return log.level === logFilter;
  });

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Top Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="sm" onClick={() => navigate('/jobs')}>
            <ArrowLeft className="w-4 h-4 mr-1" />
            Jobs
          </Button>
          <div className="flex items-center gap-2">
            <span className="font-mono text-lg font-bold text-white">{job.id}</span>
            <button
              onClick={copyJobId}
              className="p-1 rounded hover:bg-slate-800 text-slate-400 hover:text-white transition-colors cursor-pointer"
              title="Copy Job ID"
            >
              {copiedId ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
            </button>
          </div>
          <StatusBadge status={job.status} />
          <PriorityBadge priority={job.priority} />
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm" onClick={() => refetch()}>
            <RefreshCw className="w-3.5 h-3.5 mr-1" />
            Refresh
          </Button>
          {(job.status === 'FAILED' || job.status === 'CANCELLED') && (
            <Button
              variant="secondary"
              size="sm"
              onClick={handleRetry}
              isLoading={isRetrying}
            >
              <RotateCcw className="w-3.5 h-3.5 mr-1" />
              Retry Job
            </Button>
          )}
          {(job.status === 'QUEUED' || job.status === 'RUNNING' || job.status === 'RETRYING') && (
            <Button
              variant="danger"
              size="sm"
              onClick={handleCancel}
              isLoading={isCancelling}
            >
              <Ban className="w-3.5 h-3.5 mr-1" />
              Cancel Job
            </Button>
          )}
        </div>
      </div>

      {/* Execution Timeline Card */}
      <Card className="p-5 bg-[#0f172a]/80">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 text-xs font-mono">
          <div className="flex items-center gap-2">
            <div className={`w-3 h-3 rounded-full ${job.createdAt ? 'bg-blue-500' : 'bg-slate-700'}`} />
            <div className="text-slate-300">
              <span className="text-slate-500 block text-[10px]">SUBMITTED</span>
              <span>{formatDate(job.createdAt)}</span>
            </div>
          </div>
          <div className="h-0.5 flex-1 bg-slate-800 hidden sm:block" />
          <div className="flex items-center gap-2">
            <div className={`w-3 h-3 rounded-full ${job.startedAt ? 'bg-amber-500' : 'bg-slate-700'}`} />
            <div className="text-slate-300">
              <span className="text-slate-500 block text-[10px]">STARTED</span>
              <span>{formatDate(job.startedAt)}</span>
            </div>
          </div>
          <div className="h-0.5 flex-1 bg-slate-800 hidden sm:block" />
          <div className="flex items-center gap-2">
            <div
              className={`w-3 h-3 rounded-full ${
                job.status === 'COMPLETED'
                  ? 'bg-emerald-500'
                  : job.status === 'FAILED'
                  ? 'bg-rose-500'
                  : 'bg-slate-700'
              }`}
            />
            <div className="text-slate-300">
              <span className="text-slate-500 block text-[10px]">FINISHED</span>
              <span>{formatDate(job.completedAt)}</span>
            </div>
          </div>
        </div>
      </Card>

      {/* Metadata Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <Card className="p-4">
          <span className="text-xs text-slate-400">Execution Duration</span>
          <span className="text-base font-bold font-mono text-white block mt-1">
            {formatDuration(job.durationMs)}
          </span>
        </Card>
        <Card className="p-4">
          <span className="text-xs text-slate-400">Attempt Count</span>
          <span className="text-base font-bold font-mono text-white block mt-1">
            {job.attemptCount} / {job.maxRetries}
          </span>
        </Card>
        <Card className="p-4">
          <span className="text-xs text-slate-400">Assigned Worker</span>
          <span className="text-xs font-mono text-slate-200 block mt-1 truncate">
            {job.workerId || 'Pending Assignment'}
          </span>
        </Card>
        <Card className="p-4">
          <span className="text-xs text-slate-400">Idempotency Key</span>
          <span className="text-xs font-mono text-slate-200 block mt-1 truncate">
            {job.idempotencyKey || 'None'}
          </span>
        </Card>
      </div>

      {/* Error Details Alert (if Failed) */}
      {job.errorDetails && (
        <Card className="p-5 border-rose-900/60 bg-rose-950/20">
          <div className="flex items-start gap-3">
            <AlertOctagon className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
            <div className="space-y-2 w-full">
              <div>
                <span className="text-xs font-semibold text-rose-300 font-mono block">
                  {job.errorDetails.errorType || 'JobExecutionException'}: {job.errorDetails.message}
                </span>
                <span className="text-[11px] text-slate-400 font-mono">
                  Failed at: {formatDate(job.errorDetails.failedAt)}
                </span>
              </div>
              {job.errorDetails.stackTrace && (
                <div className="mt-2">
                  <span className="text-[11px] text-slate-400 font-mono block mb-1">Stack Trace:</span>
                  <pre className="p-3 bg-slate-950 border border-slate-800 rounded-lg text-[11px] font-mono text-rose-300 overflow-x-auto max-h-48">
                    {job.errorDetails.stackTrace}
                  </pre>
                </div>
              )}
            </div>
          </div>
        </Card>
      )}

      {/* Payload and Result Split */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Input Payload */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm flex items-center gap-2">
                <FileCode className="w-4 h-4 text-blue-400" />
                <span>Input Payload</span>
              </CardTitle>
              <button
                onClick={() => navigator.clipboard.writeText(JSON.stringify(job.payload, null, 2))}
                className="text-xs text-slate-400 hover:text-white flex items-center gap-1 cursor-pointer font-mono"
              >
                <Copy className="w-3 h-3" />
                <span>Copy</span>
              </button>
            </div>
          </CardHeader>
          <CardContent>
            <pre className="p-3 bg-slate-950/90 border border-slate-800/80 rounded-lg text-xs font-mono text-slate-200 overflow-x-auto max-h-72">
              {JSON.stringify(job.payload, null, 2)}
            </pre>
          </CardContent>
        </Card>

        {/* Output Result */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm flex items-center gap-2">
                <ShieldCheck className="w-4 h-4 text-emerald-400" />
                <span>Execution Result</span>
              </CardTitle>
              {job.result && (
                <button
                  onClick={() => navigator.clipboard.writeText(JSON.stringify(job.result, null, 2))}
                  className="text-xs text-slate-400 hover:text-white flex items-center gap-1 cursor-pointer font-mono"
                >
                  <Copy className="w-3 h-3" />
                  <span>Copy</span>
                </button>
              )}
            </div>
          </CardHeader>
          <CardContent>
            {job.result && Object.keys(job.result).length > 0 ? (
              <pre className="p-3 bg-slate-950/90 border border-slate-800/80 rounded-lg text-xs font-mono text-emerald-400 overflow-x-auto max-h-72">
                {JSON.stringify(job.result, null, 2)}
              </pre>
            ) : (
              <div className="p-8 text-center text-xs text-slate-500 font-mono">
                {job.status === 'RUNNING'
                  ? 'Job is currently executing. Result will populate upon completion.'
                  : job.status === 'QUEUED'
                  ? 'Job is queued in Redis Stream awaiting an idle worker.'
                  : 'No execution result available for this job state.'}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Execution Logs Terminal */}
      <Card className="bg-[#090d16] border-slate-800">
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm flex items-center gap-2">
              <Terminal className="w-4 h-4 text-sky-400" />
              <span>Execution Logs</span>
              <span className="text-xs font-mono text-slate-500">({logs.length} entries)</span>
            </CardTitle>
            <div className="flex items-center gap-1.5 font-mono text-xs">
              {(['ALL', 'INFO', 'WARN', 'ERROR'] as const).map((lvl) => (
                <button
                  key={lvl}
                  onClick={() => setLogFilter(lvl)}
                  className={`px-2 py-0.5 rounded text-[11px] transition-colors cursor-pointer ${
                    logFilter === lvl
                      ? 'bg-blue-600/30 text-blue-400 border border-blue-500/50'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  {lvl}
                </button>
              ))}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="p-3 bg-slate-950 border border-slate-800/80 rounded-lg font-mono text-xs space-y-1.5 max-h-80 overflow-y-auto">
            {filteredLogs.length > 0 ? (
              filteredLogs.map((logItem) => {
                const color =
                  logItem.level === 'ERROR'
                    ? 'text-rose-400'
                    : logItem.level === 'WARN'
                    ? 'text-amber-400'
                    : 'text-slate-300';
                return (
                  <div key={logItem.id} className="flex items-start gap-2 leading-relaxed">
                    <span className="text-slate-500 shrink-0 text-[11px]">
                      {new Date(logItem.timestamp).toISOString().substring(11, 23)}
                    </span>
                    <span className={`px-1.5 py-0.2 rounded text-[10px] uppercase font-bold shrink-0 ${
                      logItem.level === 'ERROR'
                        ? 'bg-rose-950 text-rose-400'
                        : logItem.level === 'WARN'
                        ? 'bg-amber-950 text-amber-400'
                        : 'bg-slate-800 text-slate-400'
                    }`}>
                      {logItem.level}
                    </span>
                    <span className={color}>{logItem.message}</span>
                  </div>
                );
              })
            ) : (
              <div className="py-6 text-center text-slate-500 font-mono text-xs">
                No logs recorded yet.
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
