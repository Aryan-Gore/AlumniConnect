/**
 * FeedPage — /feed
 *
 * Features:
 *  - Cursor-paginated post feed (load-more button at bottom)
 *  - Create-post composer (PUBLIC / ALUMNI_ONLY / DEPARTMENT / CONNECTIONS)
 *  - Like / unlike toggle with optimistic count
 *  - Inline comment thread (toggle per post)
 *  - Delete own post
 *  - Filters: ?author=<userId> (profile deep-link), ?departmentId=<id>
 */
import { useCallback, useEffect, useRef, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { feedApi } from '@/api/feedApi'
import { useAuth } from '@/hooks/useAuth'
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
  if (d < 7) return `${d}d ago`
  return new Date(iso).toLocaleDateString([], { month: 'short', day: 'numeric' })
}

const VISIBILITY_LABELS = {
  PUBLIC: 'Everyone',
  ALUMNI_ONLY: 'Alumni only',
  DEPARTMENT: 'My department',
  CONNECTIONS: 'Connections',
}

// ─── CommentSection ───────────────────────────────────────────────────────────

function CommentSection({ postId, commentCount }) {
  const { user } = useAuth()
  const [comments, setComments] = useState(null) // null = not loaded yet
  const [loading, setLoading] = useState(false)
  const [input, setInput] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const load = useCallback(async () => {
    if (loading) return
    setLoading(true)
    try {
      const res = await feedApi.comments(postId)
      setComments(res ?? [])
    } catch {
      setComments([])
    } finally {
      setLoading(false)
    }
  }, [postId, loading])

  useEffect(() => { load() }, []) // load on first render

  const handleSubmit = async () => {
    const text = input.trim()
    if (!text || submitting) return
    setSubmitting(true)
    try {
      const c = await feedApi.addComment(postId, text)
      setComments((prev) => [...(prev ?? []), c])
      setInput('')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (commentId) => {
    await feedApi.deleteComment(postId, commentId).catch(() => {})
    setComments((prev) => (prev ?? []).filter((c) => c.id !== commentId))
  }

  return (
    <div className="mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
      {loading && (
        <div className="flex justify-center py-2">
          <Spinner size="sm" />
        </div>
      )}

      {(comments ?? []).map((c) => (
        <div key={c.id} className="flex gap-2 mb-2">
          <Avatar src={c.author?.profilePhotoUrl} name={c.author?.fullName ?? '?'} size="xs" />
          <div className="flex-1 bg-gray-50 dark:bg-gray-800 rounded-xl px-3 py-2">
            <div className="flex items-center justify-between gap-2">
              <span className="text-xs font-semibold text-gray-700 dark:text-gray-200">
                {c.author?.fullName}
              </span>
              <div className="flex items-center gap-2">
                <span className="text-xs text-gray-400">{formatRelative(c.createdAt)}</span>
                {c.author?.id === user?.id && (
                  <button
                    onClick={() => handleDelete(c.id)}
                    className="text-gray-300 hover:text-red-400 text-xs"
                    aria-label="Delete comment"
                  >
                    ×
                  </button>
                )}
              </div>
            </div>
            <p className="text-sm text-gray-700 dark:text-gray-300 mt-0.5">{c.content}</p>
          </div>
        </div>
      ))}

      {/* composer */}
      <div className="flex gap-2 mt-2">
        <Avatar src={user?.profilePhotoUrl} name={user?.fullName ?? '?'} size="xs" />
        <div className="flex-1 flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); handleSubmit() } }}
            placeholder="Write a comment…"
            className="flex-1 rounded-xl border border-gray-200 dark:border-gray-600 bg-gray-50 dark:bg-gray-800 px-3 py-1.5 text-sm text-gray-900 dark:text-gray-100 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            onClick={handleSubmit}
            disabled={!input.trim() || submitting}
            className="px-3 py-1.5 rounded-xl bg-blue-600 text-white text-xs font-medium hover:bg-blue-700 disabled:opacity-40 transition-colors"
          >
            Post
          </button>
        </div>
      </div>
    </div>
  )
}

// ─── PostCard ─────────────────────────────────────────────────────────────────

function PostCard({ post, currentUserId, onDelete }) {
  const [liked, setLiked] = useState(post.likedByViewer ?? false)
  const [likeCount, setLikeCount] = useState(post.likeCount ?? 0)
  const [showComments, setShowComments] = useState(false)
  const [toggling, setToggling] = useState(false)

  const handleLike = async () => {
    if (toggling) return
    setToggling(true)
    const wasLiked = liked
    setLiked(!wasLiked)
    setLikeCount((c) => c + (wasLiked ? -1 : 1))
    try {
      const res = wasLiked ? await feedApi.unlike(post.id) : await feedApi.like(post.id)
      setLiked(res.liked)
      setLikeCount(res.likeCount)
    } catch {
      // revert optimistic
      setLiked(wasLiked)
      setLikeCount((c) => c + (wasLiked ? 1 : -1))
    } finally {
      setToggling(false)
    }
  }

  return (
    <article className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-4 shadow-sm">
      {/* header */}
      <div className="flex items-start gap-3 mb-3">
        <Avatar
          src={post.author?.profilePhotoUrl}
          name={post.author?.fullName ?? '?'}
          size="md"
        />
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="font-semibold text-gray-900 dark:text-white text-sm">
              {post.author?.fullName}
            </span>
            <span className="text-xs text-gray-400">·</span>
            <span className="text-xs text-gray-400">{formatRelative(post.createdAt)}</span>
            {post.visibility !== 'PUBLIC' && (
              <>
                <span className="text-xs text-gray-400">·</span>
                <span className="text-xs text-blue-500">{VISIBILITY_LABELS[post.visibility] ?? post.visibility}</span>
              </>
            )}
          </div>
          {post.author?.jobTitle && (
            <p className="text-xs text-gray-400 truncate">{post.author.jobTitle}</p>
          )}
        </div>
        {post.author?.id === currentUserId && (
          <button
            onClick={() => onDelete(post.id)}
            className="shrink-0 text-gray-300 hover:text-red-400 transition-colors"
            aria-label="Delete post"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        )}
      </div>

      {/* body */}
      <p className="text-sm text-gray-800 dark:text-gray-200 leading-relaxed whitespace-pre-wrap">
        {post.content}
      </p>

      {post.imageUrl && (
        <img
          src={post.imageUrl}
          alt="Post attachment"
          className="mt-3 rounded-xl w-full object-cover max-h-96"
        />
      )}

      {/* actions */}
      <div className="flex items-center gap-4 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
        <button
          onClick={handleLike}
          className={`flex items-center gap-1.5 text-sm transition-colors ${
            liked ? 'text-red-500' : 'text-gray-400 hover:text-red-400'
          }`}
        >
          <svg className="w-4 h-4" fill={liked ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12z" />
          </svg>
          <span>{likeCount > 0 ? likeCount : ''}</span>
        </button>

        <button
          onClick={() => setShowComments((v) => !v)}
          className="flex items-center gap-1.5 text-sm text-gray-400 hover:text-blue-500 transition-colors"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 12.76c0 1.6 1.123 2.994 2.707 3.227 1.087.16 2.185.283 3.293.369V21l4.076-4.076a1.526 1.526 0 011.037-.443 48.282 48.282 0 005.68-.494c1.584-.233 2.707-1.626 2.707-3.228V6.741c0-1.602-1.123-2.995-2.707-3.228A48.394 48.394 0 0012 3c-2.392 0-4.744.175-7.043.513C3.373 3.746 2.25 5.14 2.25 6.741v6.018z" />
          </svg>
          <span>{post.commentCount > 0 ? post.commentCount : 'Comment'}</span>
        </button>
      </div>

      {showComments && <CommentSection postId={post.id} commentCount={post.commentCount} />}
    </article>
  )
}

// ─── CreatePostCard ───────────────────────────────────────────────────────────

function CreatePostCard({ user, onCreated }) {
  const [open, setOpen] = useState(false)
  const [content, setContent] = useState('')
  const [visibility, setVisibility] = useState('PUBLIC')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const textareaRef = useRef(null)

  const handleOpen = () => {
    setOpen(true)
    setTimeout(() => textareaRef.current?.focus(), 50)
  }

  const handleSubmit = async () => {
    const text = content.trim()
    if (!text || submitting) return
    setSubmitting(true)
    setError(null)
    try {
      const post = await feedApi.createPost({ content: text, visibility })
      onCreated(post)
      setContent('')
      setVisibility('PUBLIC')
      setOpen(false)
    } catch {
      setError('Failed to post. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-4 shadow-sm">
      {!open ? (
        <div className="flex items-center gap-3">
          <Avatar src={user?.profilePhotoUrl} name={user?.fullName ?? '?'} size="md" />
          <button
            onClick={handleOpen}
            className="flex-1 text-left rounded-xl border border-gray-200 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 px-4 py-2.5 text-sm text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors"
          >
            What's on your mind?
          </button>
        </div>
      ) : (
        <div>
          <div className="flex items-start gap-3 mb-3">
            <Avatar src={user?.profilePhotoUrl} name={user?.fullName ?? '?'} size="md" />
            <div className="flex-1">
              <p className="text-sm font-semibold text-gray-900 dark:text-white">{user?.fullName}</p>
              <select
                value={visibility}
                onChange={(e) => setVisibility(e.target.value)}
                className="mt-1 text-xs text-blue-600 bg-blue-50 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-700 rounded-lg px-2 py-0.5 focus:outline-none"
              >
                {Object.entries(VISIBILITY_LABELS).map(([v, label]) => (
                  <option key={v} value={v}>{label}</option>
                ))}
              </select>
            </div>
          </div>

          <textarea
            ref={textareaRef}
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="Share something with the community…"
            rows={4}
            className="w-full rounded-xl border border-gray-200 dark:border-gray-600 bg-gray-50 dark:bg-gray-800 px-4 py-3 text-sm text-gray-900 dark:text-gray-100 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          />

          {error && <p className="text-xs text-red-500 mt-1">{error}</p>}

          <div className="flex items-center justify-end gap-2 mt-3">
            <button
              onClick={() => { setOpen(false); setContent(''); setError(null) }}
              className="px-4 py-2 rounded-xl text-sm text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={handleSubmit}
              disabled={!content.trim() || submitting}
              className="px-4 py-2 rounded-xl bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 disabled:opacity-40 transition-colors flex items-center gap-2"
            >
              {submitting && <Spinner size="xs" />}
              Post
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

// ─── FeedPage ─────────────────────────────────────────────────────────────────

export function FeedPage() {
  const { user } = useAuth()
  const [searchParams] = useSearchParams()

  const authorId = searchParams.get('author') || undefined
  const departmentId = searchParams.get('departmentId') || undefined

  const [posts, setPosts] = useState([])
  const [nextBefore, setNextBefore] = useState(null)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState(null)

  const loadFeed = useCallback(async (reset = false) => {
    if (reset) {
      setLoading(true)
      setNextBefore(null)
    } else {
      setLoadingMore(true)
    }
    setError(null)
    try {
      const res = await feedApi.feed({
        limit: 20,
        ...(reset ? {} : { before: nextBefore }),
        ...(authorId ? { authorId } : {}),
        ...(departmentId ? { departmentId } : {}),
      })
      const items = res.items ?? []
      setPosts((prev) => (reset ? items : [...prev, ...items]))
      setNextBefore(res.nextBefore ?? null)
    } catch {
      setError('Failed to load feed. Please try again.')
    } finally {
      setLoading(false)
      setLoadingMore(false)
    }
  }, [nextBefore, authorId, departmentId]) // eslint-disable-line react-hooks/exhaustive-deps

  // initial load and on filter change
  useEffect(() => {
    loadFeed(true)
  }, [authorId, departmentId]) // eslint-disable-line react-hooks/exhaustive-deps

  const handleCreated = (post) => {
    setPosts((prev) => [post, ...prev])
  }

  const handleDelete = async (postId) => {
    await feedApi.deletePost(postId).catch(() => {})
    setPosts((prev) => prev.filter((p) => p.id !== postId))
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-6">
      {/* title */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          {authorId ? 'Posts' : departmentId ? 'Department Feed' : 'Feed'}
        </h1>
      </div>

      {/* composer — not shown on author-filtered view */}
      {!authorId && (
        <div className="mb-4">
          <CreatePostCard user={user} onCreated={handleCreated} />
        </div>
      )}

      {/* posts */}
      {loading ? (
        <div className="flex justify-center py-16">
          <Spinner />
        </div>
      ) : error ? (
        <div className="text-center py-16">
          <p className="text-red-500 text-sm mb-4">{error}</p>
          <button
            onClick={() => loadFeed(true)}
            className="px-4 py-2 rounded-xl bg-blue-600 text-white text-sm hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      ) : posts.length === 0 ? (
        <div className="text-center py-16">
          <p className="text-gray-400 text-sm">No posts yet. Be the first to share something!</p>
        </div>
      ) : (
        <div className="space-y-4">
          {posts.map((post) => (
            <PostCard
              key={post.id}
              post={post}
              currentUserId={user?.id}
              onDelete={handleDelete}
            />
          ))}

          {nextBefore && (
            <div className="flex justify-center pt-2">
              <button
                onClick={() => loadFeed(false)}
                disabled={loadingMore}
                className="px-6 py-2 rounded-xl border border-gray-200 dark:border-gray-700 text-sm text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800 disabled:opacity-50 flex items-center gap-2 transition-colors"
              >
                {loadingMore && <Spinner size="xs" />}
                Load older posts
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
