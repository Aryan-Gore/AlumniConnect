import { cn } from '@/utils/cn'
import { STATUS_TONE } from '@/utils/constants'

const TONE_CLASSES = {
  success: 'bg-success-soft text-success',
  warning: 'bg-warning-soft text-warning',
  danger: 'bg-danger-soft text-danger',
  info: 'bg-info-soft text-info',
  muted: 'bg-surface-raised text-text-tertiary',
}

export function StatusBadge({ value, className }) {
  const tone = STATUS_TONE[value] ?? 'muted'
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-sm px-2 py-0.5 font-mono text-[11px] font-medium uppercase tracking-wider',
        TONE_CLASSES[tone],
        className
      )}
    >
      {value}
    </span>
  )
}

// Generic badge for non-status labels (skills, tags, counts).
export function Badge({ children, className, variant = 'default' }) {
  const variants = {
    default: 'bg-surface-raised text-text-secondary border border-border',
    accent: 'bg-accent-soft text-accent border border-accent/30',
  }
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-sm px-2 py-0.5 text-xs font-medium',
        variants[variant],
        className
      )}
    >
      {children}
    </span>
  )
}
