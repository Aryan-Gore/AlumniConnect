import { useEffect, useState } from 'react'
import { Check, ShieldCheck, Trash2, X } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Select } from '@/components/ui/Input'
import { StatusBadge } from '@/components/ui/Badge'
import { EmptyState } from '@/components/ui/EmptyState'
import { jobsApi } from '@/api/jobsApi'
import { formatDate } from '@/utils/formatters'
import { Spinner } from '@/components/ui/Spinner'

export function JobModerationPage() {
  const [jobs, setJobs] = useState([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('')

  useEffect(() => {
    jobsApi.adminModerationQueue(statusFilter ? { status: statusFilter } : {})
      .then((data) => setJobs(data?.items ?? data ?? []))
      .catch(() => setJobs([]))
      .finally(() => setLoading(false))
  }, [statusFilter])

  async function setJobStatus(id, status) {
    await jobsApi.adminSetStatus(id, status).catch(() => {})
    setJobs((prev) => prev.map((j) => j.id === id ? { ...j, status } : j))
  }

  const filtered = jobs
  const pendingCount = jobs.filter((j) => j.status === 'PENDING').length

  return (
    <div>
      <PageHeader
        title="Job moderation"
        description={`${pendingCount} listing${pendingCount === 1 ? '' : 's'} awaiting review.`}
        action={
          <Select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="w-44">
            <option value="">All statuses</option>
            {['PENDING', 'APPROVED', 'REJECTED', 'REMOVED', 'EXPIRED'].map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </Select>
        }
      />

      {loading ? (
        <div className="flex justify-center py-16"><Spinner /></div>
      ) : (
        <Card className="overflow-hidden">
          {filtered.length === 0 ? (
            <EmptyState icon={ShieldCheck} title="Nothing here" description="No listings match this filter." />
          ) : (
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-border-soft bg-surface-raised text-xs uppercase tracking-wider text-text-tertiary">
                  <th className="px-5 py-3 font-medium">Listing</th>
                  <th className="px-5 py-3 font-medium">Posted by</th>
                  <th className="px-5 py-3 font-medium">Date</th>
                  <th className="px-5 py-3 font-medium">Status</th>
                  <th className="px-5 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((job) => (
                  <tr key={job.id} className="border-b border-border-soft last:border-0 hover:bg-surface-raised/60">
                    <td className="px-5 py-3.5">
                      <p className="font-medium text-text-primary">{job.title}</p>
                      <p className="text-xs text-text-tertiary">{job.company}</p>
                    </td>
                    <td className="px-5 py-3.5 text-text-secondary">
                      {job.author?.fullName ?? job.postedBy}
                    </td>
                    <td className="px-5 py-3.5 font-mono text-xs text-text-secondary">
                      {formatDate(job.createdAt ?? job.postedOn)}
                    </td>
                    <td className="px-5 py-3.5"><StatusBadge value={job.status} /></td>
                    <td className="px-5 py-3.5">
                      <div className="flex justify-end gap-1">
                        {job.status === 'PENDING' && (
                          <>
                            <Button size="sm" variant="ghost" leftIcon={<X className="size-3.5" />} onClick={() => setJobStatus(job.id, 'REJECTED')}>
                              Reject
                            </Button>
                            <Button size="sm" leftIcon={<Check className="size-3.5" />} onClick={() => setJobStatus(job.id, 'APPROVED')}>
                              Approve
                            </Button>
                          </>
                        )}
                        {job.status === 'APPROVED' && (
                          <Button size="sm" variant="ghost" leftIcon={<Trash2 className="size-3.5" />} onClick={() => setJobStatus(job.id, 'REMOVED')}>
                            Remove
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      )}
    </div>
  )
}
