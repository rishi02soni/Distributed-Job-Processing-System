import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Send,
  Code2,
  Sparkles,
  KeyRound,
  Clock,
  RotateCw,
  AlertCircle,
  CheckCircle2,
  ArrowLeft
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { useCreateJob } from '../hooks/useJobs';
import type { JobType, JobPriority, CreateJobInput } from '../types/job';

const PAYLOAD_TEMPLATES: Record<JobType, Record<string, any>> = {
  EMAIL: {
    to: "alice.engineer@techcorp.io",
    subject: "Distributed Cluster Deployment Notice",
    body: "Build #104 has been successfully verified across all availability zones.",
    simulateFailure: false
  },
  REPORT: {
    reportName: "Quarterly Revenue & Throughput Breakdown",
    format: "PDF",
    dateRange: "2026-Q3",
    metrics: ["p99_latency", "success_rate", "active_nodes"],
    simulateFailure: false
  },
  DATA_PROCESSING: {
    dataset: "customer_activity_partition_04",
    transformation: "ETL_CLEANSE_AND_VECTORIZE",
    batchSize: 1500,
    simulateFailure: false
  },
  CUSTOM: {
    action: "SYNCHRONIZE_EVENT_STORE",
    targetTenant: "prod-tenant-west",
    parameters: {
      concurrencyLimit: 8,
      dryRun: false
    },
    simulateFailure: false
  }
};

