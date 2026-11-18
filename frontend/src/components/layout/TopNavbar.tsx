import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Plus, ExternalLink, RefreshCw } from 'lucide-react';
import { Button } from '../ui/Button';
import { useQuery } from '@tanstack/react-query';
import { api } from '../../services/api';

export const TopNavbar: React.FC = () => {
  const navigate = useNavigate();
  const [searchInput, setSearchInput] = useState('');

  const { data: health, isLoading } = useQuery({
    queryKey: ['system-health'],
    queryFn: () => api.getHealth(),
    refetchInterval: 10000,
  });

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchInput.trim()) {
      navigate(`/jobs?search=${encodeURIComponent(searchInput.trim())}`);
    }
  };

  const isUp = health?.status === 'UP';

  return (
    <header className="h-16 border-b border-slate-800/80 bg-[#0d1321]/90 backdrop-blur-md px-6 flex items-center justify-between sticky top-0 z-20">
      {/* Quick Search */}
      <form onSubmit={handleSearchSubmit} className="relative w-80">
        <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          placeholder="Search jobs by ID or name..."
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          className="w-full bg-slate-900/90 border border-slate-700/60 rounded-lg pl-9 pr-3 py-1.5 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 font-mono transition-all"
        />
      </form>

      {/* Right Controls */}
      <div className="flex items-center gap-3">
        {/* Cluster Health Pill */}
        <div className="flex items-center gap-2 px-3 py-1 rounded-full bg-slate-900 border border-slate-800 text-xs font-mono">
          <span
            className={`w-2 h-2 rounded-full ${
              isLoading ? 'bg-amber-400' : isUp ? 'bg-emerald-400' : 'bg-rose-500'
            } animate-pulse`}
          />
          <span className="text-slate-300">
            {isLoading ? 'Checking...' : isUp ? 'All Systems Operational' : 'Cluster Degraded'}
          </span>
        </div>

        {/* Swagger Docs Link */}
        <a
          href="/swagger-ui.html"
          target="_blank"
          rel="noopener noreferrer"
          className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-slate-800 text-xs font-medium text-slate-300 hover:text-white hover:bg-slate-800/60 transition-colors"
          title="Open Swagger OpenAPI Documentation"
        >
          <span>OpenAPI Docs</span>
          <ExternalLink className="w-3 h-3 text-slate-400" />
        </a>

        {/* Create Job CTA */}
        <Button size="sm" onClick={() => navigate('/jobs/new')}>
          <Plus className="w-4 h-4 mr-1" />
          <span>New Job</span>
        </Button>
      </div>
    </header>
  );
};
