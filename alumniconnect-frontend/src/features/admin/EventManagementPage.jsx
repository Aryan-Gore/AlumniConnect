import { useEffect, useState } from 'react'
import { CalendarDays, Plus, Users2, X } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { StatusBadge } from '@/components/ui/Badge'
import { eventsApi } from '@/api/eventsApi'
import { formatDate } from '@/utils/formatters'
import { Spinner } from '@/components/ui/Spinner'

export function EventManagementPage() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    eventsApi.adminList()
      .then((data) => setEvents(data ?? []))
      .catch(() => setEvents([]))
      .finally(() => setLoading(false))
  }, [])

  async function setStatus(id, status) {
    await eventsApi[status === 'PUBLISHED' ? 'publish' : 'cancel']?.(id).catch(() => {})
    setEvents((prev) => prev.map((e) => e.id === id ? { ...e, status } : e))
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  return (
    <div>
      <PageHeader
        title="Event management"
        description="Create, publish, and track institutional events."
        action={
          <Button size="sm" leftIcon={<Plus className="size-4" />}>Create event</Button>
        }
      />

      <Card className="overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-border-soft bg-surface-raised text-xs uppercase tracking-wider text-text-tertiary">
              <th className="px-5 py-3 font-medium">Event</th>
              <th className="px-5 py-3 font-medium">Date</th>
              <th className="px-5 py-3 font-medium">Registrations</th>
              <th className="px-5 py-3 font-medium">Status</th>
              <th className="px-5 py-3 text-right font-medium">Actions</th>
            </tr>
          </thead>
          <tbody>
            {events.map((event) => (
              <tr key={event.id} className="border-b border-border-soft last:border-0 hover:bg-surface-raised/60">
                <td className="px-5 py-3.5">
                  <p className="font-medium text-text-primary">{event.title}</p>
                  <p className="text-xs text-text-tertiary">
                    {event.mode} · {event.location}
                  </p>
                </td>
                <td className="px-5 py-3.5 font-mono text-xs text-text-secondary">
                  {formatDate(event.eventDate ?? event.date)}
                </td>
                <td className="px-5 py-3.5">
                  <span className="flex items-center gap-1.5 text-text-secondary">
                    <Users2 className="size-3.5 text-text-tertiary" />
                    {event.registeredCount ?? 0} / {event.capacity ?? '–'}
                  </span>
                </td>
                <td className="px-5 py-3.5">
                  <StatusBadge value={
                    event.status === 'DRAFT' ? 'PENDING' :
                    event.status === 'PUBLISHED' ? 'ACTIVE' : 'INACTIVE'
                  } />
                </td>
                <td className="px-5 py-3.5">
                  <div className="flex justify-end gap-1">
                    {event.status === 'DRAFT' && (
                      <Button size="sm" variant="ghost" onClick={() => setStatus(event.id, 'PUBLISHED')}>
                        Publish
                      </Button>
                    )}
                    {event.status === 'PUBLISHED' && (
                      <Button size="sm" variant="ghost" leftIcon={<X className="size-3.5" />} onClick={() => setStatus(event.id, 'CANCELLED')}>
                        Cancel
                      </Button>
                    )}
                    {event.status === 'CANCELLED' && (
                      <span className="text-xs text-text-tertiary">No actions</span>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>

      <p className="mt-4 flex items-center gap-1.5 text-xs text-text-tertiary">
        <CalendarDays className="size-3.5" /> Status pills reuse the alumni-record palette: Draft reads as Pending, Published as Active, Cancelled as Inactive.
      </p>
    </div>
  )
}
