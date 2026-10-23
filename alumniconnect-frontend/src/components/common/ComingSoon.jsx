import { Hammer } from 'lucide-react'
import { PageHeader } from './PageHeader'
import { EmptyState } from '@/components/ui/EmptyState'
import { Card } from '@/components/ui/Card'

export function ComingSoon({ title, description, plannedFeatures = [] }) {
  return (
    <div>
      <PageHeader title={title} description={description} />
      <Card>
        <EmptyState
          icon={Hammer}
          title="This module is scaffolded, not built yet"
          description="Routing, layout, and the API contract are already in place — the screen itself is next in line."
        />
        {plannedFeatures.length > 0 && (
          <div className="border-t border-border-soft px-5 py-4">
            <p className="mb-2 text-xs font-medium uppercase tracking-wider text-text-tertiary">
              Planned for this screen
            </p>
            <ul className="grid gap-1.5 text-sm text-text-secondary sm:grid-cols-2">
              {plannedFeatures.map((feature) => (
                <li key={feature} className="flex items-start gap-2">
                  <span className="mt-1.5 size-1 shrink-0 rounded-full bg-accent" />
                  {feature}
                </li>
              ))}
            </ul>
          </div>
        )}
      </Card>
    </div>
  )
}
