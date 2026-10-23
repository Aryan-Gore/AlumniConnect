import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { KeyRound, Search, ShieldCheck } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { authApi } from '@/api/authApi'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { Card, CardBody } from '@/components/ui/Card'
import { ROUTES } from '@/utils/constants'

const lookupInitial = { fullName: '', enrollmentNumber: '', email: '', graduationYear: '' }

export function ClaimProfilePage() {
  const { claimProfile } = useAuth()
  const navigate = useNavigate()

  const [step, setStep] = useState('lookup') // 'lookup' | 'confirm'
  const [lookupForm, setLookupForm] = useState(lookupInitial)
  const [matchedRecord, setMatchedRecord] = useState(null)
  const [passwordForm, setPasswordForm] = useState({ password: '', confirmPassword: '' })
  const [error, setError] = useState(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleLookup(event) {
    event.preventDefault()
    setError(null)
    setIsSubmitting(true)
    try {
      const record = await authApi.findClaimableRecord(lookupForm)
      setMatchedRecord(record)
      setStep('confirm')
    } catch (err) {
      setError(
        err.response?.data?.message ??
          'No matching alumni record was found. Double-check your details or ask the admin office to invite you.'
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleConfirm(event) {
    event.preventDefault()
    setError(null)

    if (passwordForm.password !== passwordForm.confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setIsSubmitting(true)
    try {
      await claimProfile({
        alumniId: matchedRecord.id,
        ...lookupForm,
        password: passwordForm.password,
      })
      navigate(ROUTES.DASHBOARD, { replace: true })
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not complete the claim. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div>
      <div className="mb-6 text-center">
        <h1 className="font-display text-2xl font-medium text-text-primary">Claim your alumni profile</h1>
        <p className="mt-1 text-sm text-text-secondary">
          Your institution likely already has a record for you — this links it to a login, it doesn't create a new one.
        </p>
      </div>

      {step === 'lookup' && (
        <Card>
          <CardBody>
            <form onSubmit={handleLookup} className="flex flex-col gap-4">
              <Input
                label="Full name (as on record)"
                name="fullName"
                required
                value={lookupForm.fullName}
                onChange={(e) => setLookupForm((p) => ({ ...p, fullName: e.target.value }))}
              />
              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="Enrollment number"
                  name="enrollmentNumber"
                  required
                  value={lookupForm.enrollmentNumber}
                  onChange={(e) => setLookupForm((p) => ({ ...p, enrollmentNumber: e.target.value }))}
                  placeholder="CS2019014"
                />
                <Input
                  label="Graduation year"
                  name="graduationYear"
                  required
                  value={lookupForm.graduationYear}
                  onChange={(e) => setLookupForm((p) => ({ ...p, graduationYear: e.target.value }))}
                  placeholder="2023"
                />
              </div>
              <Input
                label="Email on file"
                type="email"
                name="email"
                required
                value={lookupForm.email}
                onChange={(e) => setLookupForm((p) => ({ ...p, email: e.target.value }))}
              />

              {error && <p className="rounded-md bg-danger-soft px-3 py-2 text-sm text-danger">{error}</p>}

              <Button type="submit" size="lg" isLoading={isSubmitting} leftIcon={<Search className="size-4" />}>
                Find my record
              </Button>
            </form>
          </CardBody>
        </Card>
      )}

      {step === 'confirm' && matchedRecord && (
        <div className="flex flex-col gap-4">
          <Card>
            <CardBody>
              <p className="mb-3 flex items-center gap-1.5 text-xs font-medium uppercase tracking-wider text-success">
                <ShieldCheck className="size-3.5" /> Matching record found
              </p>
              <p className="font-display text-lg text-text-primary">{matchedRecord.fullName}</p>
              <p className="mt-1 font-mono text-xs text-text-tertiary">
                {matchedRecord.enrollmentNumber} · {matchedRecord.department} · Batch {matchedRecord.graduationYear}
              </p>
            </CardBody>
          </Card>

          <Card>
            <CardBody>
              <form onSubmit={handleConfirm} className="flex flex-col gap-4">
                <Input
                  label="Set a password"
                  type="password"
                  required
                  value={passwordForm.password}
                  onChange={(e) => setPasswordForm((p) => ({ ...p, password: e.target.value }))}
                />
                <Input
                  label="Confirm password"
                  type="password"
                  required
                  value={passwordForm.confirmPassword}
                  onChange={(e) => setPasswordForm((p) => ({ ...p, confirmPassword: e.target.value }))}
                />

                {error && <p className="rounded-md bg-danger-soft px-3 py-2 text-sm text-danger">{error}</p>}

                <Button type="submit" size="lg" isLoading={isSubmitting} leftIcon={<KeyRound className="size-4" />}>
                  Activate my account
                </Button>
              </form>
            </CardBody>
          </Card>
        </div>
      )}

      <p className="mt-5 text-center text-sm text-text-secondary">
        Not an alumnus?{' '}
        <Link to={ROUTES.REGISTER} className="font-medium text-accent hover:text-accent-hover">
          Register as a student
        </Link>
      </p>
    </div>
  )
}
