import { BadgeCheck } from 'lucide-react'
import { cn } from '@/utils/cn'

/** The check mark shown next to alumni whose record the alumni office has verified. */
export function VerifiedBadge({ className, size = 'size-4' }) {
  return (
    <span title="Verified by the alumni office" className={cn('inline-flex shrink-0 text-accent', className)}>
      <BadgeCheck className={size} aria-hidden />
      <span className="sr-only">Verified by the alumni office</span>
    </span>
  )
}
