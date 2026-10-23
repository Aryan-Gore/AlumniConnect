/**
 * MessagesPage — /messages  and  /messages/:conversationId
 *
 * Layout:
 *   ┌──────────────────────┬────────────────────────────────────┐
 *   │  Conversation list   │  Thread view (or empty-state)      │
 *   │  (left sidebar)      │                                    │
 *   └──────────────────────┴────────────────────────────────────┘
 *
 * Features:
 *  - Shows department group rooms AND direct DMs
 *  - "New chat" button with user search to start a DM
 *  - Cursor-paginated message history (loads older messages on scroll-to-top)
 *  - Sends via STOMP; falls back to REST if socket is not connected
 *  - Marks conversation read when thread is focused
 *  - Auto-scrolls to newest message; preserves position when loading older pages
 *  - Handles ?with=<userId> query param (starts/opens DM with that user)
 */
import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { chatApi } from '@/api/chatApi'
import { useChat } from './ChatProvider'
import { useAuth } from '@/hooks/useAuth'
import { paths, CONVERSATION_TYPE } from '@/utils/constants'
import { Avatar } from '@/components/ui/Avatar'
import { Spinner } from '@/components/ui/Spinner'

// ─── tiny helpers ────────────────────────────────────────────────────────────

function formatTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const now = new Date()
  const sameDay =
    d.getDate() === now.getDate() &&
    d.getMonth() === now.getMonth() &&
    d.getFullYear() === now.getFullYear()
  if (sameDay) return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (
    d.getDate() === yesterday.getDate() &&
    d.getMonth() === yesterday.getMonth() &&
    d.getFullYear() === yesterday.getFullYear()
  )
    return 'Yesterday'
  return d.toLocaleDateString([], { month: 'short', day: 'numeric' })
}

function convName(conv, currentUserId) {
  if (conv.type === CONVERSATION_TYPE.DEPARTMENT) return conv.departmentName ?? 'Group'
  if (conv.peer) {
    return conv.peer.id === currentUserId ? 'You' : conv.peer.fullName
  }
  return 'Direct message'
}

function convAvatar(conv, currentUserId) {
  if (conv.type === CONVERSATION_TYPE.DEPARTMENT) return { name: conv.departmentName ?? 'G', src: null }
  if (conv.peer) return { name: conv.peer.fullName, src: conv.peer.profilePhotoUrl }
  return { name: '?', src: null }
}

// ─── ConversationItem ─────────────────────────────────────────────────────────

function ConversationItem({ conv, active, currentUserId, onClick }) {
  const name = convName(conv, currentUserId)
  const av = convAvatar(conv, currentUserId)
  const lastText = conv.lastMessage?.content ?? ''
  const unread = conv.unreadCount ?? 0

  return (
    <button
      onClick={onClick}
      className={`w-full flex items-center gap-3 px-4 py-3 text-left transition-colors hover:bg-gray-50 dark:hover:bg-gray-800 ${
        active ? 'bg-blue-50 dark:bg-blue-900/30' : ''
      }`}
    >
      <Avatar src={av.src} name={av.name} size="md" />
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between gap-2">
          <span className={`text-sm truncate ${unread > 0 ? 'font-semibold text-gray-900 dark:text-white' : 'text-gray-700 dark:text-gray-300'}`}>
            {name}
          </span>
          <span className="text-xs text-gray-400 shrink-0">{formatTime(conv.updatedAt)}</span>
        </div>
        <div className="flex items-center justify-between gap-2 mt-0.5">
          <span className={`text-xs truncate ${unread > 0 ? 'text-gray-700 dark:text-gray-200' : 'text-gray-400'}`}>
            {lastText || <span className="italic">No messages yet</span>}
          </span>
          {unread > 0 && (
            <span className="shrink-0 min-w-[1.25rem] h-5 flex items-center justify-center rounded-full bg-blue-600 text-white text-xs font-medium px-1">
              {unread > 99 ? '99+' : unread}
            </span>
          )}
        </div>
      </div>
    </button>
  )
}

// ─── MessageBubble ────────────────────────────────────────────────────────────

function MessageBubble({ msg, isOwn }) {
  return (
    <div className={`flex items-end gap-2 ${isOwn ? 'flex-row-reverse' : 'flex-row'}`}>
      {!isOwn && (
        <Avatar src={msg.author?.profilePhotoUrl} name={msg.author?.fullName ?? '?'} size="sm" />
      )}
      <div className={`max-w-[70%] ${isOwn ? 'items-end' : 'items-start'} flex flex-col gap-1`}>
        {!isOwn && (
          <span className="text-xs text-gray-500 ml-1">{msg.author?.fullName}</span>
        )}
        <div
          className={`rounded-2xl px-4 py-2.5 text-sm leading-relaxed break-words ${
            isOwn
              ? 'bg-blue-600 text-white rounded-br-sm'
              : 'bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 border border-gray-200 dark:border-gray-600 rounded-bl-sm'
          }`}
        >
          {msg.content}
        </div>
        <span className="text-xs text-gray-400 px-1">{formatTime(msg.createdAt)}</span>
      </div>
    </div>
  )
}

