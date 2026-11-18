import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  ListTodo,
  PlusCircle,
  Cpu,
  Layers,
  BarChart3,
  Sliders,
  Activity,
  Terminal
} from 'lucide-react';
import { cn } from '../../lib/utils';

export const Sidebar: React.FC = () => {
  const navItems = [
    { to: '/', label: 'Overview', icon: LayoutDashboard },
    { to: '/jobs', label: 'Jobs Management', icon: ListTodo },
    { to: '/jobs/new', label: 'Create Job', icon: PlusCircle },
    { to: '/workers', label: 'Workers', icon: Cpu },
    { to: '/queue', label: 'Queue Telemetry', icon: Layers },
    { to: '/analytics', label: 'Analytics', icon: BarChart3 },
    { to: '/settings', label: 'Settings', icon: Sliders },
  ];

  return (
    <aside className="w-64 bg-[#0d1321] border-r border-slate-800/80 flex flex-col shrink-0 h-screen sticky top-0">
      {/* Brand Header */}
      <div className="h-16 flex items-center gap-3 px-5 border-b border-slate-800/80">
        <div className="w-8 h-8 rounded-lg bg-blue-600/20 border border-blue-500/40 flex items-center justify-center text-blue-400">
          <Activity className="w-5 h-5 animate-pulse" />
        </div>
        <div>
          <span className="font-bold text-sm tracking-tight text-white block">JobDS Core</span>
          <span className="text-[10px] text-slate-400 font-mono tracking-wider uppercase block">Distributed Engine</span>
        </div>
      </div>

      {/* Nav List */}
      <nav className="flex-1 px-3 py-4 space-y-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-medium transition-all group',
                  isActive
                    ? 'bg-blue-600/15 text-blue-400 border border-blue-500/30 font-semibold'
                    : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/50'
                )
              }
            >
              <Icon className="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      {/* Cluster Footer Info */}
      <div className="p-4 border-t border-slate-800/80 bg-[#090e17]">
        <div className="flex items-center gap-2 mb-2">
          <div className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
          <span className="text-xs font-medium text-slate-300">Cluster Connected</span>
        </div>
        <div className="space-y-1 text-[11px] text-slate-400 font-mono">
          <div className="flex justify-between">
            <span>Engine:</span>
            <span className="text-slate-200">Redis Streams</span>
          </div>
          <div className="flex justify-between">
            <span>Store:</span>
            <span className="text-slate-200">MongoDB 7</span>
          </div>
        </div>
      </div>
    </aside>
  );
};
