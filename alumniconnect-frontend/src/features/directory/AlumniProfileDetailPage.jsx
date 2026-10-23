import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Handshake, Lock, MapPin, Pencil } from 'lucide-react'
import { Card, CardHeader, CardTitle, CardBody } from '@/components/ui/Card'
import { Badge, StatusBadge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { useAuth } from '@/hooks/useAuth'
import { alumniApi } from '@/api/alumniApi'
import { initials } from '@/utils/formatters'
import { ROLES, ROUTES } from '@/utils/constants'
import { Spinner } from '@/components/ui/Spinner'

export function AlumniProfileDetailPage() {
  const { alumniId } = useParams()
  const navigate = useNavigate()
  const { role } = useAuth()
  const [requestSent, setRequestSent] = useState(false)
  const [alumnus, setAlumnus] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    alumniApi.getById(alumniId)
      .then(setAlumnus)
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [alumniId])

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  if (error || !alumnus) {
    return (
      <div>
        <Button variant="ghost" size="sm" onClick={() => navigate(ROUTES.DIRECTORY)} leftIcon={<ArrowLeft className="size-4" />}>
          Back to directory
        </Button>
        <p className="mt-6 text-sm text-text-secondary">No alumnus found with that ID.</p>
      </div>
    )
  }

  return (
    <div>
      <Link to={ROUTES.DIRECTORY} className="mb-5 inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary">
        <ArrowLeft className="size-4" /> Back to directory
      </Link>

      <div className="flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
        <div className="flex items-center gap-4">
          <div className="flex size-14 items-center justify-center rounded-full bg-accent-soft font-mono text-lg font-medium text-accent">
            {initials(alumnus.fullName)}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="font-display text-xl text-text-primary">{alumnus.fullName}</h1>
              <StatusBadge value={alumnus.status} />
            </div>
            <p className="text-sm text-text-secondary">
              {alumnus.jobTitle} at {alumnus.currentCompany}
            </p>
            <p className="mt-0.5 flex items-center gap-1.5 text-xs text-text-tertiary">
              <MapPin className="size-3.5" /> {alumnus.location}
            </p>
          </div>
        </div>

        {role === ROLES.STUDENT && alumnus.openToMentoring && (
          <Button
            onClick={() => setRequestSent(true)}
            disabled={requestSent}
            leftIcon={<Handshake className="size-4" />}
          >
            {requestSent ? 'Request sent' : 'Request mentorship'}
          </Button>
        )}
      </div>

      <div className="mt-6 grid gap-5 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Academic record</CardTitle>
            <span className="flex items-center gap-1 text-[11px] text-text-tertiary">
              <Lock className="size-3" /> Institution-controlled
            </span>
          </CardHeader>
          <CardBody className="space-y-2.5 text-sm">
            <Row label="Enrollment number" value={alumnus.enrollmentNumber} mono />
            <Row label="Degree" value={alumnus.degree} />
            <Row label="Department" value={alumnus.department} />
            <Row label="Batch" value={alumnus.batch} mono />
            <Row label="Graduation year" value={alumnus.graduationYear} mono />
          </CardBody>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Professional details</CardTitle>
            <span className="flex items-center gap-1 text-[11px] text-text-tertiary">
              <Pencil className="size-3" /> Alumni-controlled
            </span>
          </CardHeader>
          <CardBody className="space-y-2.5 text-sm">
            <Row label="Current company" value={alumnus.currentCompany} />
            <Row label="Job title" value={alumnus.jobTitle} />
            <Row label="Industry" value={alumnus.industry} />
            <Row label="Location" value={alumnus.location} />
          </CardBody>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Skills</CardTitle>
          </CardHeader>
          <CardBody className="flex flex-wrap gap-2">
            {alumnus.skills.map((skill) => (
              <Badge key={skill} variant="accent">
                {skill}
              </Badge>
            ))}
          </CardBody>
        </Card>
      </div>
    </div>
  )
}

function Row({ label, value, mono = false }) {
  return (
    <div className="flex items-center justify-between border-b border-border-soft pb-2 last:border-0 last:pb-0">
      <span className="text-text-tertiary">{label}</span>
      <span className={mono ? 'font-mono text-text-primary' : 'text-text-primary'}>{value}</span>
    </div>
  )
}
