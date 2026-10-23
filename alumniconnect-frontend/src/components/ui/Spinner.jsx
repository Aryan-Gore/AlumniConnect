import { Loader2 } from 'lucide-react'
import { cn } from '@/utils/cn'

export function Spinner({ label, className }) {
  return (
    <div className={cn('flex flex-col items-center gap-2 text-text-secondary', className)}>
      <Loader2 className="size-5 animate-spin text-accent" aria-hidden />
      {label && <span className="text-sm">{label}</span>}
    </div>
  )
}
