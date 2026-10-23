/**
 * GroupsPage — /groups
 *
 * Shows all institute groups, organised by institute, then department.
 * Each group card shows student count, alumni count, and links to the
 * detail page where the group chat and member list live.
 */
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { groupsApi } from '@/api/groupsApi'
import { paths } from '@/utils/constants'
import { Spinner } from '@/components/ui/Spinner'

function StatPill({ label, value }) {
  return (
    <span className="inline-flex items-center gap-1 text-xs text-gray-500 dark:text-gray-400">
      <span className="font-semibold text-gray-700 dark:text-gray-200">{value}</span>
      {label}
    </span>
  )
}

function GroupCard({ group }) {
  return (
    <Link
      to={paths.group(group.departmentId)}
      className="group flex flex-col gap-2 rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4 hover:border-blue-400 hover:shadow-md dark:hover:border-blue-500 transition-all"
    >
      {/* top row */}
      <div className="flex items-start justify-between gap-2">
        <div>
          <h3 className="text-sm font-semibold text-gray-900 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
            {group.name}
          </h3>
          {group.code && (
            <span className="text-xs text-gray-400">{group.code}</span>
          )}
        </div>
        {group.member && (
          <span className="shrink-0 text-xs bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400 px-2 py-0.5 rounded-full font-medium">
            Member
          </span>
        )}
      </div>

      {/* stats */}
      <div className="flex items-center gap-3 flex-wrap">
        <StatPill value={group.studentCount ?? 0} label="students" />
        <span className="text-gray-200 dark:text-gray-600">·</span>
        <StatPill value={group.alumniCount ?? 0} label="alumni" />
        {group.memberCount != null && (
          <>
            <span className="text-gray-200 dark:text-gray-600">·</span>
            <StatPill value={group.memberCount} label="total" />
          </>
        )}
      </div>

      {/* arrow */}
      <div className="flex justify-end">
        <svg
          className="w-4 h-4 text-gray-300 group-hover:text-blue-500 transition-colors"
          fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
        </svg>
      </div>
    </Link>
  )
}

export function GroupsPage() {
  const [institutes, setInstitutes] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')

  useEffect(() => {
    groupsApi
      .list()
      .then((data) => setInstitutes(data ?? []))
      .catch(() => setError('Failed to load groups. Please try again.'))
      .finally(() => setLoading(false))
  }, [])

  // Filter groups by name/code matching the search term
  const filtered = institutes
    .map((inst) => ({
      ...inst,
      groups: (inst.groups ?? []).filter((g) => {
        if (!search.trim()) return true
        const q = search.toLowerCase()
        return (
          g.name?.toLowerCase().includes(q) ||
          g.code?.toLowerCase().includes(q)
        )
      }),
    }))
    .filter((inst) => inst.groups.length > 0)

  return (
    <div className="max-w-4xl mx-auto px-4 py-6">
      {/* header */}
      <div className="flex flex-col sm:flex-row sm:items-center gap-3 mb-6">
        <div className="flex-1">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Groups</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
            Connect with your department community
          </p>
        </div>
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search departments…"
          className="w-full sm:w-64 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 px-4 py-2 text-sm text-gray-900 dark:text-gray-100 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {loading ? (
        <div className="flex justify-center py-16">
          <Spinner />
        </div>
      ) : error ? (
        <div className="text-center py-16">
          <p className="text-red-500 text-sm">{error}</p>
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-16">
          <p className="text-gray-400 text-sm">
            {search ? 'No departments match your search.' : 'No groups available.'}
          </p>
        </div>
      ) : (
        <div className="space-y-8">
          {filtered.map((inst) => (
            <section key={inst.institute?.id ?? inst.institute?.name}>
              <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-3">
                {inst.institute?.name}
              </h2>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                {inst.groups.map((g) => (
                  <GroupCard key={g.departmentId} group={g} />
                ))}
              </div>
            </section>
          ))}
        </div>
      )}
    </div>
  )
}
