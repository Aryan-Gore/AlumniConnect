import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { LogIn } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { Card, CardBody } from '@/components/ui/Card'
import { ROUTES } from '@/utils/constants'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const redirectTo = location.state?.from?.pathname ?? ROUTES.DASHBOARD

  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  function handleChange(event) {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setIsSubmitting(true)
    try {
      await login(form)
      navigate(redirectTo, { replace: true })
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not sign in. Check your credentials and try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div>
      <div className="mb-6 text-center">
        <h1 className="font-display text-2xl font-medium text-text-primary">Sign in</h1>
        <p className="mt-1 text-sm text-text-secondary">Access your student, alumni, or admin account.</p>
      </div>

      <Card>
        <CardBody>
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <Input
              label="Email"
              type="email"
              name="email"
              autoComplete="email"
              required
              value={form.email}
              onChange={handleChange}
              placeholder="you@example.edu"
            />
            <Input
              label="Password"
              type="password"
              name="password"
              autoComplete="current-password"
              required
              value={form.password}
              onChange={handleChange}
              placeholder="••••••••"
            />

            {error && <p className="rounded-md bg-danger-soft px-3 py-2 text-sm text-danger">{error}</p>}

            <Button type="submit" size="lg" isLoading={isSubmitting} leftIcon={<LogIn className="size-4" />}>
              Sign in
            </Button>
          </form>
        </CardBody>
      </Card>

      <div className="mt-5 flex flex-col items-center gap-1.5 text-center text-sm text-text-secondary">
        <p>
          New student?{' '}
          <Link to={ROUTES.REGISTER} className="font-medium text-accent hover:text-accent-hover">
            Create an account
          </Link>
        </p>
        <p>
          Graduated already?{' '}
          <Link to={ROUTES.CLAIM_PROFILE} className="font-medium text-accent hover:text-accent-hover">
            Claim your alumni profile
          </Link>
        </p>
      </div>
    </div>
  )
}
