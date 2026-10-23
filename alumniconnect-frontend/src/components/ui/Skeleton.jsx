import { cn } from '@/utils/cn'

/** A pulsing placeholder block: <Skeleton className="h-4 w-40" />. Decorative, so hidden from screen readers. */
export function Skeleton({ className }) {
  return <div aria-hidden="true" className={cn('animate-pulse rounded-md bg-surface-raised', className)} />
}