export const CreateJob: React.FC = () => {
  const navigate = useNavigate();
  const createJobMutation = useCreateJob();

  const [name, setName] = useState('');
  const [type, setType] = useState<JobType>('EMAIL');
  const [priority, setPriority] = useState<JobPriority>('NORMAL');
  const [payloadText, setPayloadText] = useState(JSON.stringify(PAYLOAD_TEMPLATES.EMAIL, null, 2));
  const [jsonError, setJsonError] = useState<string | null>(null);
  const [idempotencyKey, setIdempotencyKey] = useState('');
  const [delaySeconds, setDelaySeconds] = useState<number | ''>('');
  const [maxRetries, setMaxRetries] = useState<number | ''>(3);
  const [timeoutSeconds, setTimeoutSeconds] = useState<number | ''>(60);
  const [errorBanner, setErrorBanner] = useState<string | null>(null);

  const handleTypeChange = (newType: JobType) => {
    setType(newType);
    setPayloadText(JSON.stringify(PAYLOAD_TEMPLATES[newType], null, 2));
    setJsonError(null);
  };

  const handleFormatJson = () => {
    try {
      const parsed = JSON.parse(payloadText);
      setPayloadText(JSON.stringify(parsed, null, 2));
      setJsonError(null);
    } catch (err: any) {
      setJsonError('Invalid JSON format: ' + err.message);
    }
  };

  const handleGenerateIdempotencyKey = () => {
    const randomKey = 'idemp_' + Math.random().toString(36).substring(2, 10) + '_' + Date.now();
    setIdempotencyKey(randomKey);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorBanner(null);

    if (!name.trim()) {
      setErrorBanner('Job Name is required');
      return;
    }

    let parsedPayload = {};
    try {
      parsedPayload = JSON.parse(payloadText);
    } catch (err: any) {
      setJsonError('Please fix JSON syntax before submitting: ' + err.message);
      return;
    }

    const input: CreateJobInput = {
      name: name.trim(),
      type,
      priority,
      payload: parsedPayload,
      idempotencyKey: idempotencyKey.trim() || undefined,
      maxRetries: maxRetries !== '' ? Number(maxRetries) : undefined,
      timeoutSeconds: timeoutSeconds !== '' ? Number(timeoutSeconds) : undefined,
    };

    if (delaySeconds && Number(delaySeconds) > 0) {
      const scheduledTime = new Date(Date.now() + Number(delaySeconds) * 1000).toISOString();
      input.scheduledAt = scheduledTime;
    }

    try {
      const created = await createJobMutation.mutateAsync(input);
      navigate(`/jobs/${created.id}`);
    } catch (err: any) {
      setErrorBanner(err.message || 'Failed to submit job to cluster');
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="sm" onClick={() => navigate('/jobs')}>
          <ArrowLeft className="w-4 h-4 mr-1" />
          Back
        </Button>
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white">Create & Enqueue Job</h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Submit a job to the distributed Redis Stream consumer pool
          </p>
        </div>
      </div>

      {errorBanner && (
        <div className="p-4 bg-rose-950/70 border border-rose-800 rounded-lg text-xs text-rose-300 font-mono flex items-start gap-3">
          <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
          <div>
            <span className="font-semibold block mb-0.5">Submission Error</span>
            <span>{errorBanner}</span>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        <Card className="p-6 space-y-5">
          <CardHeader className="p-0">
            <CardTitle>Job Specification</CardTitle>
            <CardDescription>Configure metadata, priority, and payload</CardDescription>
          </CardHeader>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-1.5 md:col-span-2">
              <label className="text-xs font-medium text-slate-300">Job Name *</label>
              <Input
                placeholder="e.g. Generate Monthly Financial Digest"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-300">Job Type *</label>
              <Select
                value={type}
                onChange={(e) => handleTypeChange(e.target.value as JobType)}
                options={[
                  { value: 'EMAIL', label: 'EMAIL (SMTP Dispatch)' },
                  { value: 'REPORT', label: 'REPORT (Aggregation & Export)' },
                  { value: 'DATA_PROCESSING', label: 'DATA_PROCESSING (ETL Pipeline)' },
                  { value: 'CUSTOM', label: 'CUSTOM (Generic Task)' },
                ]}
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-300">Priority Level</label>
              <Select
                value={priority}
                onChange={(e) => setPriority(e.target.value as JobPriority)}
                options={[
                  { value: 'LOW', label: 'LOW (Priority 1)' },
                  { value: 'NORMAL', label: 'NORMAL (Priority 2)' },
                  { value: 'HIGH', label: 'HIGH (Priority 3)' },
                  { value: 'CRITICAL', label: 'CRITICAL (Priority 4)' },
                ]}
              />
            </div>
          </div>

          {/* JSON Payload Editor */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="text-xs font-medium text-slate-300 flex items-center gap-1.5">
                <Code2 className="w-3.5 h-3.5 text-blue-400" />
                <span>JSON Payload</span>
              </label>
              <div className="flex items-center gap-2">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={handleFormatJson}
                  className="h-7 text-[11px]"
                >
                  <Sparkles className="w-3 h-3 mr-1" />
                  Format JSON
                </Button>
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  onClick={() => handleTypeChange(type)}
                  className="h-7 text-[11px]"
                >
                  Reset Template
                </Button>
              </div>
            </div>

            <textarea
              rows={8}
              value={payloadText}
              onChange={(e) => {
                setPayloadText(e.target.value);
                setJsonError(null);
              }}
              className="w-full bg-slate-950/90 border border-slate-800 rounded-lg p-3 text-xs font-mono text-emerald-400 focus:outline-none focus:ring-1 focus:ring-blue-500 selection:bg-emerald-900"
              spellCheck={false}
            />
            {jsonError && (
              <p className="text-xs text-rose-400 font-mono mt-1">{jsonError}</p>
            )}
            <p className="text-[11px] text-slate-500">
              Tip: Set <code className="text-slate-400 font-mono">"simulateFailure": true</code> to test exponential backoff and DLQ routing.
            </p>
          </div>
        </Card>

        {/* Advanced Reliability Configuration */}
        <Card className="p-6 space-y-4">
          <CardHeader className="p-0">
            <CardTitle>Reliability & Scheduling Controls</CardTitle>
            <CardDescription>Idempotency, retries, and scheduled execution</CardDescription>
          </CardHeader>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Idempotency Key */}
            <div className="space-y-1.5 md:col-span-2">
              <div className="flex items-center justify-between">
                <label className="text-xs font-medium text-slate-300 flex items-center gap-1.5">
                  <KeyRound className="w-3.5 h-3.5 text-sky-400" />
                  <span>Idempotency Key (Optional)</span>
                </label>
                <button
                  type="button"
                  onClick={handleGenerateIdempotencyKey}
                  className="text-[11px] text-blue-400 hover:underline cursor-pointer"
                >
                  Generate Key
                </button>
              </div>
              <Input
                placeholder="Unique key to guarantee exactly-once processing (e.g. order-82381-charge)"
                value={idempotencyKey}
                onChange={(e) => setIdempotencyKey(e.target.value)}
              />
            </div>

            {/* Scheduled Delay */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-300 flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-amber-400" />
                <span>Execution Delay (Seconds)</span>
              </label>
              <Input
                type="number"
                min={0}
                placeholder="0 (Immediate execution)"
                value={delaySeconds}
                onChange={(e) => setDelaySeconds(e.target.value ? Number(e.target.value) : '')}
              />
            </div>

            {/* Max Retries Override */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-300 flex items-center gap-1.5">
                <RotateCw className="w-3.5 h-3.5 text-emerald-400" />
                <span>Max Retries</span>
              </label>
              <Input
                type="number"
                min={0}
                max={10}
                placeholder="3"
                value={maxRetries}
                onChange={(e) => setMaxRetries(e.target.value ? Number(e.target.value) : '')}
              />
            </div>
          </div>
        </Card>

        {/* Actions */}
        <div className="flex items-center justify-end gap-3">
          <Button
            type="button"
            variant="secondary"
            onClick={() => navigate('/jobs')}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            isLoading={createJobMutation.isPending}
            className="px-6"
          >
            <Send className="w-4 h-4 mr-1.5" />
            <span>Enqueue Job</span>
          </Button>
        </div>
      </form>
    </div>
  );
};