// ─── ThreadView ───────────────────────────────────────────────────────────────

function ThreadView({ conversationId }) {
  const { user } = useAuth()
  const { sendMessage, markRead, conversations } = useChat()

  const [messages, setMessages] = useState([])
  const [nextBefore, setNextBefore] = useState(null)
  const [loadingInitial, setLoadingInitial] = useState(true)
  const [loadingOlder, setLoadingOlder] = useState(false)
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const [error, setError] = useState(null)

  const bottomRef = useRef(null)
  const topRef = useRef(null)
  const containerRef = useRef(null)
  const prevScrollHeight = useRef(0)

  const conv = conversations.find((c) => c.id === Number(conversationId))

  // ── load initial messages ──────────────────────────────────────────────────
  useEffect(() => {
    if (!conversationId) return
    setMessages([])
    setNextBefore(null)
    setLoadingInitial(true)
    setError(null)

    chatApi
      .messages(conversationId, { limit: 40 })
      .then((res) => {
        setMessages(res.items ?? [])
        setNextBefore(res.nextBefore ?? null)
      })
      .catch(() => setError('Failed to load messages.'))
      .finally(() => setLoadingInitial(false))
  }, [conversationId])

  // ── mark read when thread opens ────────────────────────────────────────────
  useEffect(() => {
    if (!conversationId || !messages.length) return
    const latest = messages[messages.length - 1]
    if (latest?.id) markRead(Number(conversationId), latest.id)
  }, [conversationId, messages, markRead])

  // ── auto-scroll to bottom on new messages ─────────────────────────────────
  useEffect(() => {
    if (!loadingInitial) {
      bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
    }
  }, [messages.length, loadingInitial])

  // ── preserve scroll position when loading older messages ──────────────────
  useEffect(() => {
    if (!loadingOlder) {
      const container = containerRef.current
      if (container) {
        const diff = container.scrollHeight - prevScrollHeight.current
        container.scrollTop = diff
      }
    }
  }, [loadingOlder])

  // ── real-time: new messages pushed via STOMP ───────────────────────────────
  // ChatProvider fires READ and MESSAGE events; for MESSAGE we append to the list
  // if it belongs to this conversation. We rely on the conversations state that
  // ChatProvider manages for tracking; we re-sync from it here.
  const prevConvRef = useRef(null)
  useEffect(() => {
    if (!conv) return
    const prevConv = prevConvRef.current
    if (
      prevConv &&
      conv.lastMessage &&
      conv.lastMessage !== prevConv.lastMessage &&
      conv.lastMessage.conversationId === conv.id
    ) {
      setMessages((prev) => {
        // avoid duplicates (REST send + STOMP push)
        if (prev.some((m) => m.id === conv.lastMessage.id)) return prev
        return [...prev, conv.lastMessage]
      })
    }
    prevConvRef.current = conv
  }, [conv])

  // ── load older messages ────────────────────────────────────────────────────
  const loadOlder = useCallback(async () => {
    if (!nextBefore || loadingOlder) return
    prevScrollHeight.current = containerRef.current?.scrollHeight ?? 0
    setLoadingOlder(true)
    try {
      const res = await chatApi.messages(conversationId, { before: nextBefore, limit: 40 })
      setMessages((prev) => [...(res.items ?? []), ...prev])
      setNextBefore(res.nextBefore ?? null)
    } catch {
      // silently ignore — they can scroll down and try again
    } finally {
      setLoadingOlder(false)
    }
  }, [conversationId, nextBefore, loadingOlder])

  // IntersectionObserver on the invisible top sentinel
  useEffect(() => {
    const el = topRef.current
    if (!el) return
    const observer = new IntersectionObserver(
      ([entry]) => { if (entry.isIntersecting) loadOlder() },
      { threshold: 0 }
    )
    observer.observe(el)
    return () => observer.disconnect()
  }, [loadOlder])

  // ── send ───────────────────────────────────────────────────────────────────
  const handleSend = useCallback(async () => {
    const content = input.trim()
    if (!content || sending) return
    setSending(true)
    setInput('')

    // Optimistic bubble
    const optimistic = {
      id: `tmp-${Date.now()}`,
      content,
      createdAt: new Date().toISOString(),
      author: { id: user?.id, fullName: user?.fullName, profilePhotoUrl: user?.profilePhotoUrl },
      conversationId: Number(conversationId),
      _pending: true,
    }
    setMessages((prev) => [...prev, optimistic])

    const sent = sendMessage(Number(conversationId), content)
    if (!sent) {
      // STOMP not connected — fall back to REST
      try {
        const msg = await chatApi.send(conversationId, content)
        setMessages((prev) => prev.map((m) => (m.id === optimistic.id ? msg : m)))
      } catch {
        setMessages((prev) => prev.filter((m) => m.id !== optimistic.id))
        setInput(content) // restore so user can retry
      }
    } else {
      // STOMP accepted: the real message will arrive via push; remove the optimistic copy
      // after a short timeout if the push doesn't confirm it
      setTimeout(() => {
        setMessages((prev) => prev.filter((m) => m.id !== optimistic.id || !m._pending))
      }, 5000)
    }
    setSending(false)
  }, [input, sending, conversationId, sendMessage, user])

  const handleKey = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  // ── render ─────────────────────────────────────────────────────────────────
  const title = conv ? convName(conv, user?.id) : 'Messages'
  const av = conv ? convAvatar(conv, user?.id) : null

  return (
    <div className="flex flex-col h-full">
      {/* header */}
      <div className="flex items-center gap-3 px-4 py-3 border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 shrink-0">
        {av && <Avatar src={av.src} name={av.name} size="md" />}
        <span className="font-semibold text-gray-900 dark:text-white">{title}</span>
      </div>

      {/* messages */}
      <div
        ref={containerRef}
        className="flex-1 overflow-y-auto px-4 py-4 flex flex-col gap-3 bg-gray-50 dark:bg-gray-950"
      >
        {/* sentinel for loading older */}
        <div ref={topRef} className="h-1" />
        {loadingOlder && (
          <div className="flex justify-center py-2">
            <Spinner size="sm" />
          </div>
        )}

        {loadingInitial ? (
          <div className="flex-1 flex items-center justify-center">
            <Spinner />
          </div>
        ) : error ? (
          <div className="flex-1 flex items-center justify-center text-red-500 text-sm">{error}</div>
        ) : messages.length === 0 ? (
          <div className="flex-1 flex items-center justify-center">
            <p className="text-gray-400 text-sm">No messages yet. Say hello!</p>
          </div>
        ) : (
          messages.map((msg) => (
            <MessageBubble
              key={msg.id}
              msg={msg}
              isOwn={msg.author?.id === user?.id}
            />
          ))
        )}
        <div ref={bottomRef} />
      </div>

      {/* composer */}
      <div className="shrink-0 px-4 py-3 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900">
        <div className="flex items-end gap-2">
          <textarea
            rows={1}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKey}
            placeholder="Type a message…"
            className="flex-1 resize-none rounded-2xl border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-800 px-4 py-2.5 text-sm text-gray-900 dark:text-gray-100 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 max-h-32 overflow-y-auto"
            style={{ fieldSizing: 'content' }}
          />
          <button
            onClick={handleSend}
            disabled={!input.trim() || sending}
            className="shrink-0 rounded-full w-10 h-10 flex items-center justify-center bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            aria-label="Send"
          >
            <svg className="w-5 h-5 rotate-90" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 19V5M5 12l7-7 7 7" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  )
}

