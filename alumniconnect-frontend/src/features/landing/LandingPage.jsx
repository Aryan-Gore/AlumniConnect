import { Link } from 'react-router-dom'
import {
  GraduationCap,
  Database,
  Users2,
  ArrowRight,
  FileSpreadsheet,
  Handshake,
  CalendarDays,
  Briefcase,
} from 'lucide-react'
import { Button } from '@/components/ui/Button'
import { publicApi } from '@/api/publicApi'
import { formatCount } from '@/utils/formatters'
import { ROUTES } from '@/utils/constants'
import { useEffect, useState } from 'react'

const FALLBACK_STATS = [
  { label: 'Alumni records', value: 0 },
  { label: 'Claimed profiles', value: 0 },
  { label: 'Active mentorships', value: 0 },
]

export function LandingPage() {
  const [stats, setStats] = useState(FALLBACK_STATS)

  useEffect(() => {
    publicApi.stats().then((data) => {
      if (!data) return
      setStats([
        { label: 'Alumni records', value: data.alumniRecords ?? data.totalAlumni ?? 0 },
        { label: 'Claimed profiles', value: data.alumniMembers ?? data.claimedProfiles ?? 0 },
        { label: 'Active mentorships', value: data.activeMentorships ?? 0 },
      ])
    }).catch(() => {})
  }, [])

  return (
    <div className="bg-bg">
      <header className="mx-auto flex max-w-6xl items-center justify-between px-6 py-6">
        <span className="flex items-center gap-2">
          <GraduationCap className="size-5 text-accent" aria-hidden />
          <span className="font-display text-lg leading-none">
            <span className="font-medium text-text-primary">Alumni</span>
            <span className="font-semibold text-accent">Connect</span>
          </span>
        </span>
        <nav className="flex items-center gap-2">
          <Button as={Link} to={ROUTES.LOGIN} variant="ghost" size="sm">
            Sign in
          </Button>
          <Button as={Link} to={ROUTES.CLAIM_PROFILE} variant="secondary" size="sm">
            Claim profile
          </Button>
        </nav>
      </header>

      {/* Hero */}
      <section className="mx-auto max-w-4xl px-6 py-20 text-center">
        <p className="mb-4 font-mono text-xs uppercase tracking-[0.2em] text-accent">
          Shree Vaishnav Vidyapeeth Vishwavidyalay
        </p>
        <h1 className="text-balance font-display text-4xl font-medium leading-[1.1] text-text-primary sm:text-5xl">
          One record per alumnus, <strong className="font-semibold text-accent">for life</strong>.
        </h1>
        <p className="mx-auto mt-5 max-w-xl text-balance text-base text-text-secondary">
          AlumniConnect replaces scattered spreadsheets and WhatsApp groups with a single, structured record
          for every graduate — and a real reason for them to keep it updated.
        </p>
        <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
          <Button as={Link} to={ROUTES.REGISTER} size="lg" rightIcon={<ArrowRight className="size-4" />}>
            I'm a current student
          </Button>
          <Button as={Link} to={ROUTES.CLAIM_PROFILE} variant="secondary" size="lg">
            I'm an alumnus
          </Button>
        </div>

        <dl className="mx-auto mt-16 grid max-w-xl grid-cols-3 divide-x divide-border-soft border-y border-border-soft py-6">
          {stats.map((stat) => (
            <div key={stat.label} className="px-2">
              <dt className="text-xs uppercase tracking-wider text-text-tertiary">{stat.label}</dt>
              <dd className="mt-1 font-mono text-2xl font-semibold tabular-nums text-text-primary">
                {formatCount(stat.value)}
              </dd>
            </div>
          ))}
        </dl>
      </section>

      {/* Two pillars */}
      <section className="mx-auto max-w-6xl px-6 py-16">
        <div className="grid gap-6 md:grid-cols-2">
          <div className="rounded-lg border border-border bg-surface p-7">
            <Database className="size-5 text-accent" aria-hidden />
            <h2 className="mt-4 font-display text-xl text-text-primary">Centralized data management</h2>
            <p className="mt-2 text-sm text-text-secondary">
              Every alumnus gets one structured record — imported in bulk by the institution, then claimed and
              kept current by the alumnus themselves. Nothing is ever duplicated.
            </p>
            <ul className="mt-5 space-y-2.5 text-sm text-text-secondary">
              <li className="flex items-center gap-2.5">
                <FileSpreadsheet className="size-4 shrink-0 text-text-tertiary" /> CSV/Excel import with duplicate
                detection
              </li>
              <li className="flex items-center gap-2.5">
                <Users2 className="size-4 shrink-0 text-text-tertiary" /> Searchable, filterable alumni directory
              </li>
            </ul>
          </div>

          <div className="rounded-lg border border-border bg-surface p-7">
            <Handshake className="size-5 text-accent" aria-hidden />
            <h2 className="mt-4 font-display text-xl text-text-primary">Alumni engagement</h2>
            <p className="mt-2 text-sm text-text-secondary">
              A structured reason to stay in touch — mentorship, events, and job referrals, run through the
              institution rather than an informal group chat.
            </p>
            <ul className="mt-5 space-y-2.5 text-sm text-text-secondary">
              <li className="flex items-center gap-2.5">
                <CalendarDays className="size-4 shrink-0 text-text-tertiary" /> Reunions, talks, and workshops with
                registration
              </li>
              <li className="flex items-center gap-2.5">
                <Briefcase className="size-4 shrink-0 text-text-tertiary" /> Alumni-posted job opportunities
              </li>
            </ul>
          </div>
        </div>
      </section>

      {/* Claim flow */}
      <section className="mx-auto max-w-4xl px-6 py-16">
        <h2 className="text-center font-display text-2xl text-text-primary">How claiming a profile works</h2>
        <div className="mt-10 grid grid-cols-2 gap-x-4 gap-y-8 sm:grid-cols-4">
          {['Imported', 'Invited', 'Claimed', 'Active'].map((label, index) => (
            <div key={label} className="text-center">
              <div className="mx-auto flex size-9 items-center justify-center rounded-full border border-accent/40 bg-accent-soft font-mono text-sm text-accent">
                {index + 1}
              </div>
              <p className="mt-2.5 text-sm font-medium text-text-primary">{label}</p>
            </div>
          ))}
        </div>
      </section>

      <footer className="border-t border-border-soft px-6 py-8 text-center text-xs text-text-tertiary">
        © {new Date().getFullYear()} AlumniConnect — a final-year major project.
      </footer>
    </div>
  )
}
