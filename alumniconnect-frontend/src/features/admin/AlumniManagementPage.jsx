import { useEffect, useMemo, useState } from 'react'
import { Download, Mail, Search, ToggleLeft, ToggleRight, UserPlus, Users2 } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Select } from '@/components/ui/Input'
import { StatusBadge } from '@/components/ui/Badge'
import { EmptyState } from '@/components/ui/EmptyState'
import { alumniApi } from '@/api/alumniApi'
import { initials } from '@/utils/formatters'
import { cn } from '@/utils/cn'
import { Spinner } from '@/components/ui/Spinner'

const DEPARTMENTS = [
  'Computer Science & Engineering',
  'Electronics & Communication Engineering',
  'Mechanical Engineering',
  'Civil Engineering',
  'Information Technology',
]

export function AlumniManagementPage() {
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState('')
  const [department, setDepartment] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)

  useEffect(() => {
    setLoading(true)
    alumniApi.adminList({ query: query || undefined, department: department || undefined, status: status || undefined, page, size: 25 })
      .then((res) => {
        setRecords(res?.items ?? res ?? [])
        setTotalPages(res?.totalPages ?? 1)
      })
      .catch(() => setRecords([]))
      .finally(() => setLoading(false))
  }, [query, department, status, page])

  // Debounce query changes to avoid excessive API calls
  const [debouncedQuery, setDebouncedQuery] = useState('')
  useEffect(() => {
    const t = setTimeout(() => { setQuery(debouncedQuery); setPage(0) }, 300)
    return () => clearTimeout(t)
  }, [debouncedQuery])

  async function invite(id) {
    await alumniApi.adminInviteToClaim(id).catch(() => {})
    setRecords((prev) => prev.map((a) => a.id === id ? { ...a, status: 'INVITED' } : a))
  }

  async function toggleActive(alum) {
    const next = alum.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    await alumniApi.adminSetActive(alum.id, next === 'ACTIVE').catch(() => {})
    setRecords((prev) => prev.map((a) => a.id === alum.id ? { ...a, status: next } : a))
  }

  return (
    <div>
      <PageHeader
        title="Alumni records"
        description={`${records.length.toLocaleString()} records loaded.`}
        action={
          <div className="flex gap-2">
            <Button variant="secondary" leftIcon={<Download className="size-4" />}>Export</Button>
            <Button leftIcon={<UserPlus className="size-4" />}>Add alumni</Button>
          </div>
        }
      />

      <div className="mb-4 flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-text-tertiary" />
          <input
            value={debouncedQuery}
            onChange={(e) => setDebouncedQuery(e.target.value)}
            placeholder="Search by name or enrollment number…"
            className="h-10 w-full rounded-md border border-border bg-surface-sunken pl-9 pr-3 text-sm text-text-primary placeholder:text-text-tertiary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/50"
          />
        </div>
        <Select value={department} onChange={(e) => { setDepartment(e.target.value); setPage(0) }} className="sm:w-52">
          <option value="">All departments</option>
          {DEPARTMENTS.map((d) => <option key={d} value={d}>{d}</option>)}
        </Select>
        <Select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0) }} className="sm:w-44">
          <option value="">All statuses</option>
          {['IMPORTED', 'INVITED', 'CLAIMED', 'ACTIVE', 'INACTIVE'].map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </Select>
      </div>

      {loading ? (
        <div className="flex justify-center py-16"><Spinner /></div>
      ) : (
        <Card className="overflow-hidden">
          {records.length === 0 ? (
            <EmptyState icon={Users2} title="No matching records" description="Try a different search term or filter." />
          ) : (
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-border-soft bg-surface-raised text-xs uppercase tracking-wider text-text-tertiary">
                  <th className="px-5 py-3 font-medium">Alumnus</th>
                  <th className="px-5 py-3 font-medium">Department</th>
                  <th className="px-5 py-3 font-medium">Batch</th>
                  <th className="px-5 py-3 font-medium">Status</th>
                  <th className="px-5 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {records.map((alum) => (
                  <tr key={alum.id} className="border-b border-border-soft last:border-0 hover:bg-surface-raised/60">
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-3">
                        <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-accent-soft font-mono text-xs font-medium text-accent">
                          {initials(alum.fullName)}
                        </div>
                        <div>
                          <p className="font-medium text-text-primary">{alum.fullName}</p>
                          <p className="font-mono text-xs text-text-tertiary">{alum.enrollmentNumber}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-3.5 text-text-secondary">{alum.department}</td>
                    <td className="px-5 py-3.5 font-mono text-xs text-text-secondary">{alum.batch}</td>
                    <td className="px-5 py-3.5"><StatusBadge value={alum.status} /></td>
                    <td className="px-5 py-3.5">
                      <div className="flex items-center justify-end gap-1">
                        {alum.status === 'IMPORTED' && (
                          <Button variant="ghost" size="sm" leftIcon={<Mail className="size-3.5" />} onClick={() => invite(alum.id)}>
                            Invite
                          </Button>
                        )}
                        {(alum.status === 'ACTIVE' || alum.status === 'INACTIVE') && (
                          <button
                            type="button"
                            onClick={() => toggleActive(alum)}
                            className={cn(
                              'inline-flex items-center gap-1.5 rounded-md px-2.5 py-1.5 text-xs font-medium transition-colors',
                              alum.status === 'ACTIVE'
                                ? 'text-text-secondary hover:bg-surface-raised'
                                : 'text-accent hover:bg-accent-soft'
                            )}
                          >
                            {alum.status === 'ACTIVE' ? <ToggleRight className="size-4" /> : <ToggleLeft className="size-4" />}
                            {alum.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                          </button>
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

      {totalPages > 1 && (
        <div className="mt-4 flex items-center justify-center gap-3">
          <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Previous</Button>
          <span className="text-xs text-text-tertiary">Page {page + 1} of {totalPages}</span>
          <Button variant="secondary" size="sm" disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}>Next</Button>
        </div>
      )}
    </div>
  )
}
