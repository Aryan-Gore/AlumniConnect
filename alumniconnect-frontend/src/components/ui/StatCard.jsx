import { cn } from '@/utils/cn'
import { formatCount } from '@/utils/formatters'

export function StatCard({ label, value, icon: Icon, trend, className }) {
  return (
    <div className={cn('rounded-lg border border-border bg-surface px-5 py-4', className)}>
      <div className="flex items-center justify-between">
        <span className="text-xs font-medium uppercase tracking-wider text-text-tertiary">{label}</span>
        {Icon && <Icon className="size-4 text-accent" aria-hidden />}
      </div>
      <div className="mt-2 flex items-baseline gap-2">
        <span className="font-mono text-2xl font-semibold text-text-primary tabular-nums">
          {typeof value === 'number' ? formatCount(value) : value}
        </span>
        {trend && (
          <span className={cn('text-xs font-medium', trend.direction === 'up' ? 'text-success' : 'text-danger')}>
            {trend.direction === 'up' ? '↑' : '↓'} {trend.label}
          </span>
        )}
      </div>
    </div>
  )
}
