import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { X, GraduationCap } from 'lucide-react'
import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'
import { NavItem } from './NavItem'
import { NAV_BY_ROLE, FOOTER_NAV } from './navConfig'
import { ChatProvider } from '@/features/chat/ChatProvider'
import { useAuth } from '@/hooks/useAuth'

export function AppShell() {
  const [isMobileNavOpen, setIsMobileNavOpen] = useState(false)
  const { role } = useAuth()
  const items = [...(NAV_BY_ROLE[role] ?? []), ...FOOTER_NAV]

  // ChatProvider lives here, not on a page: the socket and the unread badge have to
  // keep working while the person is reading the feed or editing their profile.
  return (
    <ChatProvider>
      <div className="flex min-h-screen bg-bg">
        <Sidebar />

        {isMobileNavOpen && (
          <div className="fixed inset-0 z-40 flex md:hidden">
            <div
              className="absolute inset-0 bg-black/60"
              onClick={() => setIsMobileNavOpen(false)}
              aria-hidden="true"
            />
            <div className="relative flex w-64 flex-col bg-surface">
              <div className="flex h-16 items-center justify-between border-b border-border px-4">
                <span className="flex items-center gap-2 font-display text-lg">
                  <GraduationCap className="size-5 text-accent" />
                  <span>
                    <span className="text-text-primary">Alumni</span>
                    <span className="font-semibold text-accent">Connect</span>
                  </span>
                </span>
                <button onClick={() => setIsMobileNavOpen(false)} aria-label="Close navigation">
                  <X className="size-5 text-text-secondary" />
                </button>
              </div>
              <nav className="flex flex-1 flex-col gap-0.5 overflow-y-auto p-3" aria-label="Main">
                {items.map((item) => (
                  <NavItem
                    key={item.to + item.label}
                    {...item}
                    onNavigate={() => setIsMobileNavOpen(false)}
                    className="py-2.5"
                  />
                ))}
              </nav>
            </div>
          </div>
        )}

        <div className="flex min-h-screen min-w-0 flex-1 flex-col">
          <Topbar onMenuClick={() => setIsMobileNavOpen(true)} />
          <main className="flex-1 overflow-y-auto px-4 py-6 md:px-8 md:py-8">
            <div className="mx-auto max-w-6xl">
              <Outlet />
            </div>
          </main>
        </div>
      </div>
    </ChatProvider>
  )
}
