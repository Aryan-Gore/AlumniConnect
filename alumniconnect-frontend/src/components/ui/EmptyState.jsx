import { cn } from '@/utils/cn'

export function EmptyState({ icon: Icon, title, description, action, className }) {
  return (
    <div className={cn('flex flex-col items-center justify-center gap-3 px-6 py-14 text-center', className)}>
      {Icon && (
        <div className="flex size-11 items-center justify-center rounded-full bg-surface-raised text-text-tertiary">
          <Icon className="size-5" aria-hidden />
        </div>
      )}
      <div className="space-y-1">
        <p className="font-display text-base text-text-primary">{title}</p>
        {description && <p className="max-w-sm text-sm text-text-secondary">{description}</p>}
      </div>
      {action}
    </div>
  )
}
