import { useRef, useState } from 'react'
import { AlertTriangle, Check, FileSpreadsheet, UploadCloud } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { Card, CardHeader, CardTitle, CardBody, CardFooter } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Select } from '@/components/ui/Input'
import { cn } from '@/utils/cn'

const STEPS = ['Upload', 'Map columns', 'Preview & duplicates', 'Confirm']

const SYSTEM_FIELDS = [
  'Full name',
  'Email',
  'Enrollment number',
  'Department',
  'Degree',
  'Batch',
  'Graduation year',
  'Phone',
  'Company',
  'Job title',
  'Industry',
  'Location',
  '— Ignore column —',
]

// Standing in for a parsed file until the upload actually hits Spring Boot
// (which hands the file to the Python/Pandas cleaning step internally).
const MOCK_DETECTED_COLUMNS = ['Name', 'Email ID', 'Enroll_No', 'Dept', 'Batch Year', 'Company Name']
const MOCK_PREVIEW_ROWS = [
  { name: 'Ritika Anand', email: 'ritika.a@example.com', enrollment: 'CS2019014', flag: null },
  { name: 'Karan Mehta', email: 'karan.m@example.com', enrollment: 'CS2018041', flag: null },
  { name: 'Ritika Anand', email: 'ritika.anand@example.com', enrollment: 'CS2019014', flag: 'duplicate' },
  { name: 'Unlisted Dept Row', email: 'unknown@example.com', enrollment: '—', flag: 'error' },
]

