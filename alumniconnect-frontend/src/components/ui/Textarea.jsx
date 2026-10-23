import { forwardRef } from 'react'
import { cn } from '@/utils/cn'

export const Textarea = forwardRef(function Textarea(
  { label, error, helperText, id, className, rows = 4, ...props },
  ref
) {
  const textareaId = id ?? props.name

  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label htmlFor={textareaId} className="text-sm font-medium text-text-secondary">
          {label}
        </label>
      )}
      <textarea
        ref={ref}
        id={textareaId}
        rows={rows}
        className={cn(
          'rounded-md border bg-surface-sunken px-3 py-2 text-sm text-text-primary placeholder:text-text-tertiary',
          'resize-y transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/50',
          'disabled:cursor-not-allowed disabled:bg-surface-raised disabled:text-text-tertiary',
          error ? 'border-danger' : 'border-border focus:border-accent',
          className
        )}
        {...props}
      />
      {error ? (
        <p className="text-xs text-danger">{error}</p>
      ) : helperText ? (
        <p className="text-xs text-text-tertiary">{helperText}</p>
      ) : null}
    </div>
  )
})
