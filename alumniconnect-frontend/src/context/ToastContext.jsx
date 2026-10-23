import { createContext, useCallback, useMemo, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react'
import { cn } from '@/utils/cn'

export const ToastContext = createContext(null)

const TONES = {
  success: { classes: 'border-success/30', Icon: CheckCircle2, iconClass: 'text-success' },
  danger: { classes: 'border-danger/30', Icon: AlertCircle, iconClass: 'text-danger' },
  info: { classes: 'border-info/30', Icon: Info, iconClass: 'text-info' },
}

/**
 * Small transient messages ("Profile saved", "Couldn't send that"). Use through
 * `useToast()`: toast.success(msg), toast.error(msg), toast.info(msg).
 */
export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])
  const nextId = useRef(0)

  const dismiss = useCallback((id) => {
    setToasts((current) => current.filter((toast) => toast.id !== id))
  }, [])

  const push = useCallback(
    (tone, message) => {
      nextId.current += 1
      const id = nextId.current
      setToasts((current) => [...current.slice(-3), { id, tone, message }])
      setTimeout(() => dismiss(id), tone === 'danger' ? 6500 : 3500)
    },
    [dismiss]
  )

  const api = useMemo(
    () => ({
      success: (message) => push('success', message),
      error: (message) => push('danger', message),
      info: (message) => push('info', message),
    }),
    [push]
  )

  return (
    <ToastContext.Provider value={api}>
      {children}
      {createPortal(
        <div
          className="pointer-events-none fixed inset-x-0 bottom-0 z-[60] flex flex-col items-center gap-2 p-4 sm:items-end"
          aria-live="polite"
        >
          {toasts.map(({ id, tone, message }) => {
            const { classes, Icon, iconClass } = TONES[tone] ?? TONES.info
            return (
              <div
                key={id}
                className={cn(
                  'pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-md border bg-surface px-3.5 py-3 text-sm shadow-lg',
                  classes
                )}
              >
                <Icon className={cn('mt-0.5 size-4 shrink-0', iconClass)} aria-hidden />
                <p className="min-w-0 flex-1 text-text-primary">{message}</p>
                <button
                  type="button"
                  onClick={() => dismiss(id)}
                  aria-label="Dismiss"
                  className="text-text-tertiary hover:text-text-primary"
                >
                  <X className="size-4" />
                </button>
              </div>
            )
          })}
        </div>,
        document.body
      )}
    </ToastContext.Provider>
  )
}
