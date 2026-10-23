import { Link, Outlet } from 'react-router-dom'
import { GraduationCap } from 'lucide-react'
import { ROUTES } from '@/utils/constants'

export function AuthLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-bg">
      <header className="px-6 py-5">
        <Link to={ROUTES.HOME} className="inline-flex items-center gap-2">
          <GraduationCap className="size-5 text-accent" aria-hidden />
          <span className="font-display text-lg leading-none">
            <span className="font-medium text-text-primary">Alumni</span>
            <span className="font-semibold text-accent">Connect</span>
          </span>
        </Link>
      </header>

      <main className="flex flex-1 items-center justify-center px-4 pb-16">
        <div className="w-full max-w-md">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
