import { Link } from 'react-router-dom'
import { MapPin } from 'lucide-react'
import { Badge, StatusBadge } from '@/components/ui/Badge'
import { initials } from '@/utils/formatters'

export function AlumniCard({ alumnus }) {
  return (
    <Link
      to={`/directory/${alumnus.id}`}
      className="block rounded-lg border border-border bg-surface p-4 transition-colors hover:border-accent/40"
    >
      <div className="flex items-start justify-between">
        <div className="flex size-10 items-center justify-center rounded-full bg-accent-soft font-mono text-sm font-medium text-accent">
          {initials(alumnus.fullName)}
        </div>
        <StatusBadge value={alumnus.status} />
      </div>

      <p className="mt-3 font-display text-base text-text-primary">{alumnus.fullName}</p>
      <p className="text-sm text-text-secondary">{alumnus.jobTitle}</p>
      <p className="text-sm text-text-tertiary">{alumnus.currentCompany}</p>

      <p className="mt-2 font-mono text-[11px] text-text-tertiary">
        {alumnus.department} · Batch {alumnus.batch}
      </p>

      <div className="mt-3 flex items-center gap-1.5 text-xs text-text-tertiary">
        <MapPin className="size-3.5" />
        {alumnus.location}
      </div>

      {alumnus.skills?.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-1.5">
          {alumnus.skills.slice(0, 3).map((skill) => (
            <Badge key={skill}>{skill}</Badge>
          ))}
        </div>
      )}
    </Link>
  )
}
