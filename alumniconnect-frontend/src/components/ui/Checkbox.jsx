import { forwardRef } from 'react'
import { cn } from '@/utils/cn'

/** A labelled checkbox. Use <Toggle> for an on/off setting, <Checkbox> for a choice in a form or filter. */
export const Checkbox = forwardRef(function Checkbox({ label, description, error, id, className, ...props }, ref) {
  const inputId = id ?? props.name
  return (
    <div className={cn('flex flex-col gap-1', className)}>
      <div className="flex items-start gap-2.5">
        <input
          ref={ref}
          id={inputId}
          type="checkbox"
          className="mt-0.5 size-4 shrink-0 rounded border-border accent-accent disabled:cursor-not-allowed"
          {...props}
        />
        {(label || description) && (
          <label htmlFor={inputId} className="min-w-0 cursor-pointer text-sm">
            {label && <span className="font-medium text-text-primary">{label}</span>}
            {description && <span className="block text-xs text-text-secondary">{description}</span>}
          </label>
        )}
      </div>
      {error && <p className="text-xs text-danger">{error}</p>}
    </div>
  )
})
