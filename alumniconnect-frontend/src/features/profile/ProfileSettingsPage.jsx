import { useEffect, useState } from 'react'
import { Check, Lock, Pencil } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card, CardHeader, CardTitle, CardBody, CardFooter } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { useAuth } from '@/hooks/useAuth'
import { alumniApi } from '@/api/alumniApi'
import { studentApi } from '@/api/studentApi'
import { ROLES } from '@/utils/constants'
import { Spinner } from '@/components/ui/Spinner'

export function ProfileSettingsPage() {
  const { role } = useAuth()
  return role === ROLES.STUDENT ? <StudentProfileForm /> : <AlumniProfileForm />
}

// ---------- Alumni ----------

const ALUMNI_DEFAULTS = {
  enrollmentNumber: '', degree: '', department: '', batch: '',
  email: '', phone: '', location: '', currentCompany: '', jobTitle: '',
  industry: '', linkedin: '', github: '', skills: [], summary: '',
}

function AlumniProfileForm() {
  const [form, setForm] = useState(ALUMNI_DEFAULTS)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [savedAt, setSavedAt] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    alumniApi.getOwn()
      .then((data) => setForm({ ...ALUMNI_DEFAULTS, ...data, skills: data.skills ?? [] }))
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [])

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
    setSavedAt(null)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await alumniApi.updateOwn(form)
      setSavedAt(new Date())
    } catch {
      setError('Failed to save. Please try again.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  return (
    <div>
      <PageHeader title="Profile settings" description="Update the fields you control. Academic records stay with the institution." />

      <form onSubmit={handleSubmit} className="flex flex-col gap-5">
        <Card>
          <CardHeader>
            <CardTitle>Academic record</CardTitle>
            <span className="flex items-center gap-1 text-[11px] text-text-tertiary">
              <Lock className="size-3" /> Institution-controlled
            </span>
          </CardHeader>
          <CardBody className="grid gap-4 sm:grid-cols-2">
            <Input label="Enrollment number" value={form.enrollmentNumber} disabled />
            <Input label="Degree" value={form.degree} disabled />
            <Input label="Department" value={form.department} disabled />
            <Input label="Batch" value={form.batch} disabled />
          </CardBody>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Contact &amp; professional details</CardTitle>
            <span className="flex items-center gap-1 text-[11px] text-text-tertiary">
              <Pencil className="size-3" /> You control this
            </span>
          </CardHeader>
          <CardBody className="grid gap-4 sm:grid-cols-2">
            <Input label="Email" type="email" value={form.email} onChange={(e) => update('email', e.target.value)} />
            <Input label="Phone" value={form.phone} onChange={(e) => update('phone', e.target.value)} />
            <Input label="Current location" value={form.location} onChange={(e) => update('location', e.target.value)} />
            <Input label="Current company" value={form.currentCompany} onChange={(e) => update('currentCompany', e.target.value)} />
            <Input label="Job title" value={form.jobTitle} onChange={(e) => update('jobTitle', e.target.value)} />
            <Input label="Industry" value={form.industry} onChange={(e) => update('industry', e.target.value)} />
            <Input label="LinkedIn URL" value={form.linkedin} onChange={(e) => update('linkedin', e.target.value)} />
            <Input label="GitHub URL" value={form.github} onChange={(e) => update('github', e.target.value)} />
            <Input
              label="Skills (comma separated)"
              className="sm:col-span-2"
              value={form.skills.join(', ')}
              onChange={(e) =>
                update('skills', e.target.value.split(',').map((s) => s.trim()).filter(Boolean))
              }
            />
            <div className="flex flex-col gap-1.5 sm:col-span-2">
              <label className="text-sm font-medium text-text-secondary">Professional summary</label>
              <textarea
                rows={3}
                value={form.summary}
                onChange={(e) => update('summary', e.target.value)}
                className="rounded-md border border-border bg-surface-sunken px-3 py-2 text-sm text-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/50"
              />
            </div>
          </CardBody>
          <CardFooter className="justify-end gap-3">
            {error && <span className="text-xs text-danger">{error}</span>}
            {savedAt && !error && (
              <span className="flex items-center gap-1 text-xs text-success">
                <Check className="size-3.5" /> Saved
              </span>
            )}
            <Button type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Save changes'}
            </Button>
          </CardFooter>
        </Card>
      </form>
    </div>
  )
}

// ---------- Student ----------

const STUDENT_DEFAULTS = {
  enrollmentNumber: '', department: '', batch: '',
  email: '', skills: [], careerInterests: '',
}

function StudentProfileForm() {
  const [form, setForm] = useState(STUDENT_DEFAULTS)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [savedAt, setSavedAt] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    studentApi.getOwn()
      .then((data) => setForm({ ...STUDENT_DEFAULTS, ...data, skills: data.skills ?? [] }))
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [])

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
    setSavedAt(null)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await studentApi.updateOwn(form)
      setSavedAt(new Date())
    } catch {
      setError('Failed to save. Please try again.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  return (
    <div>
      <PageHeader title="Profile settings" description="Personal and academic info, skills, and career interests." />

      <form onSubmit={handleSubmit} className="flex flex-col gap-5">
        <Card>
          <CardHeader>
            <CardTitle>Academic record</CardTitle>
            <span className="flex items-center gap-1 text-[11px] text-text-tertiary">
              <Lock className="size-3" /> Institution-controlled
            </span>
          </CardHeader>
          <CardBody className="grid gap-4 sm:grid-cols-2">
            <Input label="Enrollment number" value={form.enrollmentNumber} disabled />
            <Input label="Department" value={form.department} disabled />
            <Input label="Batch" value={form.batch} disabled />
          </CardBody>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Personal &amp; career interests</CardTitle>
            <span className="flex items-center gap-1 text-[11px] text-text-tertiary">
              <Pencil className="size-3" /> You control this
            </span>
          </CardHeader>
          <CardBody className="grid gap-4 sm:grid-cols-2">
            <Input label="Email" type="email" value={form.email} onChange={(e) => update('email', e.target.value)} />
            <Input
              label="Skills (comma separated)"
              value={form.skills.join(', ')}
              onChange={(e) =>
                update('skills', e.target.value.split(',').map((s) => s.trim()).filter(Boolean))
              }
            />
            <div className="flex flex-col gap-1.5 sm:col-span-2">
              <label className="text-sm font-medium text-text-secondary">Career interests</label>
              <textarea
                rows={2}
                value={form.careerInterests}
                onChange={(e) => update('careerInterests', e.target.value)}
                className="rounded-md border border-border bg-surface-sunken px-3 py-2 text-sm text-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/50"
              />
            </div>
          </CardBody>
          <CardFooter className="justify-end gap-3">
            {error && <span className="text-xs text-danger">{error}</span>}
            {savedAt && !error && (
              <span className="flex items-center gap-1 text-xs text-success">
                <Check className="size-3.5" /> Saved
              </span>
            )}
            <Button type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Save changes'}
            </Button>
          </CardFooter>
        </Card>
      </form>
    </div>
  )
}
