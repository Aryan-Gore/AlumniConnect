import { Link } from 'react-router-dom'
import { Compass } from 'lucide-react'
import { Button } from '@/components/ui/Button'
import { ROUTES } from '@/utils/constants'

export function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-bg px-6 text-center">
      <Compass className="size-10 text-text-tertiary" />
      <div>
        <p className="font-mono text-sm text-accent">404</p>
        <h1 className="font-display text-2xl text-text-primary">This page isn't on record</h1>
        <p className="mt-1 text-sm text-text-secondary">The page you're looking for doesn't exist or has moved.</p>
      </div>
      <Button as={Link} to={ROUTES.HOME}>
        Back to home
      </Button>
    </div>
  )
}
