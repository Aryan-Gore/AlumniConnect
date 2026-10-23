import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from './Button'
import { cn } from '@/utils/cn'

/**
 * Prev / next control for the backend's PageResponse (`page` is 0-based).
 * Renders nothing when everything fits on one page.
 */
export function Pagination({ page = 0, totalPages = 1, totalElements, onPageChange, className }) {
  if (totalPages <= 1) return null

  return (
    <div className={cn('flex items-center justify-between gap-3 pt-4', className)}>
      <p className="text-sm text-text-secondary">
        Page <span className="font-mono tabular-nums text-text-primary">{page + 1}</span> of{' '}
        <span className="font-mono tabular-nums text-text-primary">{totalPages}</span>
        {typeof totalElements === 'number' && <span className="text-text-tertiary"> · {totalElements} total</span>}
      </p>
      <div className="flex items-center gap-2">
        <Button
          variant="secondary"
          size="sm"
          disabled={page <= 0}
          onClick={() => onPageChange(page - 1)}
          leftIcon={<ChevronLeft className="size-4" aria-hidden />}
        >
          Previous
        </Button>
        <Button
          variant="secondary"
          size="sm"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
          rightIcon={<ChevronRight className="size-4" aria-hidden />}
        >
          Next
        </Button>
      </div>
    </div>
  )
}
