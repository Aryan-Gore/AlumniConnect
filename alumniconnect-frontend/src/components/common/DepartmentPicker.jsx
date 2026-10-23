import { useEffect, useMemo, useState } from 'react'
import { instituteApi } from '@/api/instituteApi'
import { Select } from '@/components/ui/Input'

/**
 * Institute → Department cascading selects, fed by GET /institutes.
 *
 *  - `value` is the chosen department id (number) or null/''.
 *  - `onChange(departmentId | null)` fires when a department is picked, and with
 *    null when the institute changes (the old department no longer applies).
 *  - Filters: pass `allowAll` to add an "All departments" choice, and
 *    `onInstituteChange(instituteId | null)` to also learn about the institute on
 *    its own (so "everything in this institute" can be searched).
 *  - `instituteValue` lets a parent control the institute; otherwise it is
 *    derived from `value` and remembered locally.
 */
export function DepartmentPicker({
  value,
  onChange,
  instituteValue,
  onInstituteChange,
  label = 'Department',
  instituteLabel = 'Institute',
  error,
  disabled = false,
  required = false,
  allowAll = false,
  allInstitutesLabel = 'All institutes',
  allDepartmentsLabel = 'All departments',
  className,
}) {
  const [institutes, setInstitutes] = useState([])
  const [status, setStatus] = useState('loading') // loading | ready | failed
  const [localInstituteId, setLocalInstituteId] = useState('')
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    let cancelled = false
    instituteApi
      .list()
      .then((data) => {
        if (cancelled) return
        setInstitutes(data)
        setStatus('ready')
      })
      .catch(() => {
        if (!cancelled) setStatus('failed')
      })
    return () => {
      cancelled = true
    }
  }, [attempt])

  const departmentId = value === null || value === undefined || value === '' ? '' : String(value)

  // The institute that owns the selected department wins; otherwise what the user last picked.
  const derivedInstituteId = useMemo(() => {
    if (!departmentId) return ''
    const owner = institutes.find((institute) => institute.departments.some((d) => String(d.id) === departmentId))
    return owner ? String(owner.id) : ''
  }, [institutes, departmentId])

  const controlled = instituteValue !== undefined
  const instituteId = controlled
    ? instituteValue === null || instituteValue === ''
      ? ''
      : String(instituteValue)
    : derivedInstituteId || localInstituteId

  const departments = useMemo(
    () => institutes.find((institute) => String(institute.id) === instituteId)?.departments ?? [],
    [institutes, instituteId]
  )

  function handleInstituteChange(event) {
    const next = event.target.value
    if (!controlled) setLocalInstituteId(next)
    onInstituteChange?.(next ? Number(next) : null)
    onChange(null)
  }

  function handleDepartmentChange(event) {
    onChange(event.target.value ? Number(event.target.value) : null)
  }

  if (status === 'failed') {
    return (
      <div className={className}>
        <p className="text-sm text-danger">
          Couldn&apos;t load the list of departments.{' '}
          <button
            type="button"
            className="font-medium underline"
            onClick={() => {
              setStatus('loading')
              setAttempt((n) => n + 1)
            }}
          >
            Try again
          </button>
        </p>
      </div>
    )
  }

  return (
    <div className={className ?? 'grid gap-3 sm:grid-cols-2'}>
      <Select
        label={instituteLabel}
        value={instituteId}
        onChange={handleInstituteChange}
        disabled={disabled || status === 'loading'}
        required={required && !allowAll}
      >
        <option value="">{status === 'loading' ? 'Loading…' : allowAll ? allInstitutesLabel : 'Select an institute'}</option>
        {institutes.map((institute) => (
          <option key={institute.id} value={institute.id}>
            {institute.name}
          </option>
        ))}
      </Select>
      <Select
        label={label}
        value={departmentId}
        onChange={handleDepartmentChange}
        disabled={disabled || !instituteId}
        required={required && !allowAll}
        error={error}
      >
        <option value="">{allowAll ? allDepartmentsLabel : instituteId ? 'Select a department' : 'Choose an institute first'}</option>
        {departments.map((department) => (
          <option key={department.id} value={department.id}>
            {department.name}
          </option>
        ))}
      </Select>
    </div>
  )
}
