import { cn } from '@/utils/cn'

/**
 * Underlined tab strip. Controlled: pass the active `value` and an `onChange(key)`.
 * tabs: [{ key, label, count? }]
 */
export function Tabs({ tabs, value, onChange, className }) {
  return (
    <div role="tablist" className={cn('flex gap-1 overflow-x-auto border-b border-border', className)}>
      {tabs.map((tab) => {
        const active = tab.key === value
        return (
          <button
            key={tab.key}
            type="button"
            role="tab"
            aria-selected={active}
            onClick={() => onChange(tab.key)}
            className={cn(
              '-mb-px flex items-center gap-2 whitespace-nowrap border-b-2 px-4 py-2.5 text-sm font-medium transition-colors',
              active
                ? 'border-accent text-accent'
                : 'border-transparent text-text-secondary hover:text-text-primary'
            )}
          >
            {tab.label}
            {tab.count !== undefined && tab.count !== null && (
              <span className="rounded-full bg-surface-raised px-2 py-0.5 text-xs font-medium text-text-secondary">
                {tab.count}
              </span>
            )}
          </button>
        )
      })}
    </div>
  )
}
