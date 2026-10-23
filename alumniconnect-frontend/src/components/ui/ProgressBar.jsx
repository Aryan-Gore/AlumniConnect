import { cn } from '@/utils/cn'

/** A thin horizontal meter, 0–100. Turns green when complete. */
export function ProgressBar({ value = 0, label, showValue = true, className }) {
  const percent = Math.max(0, Math.min(100, Math.round(Number(value) || 0)))

  return (
    <div className={cn('w-full', className)}>
      {(label || showValue) && (
        <div className="mb-1.5 flex items-center justify-between text-sm">
          {label && <span className="font-medium text-text-secondary">{label}</span>}
          {showValue && <span className="font-mono text-text-primary tabular-nums">{percent}%</span>}
        </div>
      )}
      <div
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={percent}
        aria-label={label ?? 'Progress'}
        className="h-2 w-full overflow-hidden rounded-full bg-surface-raised"
      >
        <div
          className={cn('h-full rounded-full transition-all duration-500', percent >= 100 ? 'bg-success' : 'bg-accent')}
          style={{ width: `${percent}%` }}
        />
      </div>
    </div>
  )
}
