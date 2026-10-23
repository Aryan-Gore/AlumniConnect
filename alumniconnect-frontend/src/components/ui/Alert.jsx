import { AlertCircle, CheckCircle2, Info, TriangleAlert } from 'lucide-react'
import { cn } from '@/utils/cn'

const TONES = {
  danger: { classes: 'border-danger/30 bg-danger-soft text-danger', Icon: AlertCircle },
  warning: { classes: 'border-warning/30 bg-warning-soft text-warning', Icon: TriangleAlert },
  success: { classes: 'border-success/30 bg-success-soft text-success', Icon: CheckCircle2 },
  info: { classes: 'border-info/30 bg-info-soft text-info', Icon: Info },
}

/** Inline banner for form errors, success confirmations and gentle warnings. */
export function Alert({ tone = 'danger', title, children, className }) {
  const { classes, Icon } = TONES[tone] ?? TONES.danger
  return (
    <div role={tone === 'danger' ? 'alert' : 'status'} className={cn('flex gap-3 rounded-md border px-3.5 py-3 text-sm', classes, className)}>
      <Icon className="mt-0.5 size-4 shrink-0" aria-hidden />
      <div className="min-w-0">
        {title && <p className="font-medium">{title}</p>}
        {children && <div className={cn(title && 'mt-0.5', 'text-[13px] leading-relaxed')}>{children}</div>}
      </div>
    </div>
  )
}
