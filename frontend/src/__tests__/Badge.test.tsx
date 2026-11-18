import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StatusBadge, PriorityBadge } from '../components/ui/Badge';

describe('Badge Components', () => {
  it('renders COMPLETED status badge with correct text', () => {
    render(<StatusBadge status="COMPLETED" />);
    expect(screen.getByText('COMPLETED')).toBeInTheDocument();
  });

  it('renders FAILED status badge with correct text', () => {
    render(<StatusBadge status="FAILED" />);
    expect(screen.getByText('FAILED')).toBeInTheDocument();
  });

  it('renders CRITICAL priority badge', () => {
    render(<PriorityBadge priority="CRITICAL" />);
    expect(screen.getByText('CRITICAL')).toBeInTheDocument();
  });

  it('renders NORMAL priority badge', () => {
    render(<PriorityBadge priority="NORMAL" />);
    expect(screen.getByText('NORMAL')).toBeInTheDocument();
  });
});