export function CsvImportPage() {
  const [step, setStep] = useState(0)
  const [fileName, setFileName] = useState(null)
  const [isDragging, setIsDragging] = useState(false)
  const [mapping, setMapping] = useState({})
  const [isProcessing, setIsProcessing] = useState(false)
  const [importResult, setImportResult] = useState(null)
  const fileInputRef = useRef(null)

  function handleFileSelected(file) {
    if (!file) return
    setFileName(file.name)
    // Real version: alumniApi.uploadImportFile(file) -> returns an importId,
    // then submitColumnMapping / getImportPreview drive steps 2-3.
    setStep(1)
  }

  function handleConfirmImport() {
    setIsProcessing(true)
    setTimeout(() => {
      setImportResult({ total: 520, successful: 510, failed: 10, duplicatesSkipped: 8 })
      setIsProcessing(false)
      setStep(3)
    }, 900)
  }

  return (
    <div>
      <PageHeader title="Alumni CSV / Excel import" description="Bulk-load institutional records into the central database." />

      <div className="mb-6 flex items-center gap-2">
        {STEPS.map((label, index) => (
          <div key={label} className="flex flex-1 items-center gap-2">
            <div
              className={cn(
                'flex size-7 shrink-0 items-center justify-center rounded-full font-mono text-xs',
                index < step
                  ? 'bg-success text-white'
                  : index === step
                    ? 'bg-accent text-white'
                    : 'bg-surface-raised text-text-tertiary'
              )}
            >
              {index < step ? <Check className="size-3.5" /> : index + 1}
            </div>
            <span className={cn('hidden text-xs sm:block', index === step ? 'text-text-primary' : 'text-text-tertiary')}>
              {label}
            </span>
            {index < STEPS.length - 1 && <div className="h-px flex-1 bg-border" />}
          </div>
        ))}
      </div>

      {step === 0 && (
        <Card>
          <CardBody>
            <div
              onDragOver={(e) => {
                e.preventDefault()
                setIsDragging(true)
              }}
              onDragLeave={() => setIsDragging(false)}
              onDrop={(e) => {
                e.preventDefault()
                setIsDragging(false)
                handleFileSelected(e.dataTransfer.files?.[0])
              }}
              className={cn(
                'flex flex-col items-center justify-center gap-3 rounded-lg border-2 border-dashed px-6 py-16 text-center transition-colors',
                isDragging ? 'border-accent bg-accent-soft' : 'border-border'
              )}
            >
              <UploadCloud className="size-8 text-text-tertiary" />
              <div>
                <p className="text-sm font-medium text-text-primary">Drag a CSV or Excel file here</p>
                <p className="text-xs text-text-tertiary">or click below to browse</p>
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept=".csv,.xlsx,.xls"
                className="hidden"
                onChange={(e) => handleFileSelected(e.target.files?.[0])}
              />
              <Button variant="secondary" size="sm" onClick={() => fileInputRef.current?.click()}>
                Choose file
              </Button>
            </div>
          </CardBody>
        </Card>
      )}

      {step === 1 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileSpreadsheet className="size-4 text-accent" /> {fileName}
            </CardTitle>
          </CardHeader>
          <CardBody>
            <p className="mb-4 text-sm text-text-secondary">Map each detected column to a system field.</p>
            <div className="grid gap-3 sm:grid-cols-2">
              {MOCK_DETECTED_COLUMNS.map((column) => (
                <div key={column} className="flex items-center gap-3">
                  <span className="w-32 shrink-0 truncate font-mono text-xs text-text-tertiary">{column}</span>
                  <Select
                    value={mapping[column] ?? ''}
                    onChange={(e) => setMapping((prev) => ({ ...prev, [column]: e.target.value }))}
                  >
                    <option value="">Select field…</option>
                    {SYSTEM_FIELDS.map((field) => (
                      <option key={field} value={field}>
                        {field}
                      </option>
                    ))}
                  </Select>
                </div>
              ))}
            </div>
          </CardBody>
          <CardFooter className="justify-end">
            <Button onClick={() => setStep(2)}>Continue to preview</Button>
          </CardFooter>
        </Card>
      )}

      {step === 2 && (
        <Card>
          <CardHeader>
            <CardTitle>Preview — 4 of 520 rows shown</CardTitle>
          </CardHeader>
          <CardBody className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-border-soft text-xs uppercase tracking-wider text-text-tertiary">
                  <th className="pb-2 pr-4 font-medium">Name</th>
                  <th className="pb-2 pr-4 font-medium">Email</th>
                  <th className="pb-2 pr-4 font-medium">Enrollment</th>
                  <th className="pb-2 font-medium">Status</th>
                </tr>
              </thead>
              <tbody>
                {MOCK_PREVIEW_ROWS.map((row, i) => (
                  <tr key={i} className="border-b border-border-soft last:border-0">
                    <td className="py-2.5 pr-4 text-text-primary">{row.name}</td>
                    <td className="py-2.5 pr-4 font-mono text-xs text-text-secondary">{row.email}</td>
                    <td className="py-2.5 pr-4 font-mono text-xs text-text-secondary">{row.enrollment}</td>
                    <td className="py-2.5">
                      {row.flag === 'duplicate' && (
                        <span className="inline-flex items-center gap-1 text-xs text-warning">
                          <AlertTriangle className="size-3" /> Possible duplicate
                        </span>
                      )}
                      {row.flag === 'error' && (
                        <span className="inline-flex items-center gap-1 text-xs text-danger">
                          <AlertTriangle className="size-3" /> Missing department
                        </span>
                      )}
                      {!row.flag && (
                        <span className="inline-flex items-center gap-1 text-xs text-success">
                          <Check className="size-3" /> Ready
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardBody>
          <CardFooter className="justify-end gap-2">
            <Button variant="secondary" onClick={() => setStep(1)}>
              Back to mapping
            </Button>
            <Button onClick={handleConfirmImport} isLoading={isProcessing}>
              Confirm import
            </Button>
          </CardFooter>
        </Card>
      )}

      {step === 3 && importResult && (
        <Card>
          <CardBody className="flex flex-col items-center py-10 text-center">
            <div className="flex size-12 items-center justify-center rounded-full bg-success-soft text-success">
              <Check className="size-6" />
            </div>
            <p className="mt-4 font-display text-lg text-text-primary">Import complete</p>
            <div className="mt-6 grid grid-cols-3 gap-8">
              <Stat label="Total rows" value={importResult.total} />
              <Stat label="Inserted" value={importResult.successful} tone="text-success" />
              <Stat label="Failed" value={importResult.failed} tone="text-danger" />
            </div>
            <p className="mt-4 text-xs text-text-tertiary">
              {importResult.duplicatesSkipped} rows were skipped as likely duplicates of existing records.
            </p>
          </CardBody>
        </Card>
      )}
    </div>
  )
}

function Stat({ label, value, tone = 'text-text-primary' }) {
  return (
    <div>
      <p className={cn('font-mono text-2xl font-semibold tabular-nums', tone)}>{value}</p>
      <p className="text-xs text-text-tertiary">{label}</p>
    </div>
  )
}