// ─── NewChatModal ─────────────────────────────────────────────────────────────

function NewChatModal({ onClose, onConversationReady }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const timerRef = useRef(null)

  const handleSearch = (q) => {
    setQuery(q)
    clearTimeout(timerRef.current)
    if (q.length < 2) { setResults([]); return }
    timerRef.current = setTimeout(async () => {
      setLoading(true)
      try {
        const res = await chatApi.searchUsers(q, { size: 10 })
        setResults(res.content ?? res.items ?? [])
      } catch {
        setResults([])
      } finally {
        setLoading(false)
      }
    }, 300)
  }

  const startChat = async (targetUser) => {
    try {
      const conv = await chatApi.startDirect(targetUser.id)
      onConversationReady(conv.id)
      onClose()
    } catch {
      // ignore
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
    >
      <div
        className="w-full max-w-md bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">New conversation</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <input
          autoFocus
          type="text"
          value={query}
          onChange={(e) => handleSearch(e.target.value)}
          placeholder="Search by name or email…"
          className="w-full rounded-xl border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 px-4 py-2.5 text-sm text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3"
        />

        {loading && (
          <div className="flex justify-center py-4">
            <Spinner size="sm" />
          </div>
        )}

        <div className="space-y-1 max-h-60 overflow-y-auto">
          {results.map((u) => (
            <button
              key={u.id}
              onClick={() => startChat(u)}
              className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700 text-left transition-colors"
            >
              <Avatar src={u.profilePhotoUrl} name={u.fullName} size="sm" />
              <div>
                <p className="text-sm font-medium text-gray-900 dark:text-white">{u.fullName}</p>
                <p className="text-xs text-gray-400">{u.department?.name ?? u.role}</p>
              </div>
            </button>
          ))}
          {!loading && query.length >= 2 && results.length === 0 && (
            <p className="text-center text-sm text-gray-400 py-4">No users found</p>
          )}
          {query.length < 2 && (
            <p className="text-center text-sm text-gray-400 py-4">Type at least 2 characters to search</p>
          )}
        </div>
      </div>
    </div>
  )
}

// ─── MessagesPage ─────────────────────────────────────────────────────────────

export function MessagesPage() {
  const { user } = useAuth()
  const { conversations, reloadConversations } = useChat()
  const navigate = useNavigate()
  const { conversationId } = useParams()
  const [searchParams] = useSearchParams()
  const [showNewChat, setShowNewChat] = useState(false)
  const [search, setSearch] = useState('')

  const withUserId = searchParams.get('with')

  // Handle ?with=<userId> — start / open a DM immediately
  useEffect(() => {
    if (!withUserId) return
    chatApi
      .startDirect(withUserId)
      .then((conv) => {
        reloadConversations()
        navigate(paths.thread(conv.id), { replace: true })
      })
      .catch(() => navigate(paths.thread(''), { replace: true }))
  }, [withUserId, navigate, reloadConversations])

  const filtered = useMemo(() => {
    if (!search.trim()) return conversations
    const q = search.toLowerCase()
    return conversations.filter((c) => {
      const name = convName(c, user?.id).toLowerCase()
      return name.includes(q)
    })
  }, [conversations, search, user?.id])

  const handleConvClick = (conv) => {
    navigate(paths.thread(conv.id))
  }

  const handleNewConv = (newConvId) => {
    reloadConversations()
    navigate(paths.thread(newConvId))
  }

  return (
    <div className="flex h-full min-h-0 bg-white dark:bg-gray-900">
      {/* ── left sidebar ───────────────────────────────────────────────────── */}
      <aside className="w-full max-w-xs shrink-0 flex flex-col border-r border-gray-200 dark:border-gray-700">
        {/* header */}
        <div className="flex items-center justify-between px-4 py-4 border-b border-gray-200 dark:border-gray-700">
          <h1 className="text-xl font-bold text-gray-900 dark:text-white">Messages</h1>
          <button
            onClick={() => setShowNewChat(true)}
            className="rounded-full w-8 h-8 flex items-center justify-center bg-blue-600 text-white hover:bg-blue-700 transition-colors"
            aria-label="New conversation"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
            </svg>
          </button>
        </div>

        {/* search */}
        <div className="px-3 py-2">
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search conversations…"
            className="w-full rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-gray-100 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* list */}
        <div className="flex-1 overflow-y-auto">
          {filtered.length === 0 ? (
            <p className="text-center text-sm text-gray-400 py-8 px-4">
              {search ? 'No conversations match.' : 'No conversations yet.'}
            </p>
          ) : (
            filtered.map((conv) => (
              <ConversationItem
                key={conv.id}
                conv={conv}
                active={conv.id === Number(conversationId)}
                currentUserId={user?.id}
                onClick={() => handleConvClick(conv)}
              />
            ))
          )}
        </div>
      </aside>

      {/* ── right: thread view or empty state ─────────────────────────────── */}
      <main className="flex-1 min-w-0">
        {conversationId ? (
          <ThreadView key={conversationId} conversationId={conversationId} />
        ) : (
          <div className="h-full flex items-center justify-center">
            <div className="text-center">
              <div className="w-16 h-16 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" strokeWidth={1.5} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8.625 12a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H8.25m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H12m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 01-2.555-.337A5.972 5.972 0 015.41 20.97a5.969 5.969 0 01-.474-.065 4.48 4.48 0 00.978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25z" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-gray-700 dark:text-gray-200 mb-1">Your Messages</h3>
              <p className="text-gray-400 text-sm mb-4">Select a conversation or start a new one.</p>
              <button
                onClick={() => setShowNewChat(true)}
                className="px-4 py-2 rounded-xl bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 transition-colors"
              >
                New conversation
              </button>
            </div>
          </div>
        )}
      </main>

      {showNewChat && (
        <NewChatModal
          onClose={() => setShowNewChat(false)}
          onConversationReady={handleNewConv}
        />
      )}
    </div>
  )
}
