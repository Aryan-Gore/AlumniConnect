import { NavLink } from 'react-router-dom'
import { useChat } from '@/features/chat/ChatProvider'
import { cn } from '@/utils/cn'

// Live counters that navigation items can show, keyed by `badgeKey` in navConfig.js.
function useNavBadges() {
  const { unreadTotal } = useChat()
  return { chat: unreadTotal }
}

export function NavItem({ to, label, icon: Icon, badgeKey, onNavigate, className }) {
  const badges = useNavBadges()
  const badge = badgeKey ? badges[badgeKey] : 0

  return (
    <NavLink
      to={to}
      onClick={onNavigate}
      className={({ isActive }) =>
        cn(
          'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
          isActive
            ? 'bg-accent-soft text-accent'
            : 'text-text-secondary hover:bg-surface-raised hover:text-text-primary',
          className
        )
      }
    >
      <Icon className="size-4 shrink-0" aria-hidden />
      <span className="min-w-0 flex-1 truncate">{label}</span>
      {badge > 0 && (
        <span
          className="flex h-5 min-w-5 items-center justify-center rounded-full bg-accent px-1.5 font-mono text-[11px] font-semibold text-white"
          aria-label={`${badge} unread`}
        >
          {badge > 99 ? '99+' : badge}
        </span>
      )}
    </NavLink>
  )
}
