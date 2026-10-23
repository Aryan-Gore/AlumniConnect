import { useEffect, useState } from 'react'
import { CalendarDays, Check, MapPin, Users2 } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card, CardBody, CardFooter } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { eventsApi } from '@/api/eventsApi'
import { formatDate } from '@/utils/formatters'
import { Spinner } from '@/components/ui/Spinner'

export function EventsPage() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    eventsApi.list().then((data) => setEvents(data ?? [])).finally(() => setLoading(false))
  }, [])

  async function toggleRegistration(ev) {
    const wasRegistered = ev.registeredByViewer
    // optimistic
    setEvents((prev) => prev.map((e) => e.id === ev.id
      ? { ...e, registeredByViewer: !wasRegistered, registeredCount: e.registeredCount + (wasRegistered ? -1 : 1) }
      : e))
    try {
      wasRegistered ? await eventsApi.cancelRegistration(ev.id) : await eventsApi.register(ev.id)
    } catch {
      // revert
      setEvents((prev) => prev.map((e) => e.id === ev.id
        ? { ...e, registeredByViewer: wasRegistered, registeredCount: e.registeredCount + (wasRegistered ? 1 : -1) }
        : e))
    }
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  return (
    <div>
      <PageHeader title="Events" description="Reunions, career talks, workshops, and webinars." />

      <div className="grid gap-4 sm:grid-cols-2">
        {events.map((event) => (
          <Card key={event.id} className="flex flex-col">
            <CardBody className="flex-1">
              <div className="flex items-start justify-between gap-3">
                <p className="font-display text-base font-medium text-text-primary">{event.title}</p>
                <Badge variant={event.mode === 'ONLINE' ? 'default' : 'accent'}>{event.mode}</Badge>
              </div>
              <p className="mt-2 text-sm text-text-secondary">{event.description}</p>

              <div className="mt-4 space-y-1.5 text-xs text-text-tertiary">
                <p className="flex items-center gap-1.5">
                  <CalendarDays className="size-3.5" /> {formatDate(event.eventDate ?? event.date)}
                </p>
                {event.location && (
                  <p className="flex items-center gap-1.5">
                    <MapPin className="size-3.5" /> {event.location}
                  </p>
                )}
                <p className="flex items-center gap-1.5">
                  <Users2 className="size-3.5" /> {event.registeredCount} / {event.capacity} registered
                </p>
              </div>
            </CardBody>
            <CardFooter>
              <div className="h-1.5 w-24 overflow-hidden rounded-full bg-surface-raised">
                <div
                  className="h-full bg-accent"
                  style={{ width: `${Math.min(100, (event.registeredCount / event.capacity) * 100)}%` }}
                />
              </div>
              <Button
                size="sm"
                variant={event.registeredByViewer ? 'secondary' : 'primary'}
                leftIcon={event.registeredByViewer ? <Check className="size-3.5" /> : null}
                onClick={() => toggleRegistration(event)}
              >
                {event.registeredByViewer ? 'Registered' : 'Register for Event'}
              </Button>
            </CardFooter>
          </Card>
        ))}
      </div>
    </div>
  )
}
