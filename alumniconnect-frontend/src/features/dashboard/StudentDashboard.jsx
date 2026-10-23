import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Bell, Briefcase, CalendarDays, Handshake } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card, CardHeader, CardTitle, CardBody, CardFooter } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { useAuth } from '@/hooks/useAuth'
import { alumniApi } from '@/api/alumniApi'
import { eventsApi } from '@/api/eventsApi'
import { jobsApi } from '@/api/jobsApi'
import { mentorshipApi } from '@/api/mentorshipApi'
import { notificationsApi } from '@/api/notificationsApi'
import { formatDate, timeAgo, initials } from '@/utils/formatters'
import { ROUTES } from '@/utils/constants'
import { Spinner } from '@/components/ui/Spinner'

export function StudentDashboard() {
  const { user } = useAuth()
  const [featuredAlumni, setFeaturedAlumni] = useState([])
  const [upcomingEvents, setUpcomingEvents] = useState([])
  const [availableJobs, setAvailableJobs] = useState([])
  const [mentorshipCounts, setMentorshipCounts] = useState({ active: 0, pending: 0 })
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      alumniApi.search({ size: 3 }).catch(() => ({ items: [] })),
      eventsApi.list().catch(() => []),
      jobsApi.list().catch(() => []),
      mentorshipApi.myRequests().catch(() => []),
      notificationsApi.list().catch(() => []),
    ]).then(([alumniResult, events, jobs, myRequests, notes]) => {
      setFeaturedAlumni((alumniResult?.items ?? alumniResult ?? []).slice(0, 3))
      setUpcomingEvents((events ?? []).slice(0, 3))
      setAvailableJobs((jobs ?? []).filter((j) => !j.appliedByViewer).slice(0, 3))
      const reqs = myRequests ?? []
      setMentorshipCounts({
        active: reqs.filter((r) => r.status === 'ACCEPTED').length,
        pending: reqs.filter((r) => r.status === 'PENDING').length,
      })
      setNotifications((notes ?? []).slice(0, 3))
    }).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  return (
    <div>
      <PageHeader
        title={`Welcome back, ${user?.fullName?.split(' ')[0] ?? 'there'}`}
        description="Here's what's happening across the alumni network this week."
      />

      <div className="grid gap-5 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Recommended alumni to connect with</CardTitle>
            <Link to={ROUTES.DIRECTORY} className="text-xs font-medium text-accent hover:text-accent-hover">
              View directory
            </Link>
          </CardHeader>
          <CardBody className="grid gap-3 sm:grid-cols-3">
            {featuredAlumni.length === 0 ? (
              <p className="text-sm text-text-secondary sm:col-span-3">No alumni profiles to show yet.</p>
            ) : (
              featuredAlumni.map((alum) => (
                <Link
                  key={alum.id}
                  to={`${ROUTES.DIRECTORY}/${alum.id}`}
                  className="rounded-md border border-border-soft p-3.5 transition-colors hover:bg-surface-raised"
                >
                  <div className="flex size-9 items-center justify-center rounded-full bg-accent-soft font-mono text-xs font-medium text-accent">
                    {initials(alum.fullName)}
                  </div>
                  <p className="mt-2.5 text-sm font-medium text-text-primary">{alum.fullName}</p>
                  <p className="text-xs text-text-tertiary">
                    {alum.jobTitle} · {alum.currentCompany}
                  </p>
                </Link>
              ))
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Mentorship</CardTitle>
            <Handshake className="size-4 text-accent" aria-hidden />
          </CardHeader>
          <CardBody className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm text-text-secondary">Active</span>
              <span className="font-mono text-lg font-semibold text-text-primary">{mentorshipCounts.active}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-text-secondary">Pending requests</span>
              <span className="font-mono text-lg font-semibold text-text-primary">{mentorshipCounts.pending}</span>
            </div>
          </CardBody>
          <CardFooter>
            <Button as={Link} to={ROUTES.MENTORSHIP} variant="ghost" size="sm" rightIcon={<ArrowRight className="size-3.5" />}>
              Find a mentor
            </Button>
          </CardFooter>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Upcoming events</CardTitle>
            <CalendarDays className="size-4 text-accent" aria-hidden />
          </CardHeader>
          <CardBody className="space-y-3">
            {upcomingEvents.length === 0 ? (
              <p className="text-sm text-text-secondary">No upcoming events.</p>
            ) : (
              upcomingEvents.map((event) => (
                <div key={event.id} className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-sm font-medium text-text-primary">{event.title}</p>
                    <p className="text-xs text-text-tertiary">{formatDate(event.eventDate ?? event.date)}</p>
                  </div>
                  <Badge>{event.mode}</Badge>
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
            <CardTitle>Available jobs</CardTitle>
            <Briefcase className="size-4 text-accent" aria-hidden />
          </CardHeader>
          <CardBody className="space-y-3">
            {availableJobs.length === 0 ? (
              <p className="text-sm text-text-secondary">No open listings right now.</p>
            ) : (
              availableJobs.map((job) => (
                <div key={job.id}>
                  <p className="text-sm font-medium text-text-primary">{job.title}</p>
                  <p className="text-xs text-text-tertiary">
                    {job.company}{job.author ? ` · via ${job.author.fullName}` : ''}
                  </p>
                </div>
              ))
            )}
          </CardBody>
          <CardFooter>
            <Button as={Link} to={ROUTES.JOBS} variant="ghost" size="sm" rightIcon={<ArrowRight className="size-3.5" />}>
              Browse all jobs
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
