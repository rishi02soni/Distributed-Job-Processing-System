import React from 'react';
import { cn } from '../../lib/utils';
import type { JobStatus, JobPriority } from '../../types/job';

interface StatusBadgeProps {
  status: JobStatus;
  className?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, className }) => {
  const styles: Record<JobStatus, string> = {
    QUEUED: 'bg-indigo-950/80 text-indigo-400 border-indigo-800/60',
    RUNNING: 'bg-blue-950/80 text-blue-400 border-blue-800/60 animate-pulse',
    COMPLETED: 'bg-emerald-950/80 text-emerald-400 border-emerald-800/60',
    FAILED: 'bg-rose-950/80 text-rose-400 border-rose-800/60',
    RETRYING: 'bg-amber-950/80 text-amber-400 border-amber-800/60',
    CANCELLED: 'bg-slate-900 text-slate-400 border-slate-700/60',
  };

  const dots: Record<JobStatus, string> = {
    QUEUED: 'bg-indigo-400',
    RUNNING: 'bg-blue-400 animate-ping',
    COMPLETED: 'bg-emerald-400',
    FAILED: 'bg-rose-400',
    RETRYING: 'bg-amber-400',
    CANCELLED: 'bg-slate-400',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium border font-mono',
        styles[status],
        className
      )}
    >
      <span className={cn('w-1.5 h-1.5 rounded-full', dots[status])} />
      {status}
    </span>
  );
};

interface PriorityBadgeProps {
  priority: JobPriority;
  className?: string;
}

export const PriorityBadge: React.FC<PriorityBadgeProps> = ({ priority, className }) => {
  const styles: Record<JobPriority, string> = {
    LOW: 'text-slate-400 bg-slate-900 border-slate-700',
    NORMAL: 'text-sky-400 bg-sky-950/60 border-sky-800/50',
    HIGH: 'text-amber-400 bg-amber-950/60 border-amber-800/50',
    CRITICAL: 'text-rose-400 bg-rose-950/70 border-rose-800/60 font-semibold',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center px-2 py-0.5 rounded text-[11px] font-mono uppercase tracking-wider border',
        styles[priority],
        className
      )}
    >
      {priority}
    </span>
  );
};
