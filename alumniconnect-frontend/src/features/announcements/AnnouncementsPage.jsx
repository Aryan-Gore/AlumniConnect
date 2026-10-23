import { useEffect, useState } from 'react'
import { Megaphone, Plus } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card, CardBody } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Input, Select } from '@/components/ui/Input'
import { Badge } from '@/components/ui/Badge'
import { EmptyState } from '@/components/ui/EmptyState'
import { useAuth } from '@/hooks/useAuth'
import { announcementsApi } from '@/api/announcementsApi'
import { formatDate } from '@/utils/formatters'
import { ROLES } from '@/utils/constants'
import { cn } from '@/utils/cn'
import { Spinner } from '@/components/ui/Spinner'

const PRIORITY_STYLE = {
  HIGH: 'border-l-danger',
  MEDIUM: 'border-l-warning',
  LOW: 'border-l-border',
}

const emptyDraft = { title: '', content: '', targetAudience: 'ALL', priority: 'MEDIUM' }

export function AnnouncementsPage() {
  const { role } = useAuth()
  const [announcements, setAnnouncements] = useState([])
  const [loading, setLoading] = useState(true)
  const [isComposing, setIsComposing] = useState(false)
  const [draft, setDraft] = useState(emptyDraft)
  const [publishing, setPublishing] = useState(false)

  useEffect(() => {
    const fetch = role === ROLES.ADMIN ? announcementsApi.adminList() : announcementsApi.list()
    fetch.then((data) => setAnnouncements(data ?? [])).finally(() => setLoading(false))
  }, [role])

  async function publish(event) {
    event.preventDefault()
    setPublishing(true)
    try {
      const created = await announcementsApi.create(draft)
      setAnnouncements((prev) => [created, ...prev])
      setDraft(emptyDraft)
      setIsComposing(false)
    } catch {
      // stay open so user can retry
    } finally {
      setPublishing(false)
    }
  }

  return (
    <div>
      <PageHeader
        title="Announcements"
        description="Institutional updates, targeted by audience."
        action={
          role === ROLES.ADMIN ? (
            <Button size="sm" leftIcon={<Plus className="size-4" />} onClick={() => setIsComposing((v) => !v)}>
              {isComposing ? 'Cancel' : 'New announcement'}
            </Button>
          ) : null
        }
      />

      {isComposing && (
        <Card className="mb-5">
          <CardBody>
            <form onSubmit={publish} className="flex flex-col gap-3">
              <Input
                label="Title"
                required
                value={draft.title}
                onChange={(e) => setDraft((d) => ({ ...d, title: e.target.value }))}
              />
              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium text-text-secondary">Content</label>
                <textarea
                  required
                  rows={3}
                  value={draft.content}
                  onChange={(e) => setDraft((d) => ({ ...d, content: e.target.value }))}
                  className="rounded-md border border-border bg-surface-sunken px-3 py-2 text-sm text-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/50"
                />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <Select
                  label="Target audience"
                  value={draft.targetAudience}
                  onChange={(e) => setDraft((d) => ({ ...d, targetAudience: e.target.value }))}
                >
                  {[
                    { value: 'ALL', label: 'All users' },
                    { value: 'STUDENTS', label: 'Students' },
                    { value: 'ALUMNI', label: 'Alumni' },
                  ].map(({ value, label }) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </Select>
                <Select
                  label="Priority"
                  value={draft.priority}
                  onChange={(e) => setDraft((d) => ({ ...d, priority: e.target.value }))}
                >
                  {['LOW', 'MEDIUM', 'HIGH'].map((p) => (
                    <option key={p} value={p}>
                      {p}
                    </option>
                  ))}
                </Select>
              </div>
              <Button type="submit" className="self-start" disabled={publishing}>
                {publishing ? 'Publishing…' : 'Publish'}
              </Button>
            </form>
          </CardBody>
        </Card>
      )}

      {loading ? (
        <div className="flex justify-center py-16">
          <Spinner />
        </div>
      ) : announcements.length === 0 ? (
        <Card>
          <EmptyState icon={Megaphone} title="No announcements yet" description="Published announcements will appear here." />
        </Card>
      ) : (
        <div className="flex flex-col gap-3">
          {announcements.map((a) => (
            <Card key={a.id} className={cn('border-l-4', PRIORITY_STYLE[a.priority] ?? 'border-l-border')}>
              <CardBody>
                <div className="flex items-start justify-between gap-3">
                  <p className="font-medium text-text-primary">{a.title}</p>
                  <Badge>{a.targetAudience}</Badge>
                </div>
                <p className="mt-1.5 text-sm text-text-secondary">{a.content}</p>
                <p className="mt-2 text-xs text-text-tertiary">{formatDate(a.publishDate ?? a.createdAt)}</p>
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
