import { useCallback, useEffect, useRef, useState } from 'react'
import { Search, Users2 } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { EmptyState } from '@/components/ui/EmptyState'
import { Select } from '@/components/ui/Input'
import { AlumniCard } from './AlumniCard'
import { alumniApi } from '@/api/alumniApi'
import { Spinner } from '@/components/ui/Spinner'

export function AlumniDirectoryPage() {
  const [query, setQuery] = useState('')
  const [department, setDepartment] = useState('')
  const [graduationYear, setGraduationYear] = useState('')
  const [alumni, setAlumni] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const debounceRef = useRef(null)

  const fetchAlumni = useCallback(async (q, dept, year, pg) => {
    setLoading(true)
    setError(null)
    try {
      const res = await alumniApi.search({
        query: q || undefined,
        department: dept || undefined,
        graduationYear: year || undefined,
        page: pg,
        size: 24,
      })
      setAlumni(res.items ?? res.content ?? [])
      setTotalPages(res.totalPages ?? 1)
    } catch {
      setError('Failed to load alumni. Please try again.')
    } finally {
      setLoading(false)
    }
  }, [])

  // Debounced search
  useEffect(() => {
    clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => {
      setPage(0)
      fetchAlumni(query, department, graduationYear, 0)
    }, 300)
    return () => clearTimeout(debounceRef.current)
  }, [query, department, graduationYear, fetchAlumni])

  useEffect(() => {
    fetchAlumni(query, department, graduationYear, page)
  }, [page]) // eslint-disable-line react-hooks/exhaustive-deps

  const filtered = alumni // server-filtered

  return (
    <div>
      <PageHeader title="Alumni directory" description="Search and filter the institution's alumni records." />

      <div className="mb-5 flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-text-tertiary" />
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search by name, company, or skill…"
            className="h-10 w-full rounded-md border border-border bg-surface-sunken pl-9 pr-3 text-sm text-text-primary placeholder:text-text-tertiary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/50"
          />
        </div>
        <Select value={department} onChange={(e) => setDepartment(e.target.value)} className="sm:w-52">
          <option value="">All departments</option>
          {['Computer Science','Information Technology','Electronics','Mechanical','Civil','MBA'].map((d) => (
            <option key={d} value={d}>{d}</option>
          ))}
        </Select>
        <Select value={graduationYear} onChange={(e) => setGraduationYear(e.target.value)} className="sm:w-40">
          <option value="">All batches</option>
          {[2020,2021,2022,2023,2024,2025].map((y) => (
            <option key={y} value={y}>{y}</option>
          ))}
        </Select>
      </div>

      {loading ? (
        <div className="flex justify-center py-16"><Spinner /></div>
      ) : error ? (
        <p className="text-center text-sm text-red-500 py-16">{error}</p>
      ) : filtered.length === 0 ? (
        <EmptyState
          icon={Users2}
          title="No matching alumni"
          description="Try a different name, department, or graduation year."
        />
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {filtered.map((alumnus) => (
              <AlumniCard key={alumnus.id} alumnus={alumnus} />
            ))}
          </div>
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-3 mt-6">
              <button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}
                className="px-3 py-1.5 rounded-lg border text-sm disabled:opacity-40 hover:bg-gray-50 transition-colors">← Prev</button>
              <span className="text-sm text-gray-500">Page {page + 1} of {totalPages}</span>
              <button onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
                className="px-3 py-1.5 rounded-lg border text-sm disabled:opacity-40 hover:bg-gray-50 transition-colors">Next →</button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
