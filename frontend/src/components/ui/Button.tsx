import React from 'react';
import { cn } from '../../lib/utils';
import { Loader2 } from 'lucide-react';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg' | 'icon';
  isLoading?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  className,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  disabled,
  children,
  ...props
}) => {
  const baseStyles = 'inline-flex items-center justify-center font-medium rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 disabled:pointer-events-none disabled:opacity-50 cursor-pointer select-none';

  const variants = {
    primary: 'bg-blue-600 text-white hover:bg-blue-500 shadow-sm active:bg-blue-700',
    secondary: 'bg-slate-800 text-slate-100 hover:bg-slate-700 border border-slate-700/80 active:bg-slate-800',
    danger: 'bg-rose-600/90 text-white hover:bg-rose-500 active:bg-rose-700',
    outline: 'border border-slate-700 text-slate-300 hover:bg-slate-800/80 hover:text-white',
    ghost: 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/50',
  };

  const sizes = {
    sm: 'h-8 px-3 text-xs gap-1.5',
    md: 'h-9 px-4 text-sm gap-2',
    lg: 'h-11 px-6 text-base gap-2.5',
    icon: 'h-9 w-9 p-0',
  };

  return (
    <button
      className={cn(baseStyles, variants[variant], sizes[size], className)}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading && <Loader2 className="w-4 h-4 animate-spin shrink-0" />}
      {children}
    </button>
  );
};
