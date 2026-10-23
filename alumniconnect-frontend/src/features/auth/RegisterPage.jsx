import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { UserPlus } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { Card, CardBody } from '@/components/ui/Card'
import { ROUTES } from '@/utils/constants'

const initialForm = {
  fullName: '',
  email: '',
  enrollmentNumber: '',
  department: '',
  password: '',
  confirmPassword: '',
}

export function RegisterPage() {
  const { registerStudent } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  function handleChange(event) {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setIsSubmitting(true)
    try {
      const { confirmPassword: _confirmPassword, ...payload } = form
      await registerStudent(payload)
      navigate(ROUTES.DASHBOARD, { replace: true })
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not create your account. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div>
      <div className="mb-6 text-center">
        <h1 className="font-display text-2xl font-medium text-text-primary">Create a student account</h1>
        <p className="mt-1 text-sm text-text-secondary">Use your institutional email to register.</p>
      </div>

      <Card>
        <CardBody>
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <Input label="Full name" name="fullName" required value={form.fullName} onChange={handleChange} />
            <Input
              label="Institutional email"
              type="email"
              name="email"
              required
              value={form.email}
              onChange={handleChange}
              placeholder="you@svvv.edu.in"
            />
            <div className="grid grid-cols-2 gap-3">
              <Input
                label="Enrollment number"
                name="enrollmentNumber"
                required
                value={form.enrollmentNumber}
                onChange={handleChange}
                placeholder="CS2023014"
              />
              <Input
                label="Department"
                name="department"
                required
                value={form.department}
                onChange={handleChange}
                placeholder="Computer Science"
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <Input
                label="Password"
                type="password"
                name="password"
                required
                value={form.password}
                onChange={handleChange}
              />
              <Input
                label="Confirm password"
                type="password"
                name="confirmPassword"
                required
                value={form.confirmPassword}
                onChange={handleChange}
              />
            </div>

            {error && <p className="rounded-md bg-danger-soft px-3 py-2 text-sm text-danger">{error}</p>}

            <Button type="submit" size="lg" isLoading={isSubmitting} leftIcon={<UserPlus className="size-4" />}>
              Create account
            </Button>
          </form>
        </CardBody>
      </Card>

      <p className="mt-5 text-center text-sm text-text-secondary">
        Already have an account?{' '}
        <Link to={ROUTES.LOGIN} className="font-medium text-accent hover:text-accent-hover">
          Sign in
        </Link>
      </p>
    </div>
  )
}
