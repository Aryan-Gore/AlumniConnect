import { forwardRef } from 'react'
import { cn } from '@/utils/cn'

export const Input = forwardRef(function Input(
  { label, error, helperText, id, className, ...props },
  ref
) {
  const inputId = id ?? props.name

  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label htmlFor={inputId} className="text-sm font-medium text-text-secondary">
          {label}
        </label>
      )}
      <input
        ref={ref}
        id={inputId}
        className={cn(
          'h-10 rounded-md border bg-surface-sunken px-3 text-sm text-text-primary placeholder:text-text-tertiary',
          'transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/50',
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

export const Select = forwardRef(function Select(
  { label, error, helperText, id, className, children, ...props },
  ref
) {
  const selectId = id ?? props.name
  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label htmlFor={selectId} className="text-sm font-medium text-text-secondary">
          {label}
        </label>
      )}
      <select
        ref={ref}
        id={selectId}
        className={cn(
          'h-10 rounded-md border bg-surface-sunken px-3 text-sm text-text-primary',
          'transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/50',
          error ? 'border-danger' : 'border-border focus:border-accent',
          className
        )}
        {...props}
      >
        {children}
      </select>
      {error ? (
        <p className="text-xs text-danger">{error}</p>
      ) : helperText ? (
        <p className="text-xs text-text-tertiary">{helperText}</p>
      ) : null}
    </div>
  )
})
