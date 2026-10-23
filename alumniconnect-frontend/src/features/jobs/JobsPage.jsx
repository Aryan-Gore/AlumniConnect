import { useEffect, useState } from 'react'
import { Briefcase, Check, MapPin, Plus } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card, CardBody, CardFooter } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Badge, StatusBadge } from '@/components/ui/Badge'
import { EmptyState } from '@/components/ui/EmptyState'
import { useAuth } from '@/hooks/useAuth'
import { jobsApi } from '@/api/jobsApi'
import { formatDate } from '@/utils/formatters'
import { ROLES } from '@/utils/constants'
import { cn } from '@/utils/cn'
import { Spinner } from '@/components/ui/Spinner'

export function JobsPage() {
  const { role } = useAuth()
  const [jobs, setJobs] = useState([])
  const [myPostings, setMyPostings] = useState([])
  const [loading, setLoading] = useState(true)
  const [tab, setTab] = useState('browse')

  useEffect(() => {
    const promises = [jobsApi.list()]
    if (role === ROLES.ALUMNI) promises.push(jobsApi.myPostings())
    Promise.all(promises)
      .then(([all, mine]) => { setJobs(all ?? []); if (mine) setMyPostings(mine ?? []) })
      .finally(() => setLoading(false))
  }, [role])

  async function apply(id) {
    await jobsApi.apply(id).catch(() => {})
    setJobs((prev) => prev.map((j) => (j.id === id ? { ...j, appliedByViewer: true } : j)))
  }

  const browseable = jobs

  return (
    <div>
      <PageHeader
        title="Jobs"
        description="Opportunities shared by alumni."
        action={
          role === ROLES.ALUMNI ? (
            <Button size="sm" leftIcon={<Plus className="size-4" />} onClick={() => setTab('mine')}>
              Post a job
            </Button>
          ) : null
        }
      />

      {role === ROLES.ALUMNI && (
        <div className="mb-5 flex gap-1 border-b border-border-soft">
          {[
            { id: 'browse', label: 'Browse' },
            { id: 'mine', label: 'My postings' },
          ].map((t) => (
            <button
              key={t.id}
              onClick={() => setTab(t.id)}
              className={cn(
                'border-b-2 px-3 py-2 text-sm font-medium transition-colors',
                tab === t.id ? 'border-accent text-accent' : 'border-transparent text-text-secondary hover:text-text-primary'
              )}
            >
              {t.label}
            </button>
          ))}
        </div>
      )}

      {loading ? <div className="flex justify-center py-16"><Spinner /></div> : null}

      {!loading && tab === 'browse' && (
        <div className="grid gap-4 sm:grid-cols-2">
          {browseable.map((job) => (
            <Card key={job.id} className="flex flex-col">
              <CardBody className="flex-1">
                <p className="font-display text-base font-medium text-text-primary">{job.title}</p>
                <p className="text-sm text-text-secondary">{job.company}</p>
                <p className="mt-2 text-sm text-text-secondary">{job.description}</p>
                <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-text-tertiary">
                  <Badge>{job.jobType ?? job.type}</Badge>
                  {job.location && (
                    <span className="flex items-center gap-1">
                      <MapPin className="size-3" /> {job.location}
                    </span>
                  )}
                  {job.author && <span>via {job.author.fullName}</span>}
                </div>
              </CardBody>
              <CardFooter className="justify-end">
                <Button
                  size="sm"
                  variant={job.appliedByViewer ? 'secondary' : 'primary'}
                  leftIcon={job.appliedByViewer ? <Check className="size-3.5" /> : null}
                  disabled={job.appliedByViewer}
                  onClick={() => apply(job.id)}
                >
                  {job.appliedByViewer ? 'Applied' : 'Apply'}
                </Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}

      {!loading && tab === 'mine' && (
        <div className="flex flex-col gap-3">
          {myPostings.length === 0 ? (
            <Card>
              <EmptyState icon={Briefcase} title="You haven't posted a job yet" description="Share an opportunity with students and fellow alumni." />
            </Card>
          ) : (
            myPostings.map((job) => (
              <Card key={job.id}>
                <CardBody className="flex items-center justify-between gap-4">
                  <div>
                    <p className="font-medium text-text-primary">{job.title}</p>
                    <p className="text-xs text-text-tertiary">
                      {job.company} · Posted {formatDate(job.createdAt ?? job.postedOn)}
                    </p>
                  </div>
                  <StatusBadge value={job.status} />
                </CardBody>
              </Card>
            ))
          )}
        </div>
      )}
    </div>
  )
}
