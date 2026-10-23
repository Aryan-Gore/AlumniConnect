import { useEffect, useState } from 'react'
import { ScrollText } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card } from '@/components/ui/Card'
import { Select } from '@/components/ui/Input'
import { EmptyState } from '@/components/ui/EmptyState'
import { adminApi } from '@/api/adminApi'
import { formatDateTime } from '@/utils/formatters'
import { Spinner } from '@/components/ui/Spinner'

const ACTION_TONE = {
  IMPORT_ALUMNI: 'text-info',
  EXPORT_ALUMNI: 'text-info',
  ADD_ALUMNI: 'text-success',
  UPDATE_ALUMNI: 'text-text-secondary',
  DEACTIVATE_ALUMNI: 'text-danger',
  INVITE_ALUMNI: 'text-accent',
  CREATE_EVENT: 'text-success',
  UPDATE_EVENT: 'text-text-secondary',
  APPROVE_JOB: 'text-success',
  REJECT_JOB: 'text-danger',
}

const KNOWN_ACTIONS = Object.keys(ACTION_TONE)

export function AuditLogPage() {
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)
  const [actionFilter, setActionFilter] = useState('')

  useEffect(() => {
    adminApi.auditLog(actionFilter ? { action: actionFilter } : {})
      .then((data) => setEntries(data ?? []))
      .catch(() => setEntries([]))
      .finally(() => setLoading(false))
  }, [actionFilter])

  // Derive unique actions from loaded entries + known ones
  const actions = [...new Set([...KNOWN_ACTIONS, ...entries.map((e) => e.action)])]

  return (
    <div>
      <PageHeader
        title="Audit log"
        description="Read-only record of administrative actions. Not editable by any role."
        action={
          <Select value={actionFilter} onChange={(e) => { setActionFilter(e.target.value) }} className="w-52">
            <option value="">All actions</option>
            {actions.map((a) => <option key={a} value={a}>{a}</option>)}
          </Select>
        }
      />

      {loading ? (
        <div className="flex justify-center py-16"><Spinner /></div>
      ) : (
        <Card className="overflow-hidden">
          {entries.length === 0 ? (
            <EmptyState icon={ScrollText} title="No matching entries" />
          ) : (
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-border-soft bg-surface-raised text-xs uppercase tracking-wider text-text-tertiary">
                  <th className="px-5 py-3 font-medium">Action</th>
                  <th className="px-5 py-3 font-medium">Details</th>
                  <th className="px-5 py-3 font-medium">Admin</th>
                  <th className="px-5 py-3 font-medium">Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {entries.map((entry) => (
                  <tr key={entry.id} className="border-b border-border-soft last:border-0">
                    <td className="px-5 py-3.5">
                      <span className={`font-mono text-xs font-medium ${ACTION_TONE[entry.action] ?? 'text-text-secondary'}`}>
                        {entry.action}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-text-secondary">{entry.details}</td>
                    <td className="px-5 py-3.5 font-mono text-xs text-text-tertiary">{entry.adminEmail ?? entry.admin}</td>
                    <td className="px-5 py-3.5 text-xs text-text-tertiary">{formatDateTime(entry.timestamp ?? entry.createdAt)}</td>
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
