/**
 * GroupDetailPage — /groups/:departmentId
 *
 * Three tabs:
 *   About   — group stats + description
 *   Members — paginated member list with search
 *   Feed    — department-scoped post feed (reuses FeedPage internals)
 *
 * If the viewer is a member/admin the "Open group chat" button navigates to
 * the department conversation in MessagesPage.
 */
import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { groupsApi } from '@/api/groupsApi'
import { feedApi } from '@/api/feedApi'
import { useAuth } from '@/hooks/useAuth'
import { paths, ROLES } from '@/utils/constants'
import { Avatar } from '@/components/ui/Avatar'
import { Spinner } from '@/components/ui/Spinner'

// ─── helpers ──────────────────────────────────────────────────────────────────

function formatRelative(iso) {
  if (!iso) return ''
  const diff = Date.now() - new Date(iso).getTime()
  const s = Math.floor(diff / 1000)
  if (s < 60) return 'Just now'
  const m = Math.floor(s / 60)
  if (m < 60) return `${m}m ago`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}h ago`
  const d = Math.floor(h / 24)
  return d < 7 ? `${d}d ago` : new Date(iso).toLocaleDateString([], { month: 'short', day: 'numeric' })
}

// ─── MembersTab ───────────────────────────────────────────────────────────────

function MembersTab({ departmentId }) {
  const [members, setMembers] = useState([])
  const [totalPages, setTotalPages] = useState(1)
  const [page, setPage] = useState(0)
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(false)

  const load = useCallback(
    async (p = 0, q = '') => {
      setLoading(true)
      try {
        const res = await groupsApi.members(departmentId, { query: q || undefined, page: p, size: 20 })
        setMembers(res.content ?? res.items ?? [])
        setTotalPages(res.totalPages ?? 1)
        setPage(p)
      } finally {
        setLoading(false)
      }
    },
    [departmentId]
  )

  useEffect(() => { load(0, '') }, [load])

  const handleSearch = (q) => {
    setQuery(q)
    load(0, q)
  }

  return (
    <div>
      <div className="mb-4">
        <input
          type="text"
          value={query}
          onChange={(e) => handleSearch(e.target.value)}
          placeholder="Search members…"
          className="w-full sm:w-72 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 px-4 py-2 text-sm text-gray-900 dark:text-gray-100 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {loading ? (
        <div className="flex justify-center py-8">
          <Spinner />
        </div>
      ) : members.length === 0 ? (
        <p className="text-center text-gray-400 text-sm py-8">No members found.</p>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {members.map((m) => (
              <div
                key={m.id}
                className="flex items-center gap-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-3"
              >
                <Avatar src={m.profilePhotoUrl} name={m.fullName ?? '?'} size="md" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{m.fullName}</p>
                  <p className="text-xs text-gray-400 truncate">
                    {m.jobTitle ?? m.role ?? ''}
                  </p>
                </div>
                <Link
                  to={paths.messageUser(m.id)}
                  className="shrink-0 text-xs text-blue-600 hover:underline"
                >
                  Message
                </Link>
              </div>
            ))}
          </div>

          {/* pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-4">
              <button
                onClick={() => load(page - 1, query)}
                disabled={page === 0}
                className="px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-700 text-sm disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
                ← Prev
              </button>
              <span className="text-sm text-gray-500">
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => load(page + 1, query)}
                disabled={page >= totalPages - 1}
                className="px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-700 text-sm disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
                Next →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

// ─── FeedTab — department-scoped feed ─────────────────────────────────────────

function FeedTab({ departmentId }) {
  const { user } = useAuth()
  const [posts, setPosts] = useState([])
  const [nextBefore, setNextBefore] = useState(null)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [content, setContent] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const loadPosts = useCallback(
    async (reset = false) => {
      if (reset) setLoading(true)
      else setLoadingMore(true)
      try {
        const res = await feedApi.feed({
          departmentId,
          limit: 15,
          ...(reset ? {} : { before: nextBefore }),
        })
        const items = res.items ?? []
        setPosts((prev) => (reset ? items : [...prev, ...items]))
        setNextBefore(res.nextBefore ?? null)
      } finally {
        setLoading(false)
        setLoadingMore(false)
      }
    },
    [departmentId, nextBefore] // eslint-disable-line react-hooks/exhaustive-deps
  )

  useEffect(() => { loadPosts(true) }, [departmentId]) // eslint-disable-line react-hooks/exhaustive-deps

  const handlePost = async () => {
    const text = content.trim()
    if (!text || submitting) return
    setSubmitting(true)
    try {
      const post = await feedApi.createPost({ content: text, visibility: 'DEPARTMENT' })
      setPosts((prev) => [post, ...prev])
      setContent('')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-4">
      {/* mini composer */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-4">
        <div className="flex gap-3">
          <Avatar src={user?.profilePhotoUrl} name={user?.fullName ?? '?'} size="sm" />
          <div className="flex-1">
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Post to this department group…"
              rows={2}
              className="w-full rounded-xl border border-gray-200 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 px-3 py-2 text-sm text-gray-900 dark:text-gray-100 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
            />
            <div className="flex justify-end mt-2">
              <button
                onClick={handlePost}
                disabled={!content.trim() || submitting}
                className="px-3 py-1.5 rounded-xl bg-blue-600 text-white text-xs font-medium hover:bg-blue-700 disabled:opacity-40 transition-colors"
              >
                Post
              </button>
            </div>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-8"><Spinner /></div>
      ) : posts.length === 0 ? (
        <p className="text-center text-gray-400 text-sm py-8">No posts yet in this group.</p>
      ) : (
        <>
          {posts.map((post) => (
            <article
              key={post.id}
              className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-4"
            >
              <div className="flex items-center gap-3 mb-2">
                <Avatar src={post.author?.profilePhotoUrl} name={post.author?.fullName ?? '?'} size="sm" />
                <div>
                  <p className="text-sm font-semibold text-gray-900 dark:text-white">{post.author?.fullName}</p>
                  <p className="text-xs text-gray-400">{formatRelative(post.createdAt)}</p>
                </div>
              </div>
              <p className="text-sm text-gray-800 dark:text-gray-200 whitespace-pre-wrap">{post.content}</p>
              <div className="flex gap-3 mt-3 text-xs text-gray-400">
                <span>{post.likeCount ?? 0} likes</span>
                <span>{post.commentCount ?? 0} comments</span>
              </div>
            </article>
          ))}
          {nextBefore && (
            <div className="flex justify-center">
              <button
                onClick={() => loadPosts(false)}
                disabled={loadingMore}
                className="px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 text-sm text-gray-500 hover:bg-gray-50 dark:hover:bg-gray-800 disabled:opacity-50 flex items-center gap-2"
              >
                {loadingMore && <Spinner size="xs" />}
                Load more
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

// ─── GroupDetailPage ──────────────────────────────────────────────────────────

const TABS = ['About', 'Members', 'Feed']

export function GroupDetailPage() {
  const { departmentId } = useParams()
  const { user } = useAuth()
  const [group, setGroup] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [tab, setTab] = useState('About')

  useEffect(() => {
    setLoading(true)
    setError(null)
    groupsApi
      .get(departmentId)
      .then(setGroup)
      .catch(() => setError('Group not found or you do not have access.'))
      .finally(() => setLoading(false))
  }, [departmentId])

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[40vh]">
        <Spinner />
      </div>
    )
  }

  if (error || !group) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-16 text-center">
        <p className="text-red-500 text-sm">{error ?? 'Something went wrong.'}</p>
        <Link to={paths.group('')} className="text-blue-600 text-sm mt-2 inline-block hover:underline">
          ← Back to groups
        </Link>
      </div>
    )
  }

  const isAdmin = user?.role === ROLES.ADMIN
  const canChat = !!group.conversationId || isAdmin

  return (
    <div className="max-w-4xl mx-auto px-4 py-6">
      {/* breadcrumb */}
      <nav className="mb-4">
        <Link to="/groups" className="text-sm text-blue-600 hover:underline">
          ← Groups
        </Link>
      </nav>

      {/* hero header */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-6 mb-6">
        <div className="flex flex-col sm:flex-row sm:items-start gap-4">
          {/* icon */}
          <div className="w-14 h-14 rounded-2xl bg-blue-100 dark:bg-blue-900/40 flex items-center justify-center shrink-0">
            <svg className="w-7 h-7 text-blue-600" fill="none" stroke="currentColor" strokeWidth={1.5} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
            </svg>
          </div>

          <div className="flex-1">
            <div className="flex items-start justify-between gap-3 flex-wrap">
              <div>
                <h1 className="text-xl font-bold text-gray-900 dark:text-white">{group.name}</h1>
                {group.instituteName && (
                  <p className="text-sm text-gray-400 mt-0.5">{group.instituteName}</p>
                )}
              </div>
              {canChat && (
                <Link
                  to={paths.thread(group.conversationId)}
                  className="px-4 py-2 rounded-xl bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 transition-colors flex items-center gap-2"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M8.625 12a.375.375 0 11-.75 0 .375.375 0 01.75 0zm3.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm3.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zM21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 01-2.555-.337A5.972 5.972 0 015.41 20.97a5.969 5.969 0 01-.474-.065 4.48 4.48 0 00.978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25z" />
                  </svg>
                  Group Chat
                </Link>
              )}
            </div>

            {/* stats */}
            <div className="flex flex-wrap gap-4 mt-3">
              {[
                { label: 'Students', value: group.studentCount ?? 0 },
                { label: 'Alumni', value: group.alumniCount ?? 0 },
                { label: 'Total members', value: group.memberCount ?? (group.studentCount ?? 0) + (group.alumniCount ?? 0) },
              ].map((s) => (
                <div key={s.label}>
                  <p className="text-lg font-bold text-gray-900 dark:text-white">{s.value}</p>
                  <p className="text-xs text-gray-400">{s.label}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* tabs */}
      <div className="flex gap-1 border-b border-gray-200 dark:border-gray-700 mb-6">
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-2 text-sm font-medium transition-colors border-b-2 -mb-px ${
              tab === t
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      {/* tab content */}
      {tab === 'About' && (
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-6">
          <dl className="space-y-4">
            {[
              { label: 'Department', value: group.name },
              { label: 'Code', value: group.code ?? '—' },
              { label: 'Institute', value: group.instituteName ?? '—' },
              { label: 'Students', value: group.studentCount ?? 0 },
              { label: 'Alumni', value: group.alumniCount ?? 0 },
            ].map((row) => (
              <div key={row.label} className="flex gap-4">
                <dt className="w-32 shrink-0 text-sm text-gray-400">{row.label}</dt>
                <dd className="text-sm font-medium text-gray-900 dark:text-white">{row.value}</dd>
              </div>
            ))}
          </dl>
        </div>
      )}

      {tab === 'Members' && <MembersTab departmentId={departmentId} />}
      {tab === 'Feed' && <FeedTab departmentId={departmentId} />}
    </div>
  )
}
