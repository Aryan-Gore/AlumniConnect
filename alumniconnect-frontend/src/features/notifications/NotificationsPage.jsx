import { useEffect, useState } from 'react'
import { Bell, BriefcaseIcon, CalendarDays, Handshake, Megaphone, ShieldCheck } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { EmptyState } from '@/components/ui/EmptyState'
import { notificationsApi } from '@/api/notificationsApi'
import { timeAgo } from '@/utils/formatters'
import { cn } from '@/utils/cn'
import { Spinner } from '@/components/ui/Spinner'

const CATEGORY_ICON = {
  MENTORSHIP: Handshake,
  EVENT: CalendarDays,
  ANNOUNCEMENT: Megaphone,
  JOB: BriefcaseIcon,
  ACCOUNT: ShieldCheck,
  POST: Bell,
  MESSAGE: Bell,
}

export function NotificationsPage() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const unreadCount = notifications.filter((n) => !n.read).length

  useEffect(() => {
    notificationsApi.list()
      .then((data) => setNotifications(data ?? []))
      .finally(() => setLoading(false))
  }, [])

  async function markAllRead() {
    await notificationsApi.markAllRead().catch(() => {})
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })))
  }

  async function markRead(id) {
    await notificationsApi.markRead(id).catch(() => {})
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)))
  }

  return (
    <div>
      <PageHeader
        title="Notifications"
        description={unreadCount > 0 ? `${unreadCount} unread` : 'All caught up.'}
        action={
          unreadCount > 0 ? (
            <Button variant="secondary" size="sm" onClick={markAllRead}>
              Mark all as read
            </Button>
          ) : null
        }
      />

      {loading ? <div className="flex justify-center py-8"><Spinner /></div> : notifications.length === 0 ? (
        <Card>
          <EmptyState icon={Bell} title="No notifications yet" description="You'll see mentorship, event, and announcement updates here." />
        </Card>
      ) : (
        <Card className="divide-y divide-border-soft overflow-hidden">
          {notifications.map((note) => {
            const Icon = CATEGORY_ICON[note.category] ?? Bell
            return (
              <button
                key={note.id}
                onClick={() => markRead(note.id)}
                className={cn(
                  'flex w-full items-start gap-3 px-5 py-4 text-left transition-colors hover:bg-surface-raised',
                  !note.read && 'bg-accent-soft/40'
                )}
              >
                <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-surface-raised text-text-secondary">
                  <Icon className="size-4" />
                </div>
                <div className="flex-1">
                  <p className={cn('text-sm', note.read ? 'text-text-secondary' : 'font-medium text-text-primary')}>
                    {note.message}
                  </p>
                  <p className="mt-0.5 text-xs text-text-tertiary">
                    {note.category} · {timeAgo(note.createdAt)}
                  </p>
                </div>
                {!note.read && <span className="mt-1.5 size-2 shrink-0 rounded-full bg-accent" />}
              </button>
            )
          })}
        </Card>
      )}
    </div>
  )
}
