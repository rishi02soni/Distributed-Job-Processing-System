import React, { useState, useEffect } from 'react';
import {
  Sliders,
  Save,
  RotateCcw,
  AlertCircle,
  CheckCircle2,
  AlertTriangle,
  Info
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { useSettings } from '../hooks/useSettings';
import type { SystemSettings } from '../types/settings';

export const SettingsPage: React.FC = () => {
  const { settings, isLoading, updateSettings, isUpdating } = useSettings();

  const [formData, setFormData] = useState<SystemSettings>({
    defaultMaxRetries: 3,
    backoffBaseMs: 2000,
    backoffMultiplier: 2.0,
    jobTimeoutSeconds: 60,
    workerConcurrency: 4,
    streamRetentionMaxLen: 10000,
    dashboardRefreshIntervalMs: 3000,
  });

  const [successBanner, setSuccessBanner] = useState(false);
  const [errorBanner, setErrorBanner] = useState<string | null>(null);

  useEffect(() => {
    if (settings) {
      setFormData(settings);
    }
  }, [settings]);

  const handleChange = (field: keyof SystemSettings, val: number) => {
    setFormData((prev) => ({
      ...prev,
      [field]: val,
    }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorBanner(null);
    setSuccessBanner(false);

    try {
      await updateSettings(formData);
      setSuccessBanner(true);
      setTimeout(() => setSuccessBanner(false), 4000);
    } catch (err: any) {
      setErrorBanner(err.message || 'Failed to save settings');
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white">Engine Configuration & Settings</h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Tune queue backoff, retry limits, job watchdog timeouts, and thread concurrency
          </p>
        </div>
      </div>

      {successBanner && (
        <div className="p-4 bg-emerald-950/70 border border-emerald-800 rounded-lg text-xs text-emerald-300 font-mono flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>Settings successfully updated and persisted in cluster configuration.</span>
        </div>
      )}

      {errorBanner && (
        <div className="p-4 bg-rose-950/70 border border-rose-800 rounded-lg text-xs text-rose-300 font-mono flex items-center gap-2">
          <AlertCircle className="w-4 h-4 text-rose-400 shrink-0" />
          <span>{errorBanner}</span>
        </div>
      )}

      <form onSubmit={handleSave} className="space-y-6">
        {/* Retry & Backoff Configuration */}
        <Card className="p-6 space-y-5">
          <CardHeader className="p-0">
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm">Retry & Exponential Backoff Policy</CardTitle>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800">
                Hot Reloadable
              </span>
            </div>
            <CardDescription>
              Configures automatic retry scheduling behavior before routing to Dead Letter Queue (DLQ)
            </CardDescription>
          </CardHeader>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-300">Default Max Retries</label>
              <Input
                type="number"
                min={0}
                max={10}
                value={formData.defaultMaxRetries}
                onChange={(e) => handleChange('defaultMaxRetries', Number(e.target.value))}
              />
              <span className="text-[11px] text-slate-500">Number of retries allowed</span>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-300">Base Backoff (ms)</label>
              <Input
                type="number"
                min={100}
                step={500}
                value={formData.backoffBaseMs}
                onChange={(e) => handleChange('backoffBaseMs', Number(e.target.value))}
              />
              <span className="text-[11px] text-slate-500">Initial delay for attempt 1</span>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-300">Backoff Multiplier</label>
              <Input
                type="number"
                min={1.0}
                max={5.0}
                step={0.5}
                value={formData.backoffMultiplier}
                onChange={(e) => handleChange('backoffMultiplier', Number(e.target.value))}
              />
              <span className="text-[11px] text-slate-500">Exponential growth factor</span>
            </div>
          </div>
        </Card>

        {/* Worker Execution & Concurrency */}
        <Card className="p-6 space-y-5">
          <CardHeader className="p-0">
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm">Execution Watchdog & Concurrency</CardTitle>
            </div>
            <CardDescription>
              Job timeouts and worker thread pool boundaries
            </CardDescription>
          </CardHeader>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label className="text-xs font-medium text-slate-300">Job Execution Timeout (Seconds)</label>
                <span className="text-[10px] font-mono text-emerald-400">Dynamic</span>
              </div>
              <Input
                type="number"
                min={5}
                max={3600}
                value={formData.jobTimeoutSeconds}
                onChange={(e) => handleChange('jobTimeoutSeconds', Number(e.target.value))}
              />
              <span className="text-[11px] text-slate-500">
                Jobs exceeding this duration are aborted and marked timed out
              </span>
            </div>

            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label className="text-xs font-medium text-slate-300">Worker Concurrency (Threads)</label>
                <span className="text-[10px] font-mono text-amber-400 bg-amber-950/60 px-1.5 py-0.5 rounded border border-amber-800/50">
                  Requires Restart
                </span>
              </div>
              <Input
                type="number"
                min={1}
                max={64}
                value={formData.workerConcurrency}
                onChange={(e) => handleChange('workerConcurrency', Number(e.target.value))}
              />
              <span className="text-[11px] text-slate-500">
                Active execution threads per worker node instance
              </span>
            </div>
          </div>
        </Card>

        {/* Dashboard & Telemetry */}
        <Card className="p-6 space-y-5">
          <CardHeader className="p-0">
            <CardTitle className="text-sm">Telemetry & Stream Retention</CardTitle>
            <CardDescription>
              Client polling interval and stream trimming limit
            </CardDescription>
          </CardHeader>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-300">Dashboard Refresh Interval (ms)</label>
              <Input
                type="number"
                min={1000}
                max={60000}
                step={500}
                value={formData.dashboardRefreshIntervalMs}
                onChange={(e) => handleChange('dashboardRefreshIntervalMs', Number(e.target.value))}
              />
              <span className="text-[11px] text-slate-500">Live query polling rate</span>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-300">Stream Retention Limit (MAXLEN)</label>
              <Input
                type="number"
                min={1000}
                step={5000}
                value={formData.streamRetentionMaxLen}
                onChange={(e) => handleChange('streamRetentionMaxLen', Number(e.target.value))}
              />
              <span className="text-[11px] text-slate-500">Max historical records retained in Redis Streams</span>
            </div>
          </div>
        </Card>

        {/* Action Button */}
        <div className="flex justify-end">
          <Button type="submit" isLoading={isUpdating} className="px-6">
            <Save className="w-4 h-4 mr-1.5" />
            <span>Save Configuration</span>
          </Button>
        </div>
      </form>
    </div>
  );
};
