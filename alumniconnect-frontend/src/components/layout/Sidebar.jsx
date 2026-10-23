import { GraduationCap } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { NAV_BY_ROLE, FOOTER_NAV } from './navConfig'
import { NavItem } from './NavItem'

export function Sidebar() {
  const { role } = useAuth()
  const items = NAV_BY_ROLE[role] ?? []

  return (
    <aside className="hidden w-60 shrink-0 flex-col border-r border-border bg-surface md:flex">
      <div className="flex h-16 items-center gap-2 border-b border-border px-5">
        <GraduationCap className="size-5 text-accent" aria-hidden />
        <span className="font-display text-lg leading-none">
          <span className="font-medium text-text-primary">Alumni</span>
          <span className="font-semibold text-accent">Connect</span>
        </span>
      </div>

      <nav className="flex flex-1 flex-col gap-0.5 overflow-y-auto px-3 py-4" aria-label="Main">
        {items.map((item) => (
          <NavItem key={item.to + item.label} {...item} />
        ))}
      </nav>

      <div className="flex flex-col gap-0.5 border-t border-border-soft px-3 py-3">
        {FOOTER_NAV.map((item) => (
          <NavItem key={item.to} {...item} />
        ))}
      </div>
    </aside>
  )
}
