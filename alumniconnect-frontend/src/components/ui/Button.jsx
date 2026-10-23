import { Loader2 } from 'lucide-react'
import { cn } from '@/utils/cn'

const VARIANTS = {
  primary:
    'bg-accent text-white hover:bg-accent-hover disabled:bg-accent/50',
  secondary:
    'bg-white text-text-primary border border-border hover:bg-surface-raised hover:border-text-tertiary disabled:opacity-50',
  ghost:
    'bg-transparent text-text-secondary hover:text-text-primary hover:bg-surface-raised disabled:opacity-50',
  danger:
    'bg-danger text-white hover:bg-danger/90 disabled:opacity-50',
}

const SIZES = {
  sm: 'h-8 px-3 text-sm gap-1.5',
  md: 'h-10 px-4 text-sm gap-2',
  lg: 'h-12 px-6 text-base gap-2',
}

export function Button({
  as: Component = 'button',
  variant = 'primary',
  size = 'md',
  isLoading = false,
  leftIcon = null,
  rightIcon = null,
  className,
  children,
  disabled,
  ...props
}) {
  return (
    <Component
      className={cn(
        'inline-flex items-center justify-center rounded-md font-medium tracking-wide transition-colors duration-150',
        'focus-visible:outline-none disabled:cursor-not-allowed',
        VARIANTS[variant],
        SIZES[size],
        className
      )}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading ? <Loader2 className="size-4 animate-spin" aria-hidden /> : leftIcon}
      {children}
      {!isLoading && rightIcon}
    </Component>
  )
}
