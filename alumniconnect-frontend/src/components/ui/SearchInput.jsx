import { forwardRef } from 'react'
import { Search, X } from 'lucide-react'
import { cn } from '@/utils/cn'

/**
 * A search box with a magnifier and a clear button.
 * Controlled, and — unlike <Input> — `onChange` receives the new *string*, not the event.
 * Pair it with useDebouncedValue() before sending the text to the API.
 */
export const SearchInput = forwardRef(function SearchInput(
  { value, onChange, placeholder = 'Search', label, className, inputClassName, ...props },
  ref
) {
  const text = value ?? ''
  return (
    <div className={cn('relative', className)}>
      <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-text-tertiary" aria-hidden />
      <input
        ref={ref}
        type="search"
        value={text}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        aria-label={label ?? placeholder}
        className={cn(
          'h-10 w-full rounded-md border border-border bg-surface-sunken pl-9 pr-9 text-sm text-text-primary placeholder:text-text-tertiary',
          'transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/50 focus:border-accent',
          '[&::-webkit-search-cancel-button]:appearance-none',
          inputClassName
        )}
        {...props}
      />
      {text && (
        <button
          type="button"
          onClick={() => onChange('')}
          aria-label="Clear search"
          className="absolute right-2 top-1/2 flex size-6 -translate-y-1/2 items-center justify-center rounded text-text-tertiary hover:bg-surface-raised hover:text-text-primary"
        >
          <X className="size-3.5" aria-hidden />
        </button>
      )}
    </div>
  )
})
