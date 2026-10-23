import { cn } from '@/utils/cn'

export function Card({ className, children, ...props }) {
  return (
    <div
      className={cn('rounded-lg border border-border bg-surface shadow-sm', className)}
      {...props}
    >
      {children}
    </div>
  )
}

export function CardHeader({ className, children, ...props }) {
  return (
    <div className={cn('flex items-start justify-between gap-3 border-b border-border-soft px-5 py-4', className)} {...props}>
      {children}
    </div>
  )
}

export function CardTitle({ className, children, ...props }) {
  return (
    <h3 className={cn('font-display text-base font-medium text-text-primary', className)} {...props}>
      {children}
    </h3>
  )
}

export function CardBody({ className, children, ...props }) {
  return (
    <div className={cn('px-5 py-4', className)} {...props}>
      {children}
    </div>
  )
}

export function CardFooter({ className, children, ...props }) {
  return (
    <div className={cn('flex items-center justify-between gap-3 border-t border-border-soft px-5 py-3', className)} {...props}>
      {children}
    </div>
  )
}
