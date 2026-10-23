import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Check, Handshake, Users2, X } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card, CardBody } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { StatusBadge } from '@/components/ui/Badge'
import { EmptyState } from '@/components/ui/EmptyState'
import { useAuth } from '@/hooks/useAuth'
import { mentorshipApi } from '@/api/mentorshipApi'
import { initials, formatDate } from '@/utils/formatters'
import { ROLES, ROUTES } from '@/utils/constants'
import { Spinner } from '@/components/ui/Spinner'

export function MentorshipPage() {
  const { role } = useAuth()
  return role === ROLES.ALUMNI ? <ReceivedRequests /> : <SentRequests />
}

function SentRequests() {
  const [requests, setRequests] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    mentorshipApi.myRequests().then((data) => setRequests(data ?? [])).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  return (
    <div>
      <PageHeader
        title="Mentorship"
        description="Requests you've sent to alumni."
        action={
          <Button as={Link} to={ROUTES.DIRECTORY} variant="secondary" size="sm">
            Find a mentor
          </Button>
        }
      />
      {requests.length === 0 ? (
        <Card>
          <EmptyState
            icon={Handshake}
            title="No mentorship requests yet"
            description="Browse the alumni directory and request mentorship from someone open to it."
          />
        </Card>
      ) : (
        <div className="flex flex-col gap-3">
          {requests.map((req) => (
            <Card key={req.id}>
              <CardBody className="flex items-start justify-between gap-4">
                <div className="flex items-start gap-3">
                  <div className="flex size-10 shrink-0 items-center justify-center rounded-full bg-accent-soft font-mono text-xs font-medium text-accent">
                    {initials(req.alumni?.fullName ?? req.alumniName ?? '?')}
                  </div>
                  <div>
                    <p className="font-medium text-text-primary">{req.alumni?.fullName ?? req.alumniName}</p>
                    <p className="text-xs text-text-tertiary">
                      {req.alumni?.jobTitle ?? req.alumniTitle} · {req.alumni?.currentCompany ?? req.alumniCompany}
                    </p>
                    <p className="mt-2 text-sm text-text-secondary">"{req.message}"</p>
                    <p className="mt-2 text-xs text-text-tertiary">Sent {formatDate(req.createdAt ?? req.requestedOn)}</p>
                  </div>
                </div>
                <StatusBadge value={req.status} />
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}

function ReceivedRequests() {
  const [requests, setRequests] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    mentorshipApi.receivedRequests().then((data) => setRequests(data ?? [])).finally(() => setLoading(false))
  }, [])

  async function respond(id, decision) {
    await mentorshipApi.respondToRequest(id, decision).catch(() => {})
    setRequests((prev) => prev.map((r) => (r.id === id ? { ...r, status: decision } : r)))
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  const pending = requests.filter((r) => r.status === 'PENDING')
  const decided = requests.filter((r) => r.status !== 'PENDING')

  return (
    <div>
      <PageHeader title="Mentorship requests" description={`${pending.length} awaiting your response.`} />

      {requests.length === 0 ? (
        <Card>
          <EmptyState icon={Users2} title="No requests yet" description="Students will appear here when they request your mentorship." />
        </Card>
      ) : (
        <div className="flex flex-col gap-3">
          {[...pending, ...decided].map((req) => (
            <Card key={req.id}>
              <CardBody className="flex items-start justify-between gap-4">
                <div className="flex items-start gap-3">
                  <div className="flex size-10 shrink-0 items-center justify-center rounded-full bg-accent-soft font-mono text-xs font-medium text-accent">
                    {initials(req.student?.fullName ?? req.studentName ?? '?')}
                  </div>
                  <div>
                    <p className="font-medium text-text-primary">{req.student?.fullName ?? req.studentName}</p>
                    <p className="text-xs text-text-tertiary">
                      {req.student?.department ?? req.department}
                    </p>
                    <p className="mt-2 text-sm text-text-secondary">"{req.message}"</p>
                    <p className="mt-2 text-xs text-text-tertiary">Received {formatDate(req.createdAt ?? req.requestedOn)}</p>
                  </div>
                </div>

                {req.status === 'PENDING' ? (
                  <div className="flex shrink-0 gap-2">
                    <Button size="sm" variant="secondary" leftIcon={<X className="size-3.5" />} onClick={() => respond(req.id, 'REJECTED')}>
                      Decline
                    </Button>
                    <Button size="sm" leftIcon={<Check className="size-3.5" />} onClick={() => respond(req.id, 'ACCEPTED')}>
                      Accept
                    </Button>
                  </div>
                ) : (
                  <StatusBadge value={req.status} />
                )}
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
