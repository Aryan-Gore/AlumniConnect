import { useEffect, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { Bell, ChevronDown, LogOut, Menu } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { notificationsApi } from '@/api/notificationsApi'
import { Avatar } from '@/components/ui/Avatar'
import { ROUTES } from '@/utils/constants'
import { ROLE_LABELS } from '@/utils/labels'
import { cn } from '@/utils/cn'

const NOTIFICATION_POLL_MS = 60_000

export function Topbar({ onMenuClick }) {
  const { user, role, logout } = useAuth()
  const location = useLocation()
  const [unreadCount, setUnreadCount] = useState(0)
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const menuRef = useRef(null)

  // Refresh the bell on every navigation and once a minute while the tab is open.
  useEffect(() => {
    let cancelled = false
    function load() {
      notificationsApi
        .unreadCount()
        .then((data) => {
          if (!cancelled) setUnreadCount(data?.count ?? 0)
        })
        .catch(() => {
          // The bell just stays as it was; a failed poll is not worth interrupting anyone for.
        })
    }
    load()
    const timer = setInterval(load, NOTIFICATION_POLL_MS)
    return () => {
      cancelled = true
      clearInterval(timer)
    }
  }, [location.pathname])

  useEffect(() => {
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setIsMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  return (
    <header className="flex h-16 shrink-0 items-center justify-between border-b border-border bg-surface px-4 md:px-6">
      <button
        type="button"
        onClick={onMenuClick}
        className="flex size-9 items-center justify-center rounded-md text-text-secondary hover:bg-surface-raised md:hidden"
        aria-label="Open navigation"
      >
        <Menu className="size-5" />
      </button>

      <div className="hidden md:block" />

      <div className="flex items-center gap-2">
        <Link
          to={ROUTES.NOTIFICATIONS}
          className="relative flex size-9 items-center justify-center rounded-md text-text-secondary hover:bg-surface-raised hover:text-text-primary"
          aria-label={unreadCount > 0 ? `Notifications (${unreadCount} unread)` : 'Notifications'}
        >
          <Bell className="size-[18px]" />
          {unreadCount > 0 && (
            <span className="absolute right-1 top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-accent px-1 font-mono text-[10px] font-semibold text-white">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </Link>

        <div className="relative" ref={menuRef}>
          <button
            type="button"
            onClick={() => setIsMenuOpen((open) => !open)}
            className="flex items-center gap-2 rounded-md py-1.5 pl-1.5 pr-2.5 hover:bg-surface-raised"
            aria-haspopup="menu"
            aria-expanded={isMenuOpen}
          >
            <Avatar src={user?.profilePhotoUrl} name={user?.fullName ?? ''} size="sm" />
            <span className="hidden text-left sm:block">
              <span className="block text-sm font-medium leading-tight text-text-primary">
                {user?.fullName ?? 'Account'}
              </span>
              <span className="block text-xs leading-tight text-text-tertiary">{ROLE_LABELS[role] ?? ''}</span>
            </span>
            <ChevronDown className={cn('size-3.5 text-text-tertiary transition-transform', isMenuOpen && 'rotate-180')} />
          </button>

          {isMenuOpen && (
            <div
              role="menu"
              className="absolute right-0 top-12 z-30 w-48 overflow-hidden rounded-md border border-border bg-surface shadow-xl"
            >
              <Link
                to={ROUTES.PROFILE}
                role="menuitem"
                onClick={() => setIsMenuOpen(false)}
                className="block px-3.5 py-2.5 text-sm text-text-secondary hover:bg-surface-raised hover:text-text-primary"
              >
                Profile settings
              </Link>
              <button
                type="button"
                role="menuitem"
                onClick={logout}
                className="flex w-full items-center gap-2 border-t border-border-soft px-3.5 py-2.5 text-left text-sm text-danger hover:bg-surface-raised"
              >
                <LogOut className="size-3.5" />
                Log out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}
