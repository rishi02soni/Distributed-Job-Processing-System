import React from 'react';
import { cn } from '../../lib/utils';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  error?: string;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, error, ...props }, ref) => {
    return (
      <div className="w-full">
        <input
          type={type}
          className={cn(
            'flex h-9 w-full rounded-lg border border-slate-700/80 bg-slate-900/90 px-3 py-1 text-sm text-slate-100 shadow-sm transition-colors',
            'file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-slate-500',
            'focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-blue-500 focus-visible:border-blue-500',
            'disabled:cursor-not-allowed disabled:opacity-50',
            error && 'border-rose-500 focus-visible:ring-rose-500',
            className
          )}
          ref={ref}
          {...props}
        />
        {error && <p className="mt-1 text-xs text-rose-400">{error}</p>}
      </div>
    );
  }
);

Input.displayName = 'Input';
