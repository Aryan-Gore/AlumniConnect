import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Bell, Briefcase, CalendarDays, Handshake } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card, CardHeader, CardTitle, CardBody, CardFooter } from '@/components/ui/Card'
import { StatusBadge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { useAuth } from '@/hooks/useAuth'
import { eventsApi } from '@/api/eventsApi'
import { jobsApi } from '@/api/jobsApi'
import { mentorshipApi } from '@/api/mentorshipApi'
import { notificationsApi } from '@/api/notificationsApi'
import { initials, formatDate, timeAgo } from '@/utils/formatters'
import { ROUTES } from '@/utils/constants'
import { Spinner } from '@/components/ui/Spinner'

export function AlumniDashboard() {
  const { user } = useAuth()
  const [pendingRequests, setPendingRequests] = useState([])
  const [registeredEvents, setRegisteredEvents] = useState([])
  const [myPostings, setMyPostings] = useState([])
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      mentorshipApi.receivedRequests().catch(() => []),
      eventsApi.list().catch(() => []),
      jobsApi.myPostings().catch(() => []),
      notificationsApi.list().catch(() => []),
    ]).then(([received, events, postings, notes]) => {
      setPendingRequests((received ?? []).filter((r) => r.status === 'PENDING'))
      setRegisteredEvents((events ?? []).filter((e) => e.registeredByViewer))
      setMyPostings(postings ?? [])
      setNotifications((notes ?? []).slice(0, 3))
    }).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  return (
    <div>
      <PageHeader
        title={`Welcome back, ${user?.fullName?.split(' ')[0] ?? 'there'}`}
        description="Requests, your postings, and what's coming up."
      />

      <div className="grid gap-5 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Mentorship requests awaiting you</CardTitle>
            <Link to={ROUTES.MENTORSHIP} className="text-xs font-medium text-accent hover:text-accent-hover">
              View all
            </Link>
          </CardHeader>
          <CardBody className="space-y-3">
            {pendingRequests.length === 0 ? (
              <p className="text-sm text-text-secondary">No pending requests right now.</p>
            ) : (
              pendingRequests.map((req) => (
                <div key={req.id} className="flex items-center gap-3">
                  <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-accent-soft font-mono text-xs font-medium text-accent">
                    {initials(req.student?.fullName ?? req.studentName ?? '?')}
                  </div>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-text-primary">{req.student?.fullName ?? req.studentName}</p>
                    <p className="text-xs text-text-tertiary">
                      {req.student?.department ?? req.department}
                    </p>
                  </div>
                  <StatusBadge value={req.status} />
                </div>
              ))
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Your job postings</CardTitle>
            <Briefcase className="size-4 text-accent" aria-hidden />
          </CardHeader>
          <CardBody className="space-y-3">
            {myPostings.length === 0 ? (
              <p className="text-sm text-text-secondary">You haven't posted a job yet.</p>
            ) : (
              myPostings.map((job) => (
                <div key={job.id} className="flex items-center justify-between">
                  <p className="text-sm text-text-primary">{job.title}</p>
                  <StatusBadge value={job.status} />
                </div>
              ))
            )}
          </CardBody>
          <CardFooter>
            <Button as={Link} to={ROUTES.JOBS} variant="ghost" size="sm" rightIcon={<ArrowRight className="size-3.5" />}>
              Manage postings
            </Button>
          </CardFooter>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Your registered events</CardTitle>
            <CalendarDays className="size-4 text-accent" aria-hidden />
          </CardHeader>
          <CardBody className="space-y-3">
            {registeredEvents.length === 0 ? (
              <p className="text-sm text-text-secondary">No events registered yet.</p>
            ) : (
              registeredEvents.map((event) => (
                <div key={event.id}>
                  <p className="text-sm font-medium text-text-primary">{event.title}</p>
                  <p className="text-xs text-text-tertiary">{formatDate(event.eventDate ?? event.date)}</p>
                </div>
              ))
            )}
          </CardBody>
          <CardFooter>
            <Button as={Link} to={ROUTES.EVENTS} variant="ghost" size="sm" rightIcon={<ArrowRight className="size-3.5" />}>
              Browse all events
            </Button>
          </CardFooter>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Mentorship</CardTitle>
            <Handshake className="size-4 text-accent" aria-hidden />
          </CardHeader>
          <CardBody>
            <div className="flex items-baseline gap-2">
              <span className="font-mono text-2xl font-semibold text-text-primary">{pendingRequests.length}</span>
              <span className="text-sm text-text-secondary">pending</span>
            </div>
          </CardBody>
          <CardFooter>
            <Button as={Link} to={ROUTES.MENTORSHIP} variant="ghost" size="sm" rightIcon={<ArrowRight className="size-3.5" />}>
              Review requests
            </Button>
          </CardFooter>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Notifications</CardTitle>
            <Bell className="size-4 text-accent" aria-hidden />
          </CardHeader>
          <CardBody className="space-y-3">
            {notifications.length === 0 ? (
              <p className="text-sm text-text-secondary">Nothing new.</p>
            ) : (
              notifications.map((note) => (
                <div key={note.id} className="flex items-start gap-2.5">
                  <span className={`mt-1.5 size-1.5 shrink-0 rounded-full ${note.read ? 'bg-transparent' : 'bg-accent'}`} />
                  <div>
                    <p className="text-sm text-text-secondary">{note.message}</p>
                    <p className="text-xs text-text-tertiary">{timeAgo(note.createdAt)}</p>
                  </div>
                </div>
              ))
            )}
          </CardBody>
          <CardFooter>
            <Button as={Link} to={ROUTES.NOTIFICATIONS} variant="ghost" size="sm" rightIcon={<ArrowRight className="size-3.5" />}>
              View all
            </Button>
          </CardFooter>
        </Card>
      </div>
    </div>
  )
}
