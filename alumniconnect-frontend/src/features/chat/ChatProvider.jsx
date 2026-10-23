/**
 * ChatProvider — owns the STOMP connection, conversation list, and unread counts.
 * Mounted once inside <AppShell> so the socket persists across page navigations.
 *
 * STOMP contract (mirrors backend §8):
 *   WS endpoint : /api/ws  (plain WebSocket, no SockJS)
 *   Connect hdr : Authorization: Bearer <token>
 *   Subscribe   : /user/queue/messages  (MESSAGE / READ events)
 *                 /user/queue/errors    (error frames)
 *   Send        : /app/chat.send  { conversationId, content, clientId? }
 *                 /app/chat.read  { conversationId, messageId }
 */
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react'
import { Client } from '@stomp/stompjs'
import { chatApi } from '@/api/chatApi'
import { getStoredToken, API_BASE_URL } from '@/api/axiosClient'
import { useAuth } from '@/hooks/useAuth'

const ChatContext = createContext({ unreadTotal: 0 })

// Derive native WebSocket URL from the REST base URL
// http://host/api  -> ws://host/api/ws
// https://host/api -> wss://host/api/ws
function wsUrl() {
  return API_BASE_URL.replace(/^http/, 'ws') + '/ws'
}

export function ChatProvider({ children }) {
  const { user } = useAuth()

  // conversations: array matching the backend shape
  // { id, type, peer?, departmentId?, departmentName?, lastMessage?, unreadCount, updatedAt }
  const [conversations, setConversations] = useState([])
  const [unreadTotal, setUnreadTotal] = useState(0)
  const [connected, setConnected] = useState(false)

  // Ref to the active STOMP client so callbacks can reference without stale closures
  const clientRef = useRef(null)

  // -----------------------------------------------------------------------
  // Load initial conversation list
  // -----------------------------------------------------------------------
  const loadConversations = useCallback(async () => {
    try {
      const list = await chatApi.conversations()
      setConversations(list)
      setUnreadTotal(list.reduce((sum, c) => sum + (c.unreadCount ?? 0), 0))
    } catch {
      // silent — STOMP will push updates anyway
    }
  }, [])

  useEffect(() => {
    if (user) loadConversations()
  }, [user, loadConversations])

  // -----------------------------------------------------------------------
  // STOMP connection
  // -----------------------------------------------------------------------
  useEffect(() => {
    if (!user) return

    const token = getStoredToken()
    if (!token) return

    const client = new Client({
      brokerURL: wsUrl(),
      connectHeaders: { Authorization: `Bearer ${token}` },
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)

        client.subscribe('/user/queue/messages', (frame) => {
          try {
            const event = JSON.parse(frame.body)
            handleServerEvent(event)
          } catch {
            // malformed frame — ignore
          }
        })

        client.subscribe('/user/queue/errors', (frame) => {
          // Auth errors (expired token) arrive here; reload the page to force re-login
          try {
            const err = JSON.parse(frame.body)
            if (err.code === 'TOKEN_EXPIRED' || err.code === 'AUTH_REQUIRED') {
              window.location.reload()
            }
          } catch {
            // ignore parse errors
          }
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError: () => setConnected(false),
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
      setConnected(false)
    }
  }, [user]) // reconnect if user changes (re-login)

  // -----------------------------------------------------------------------
  // Handle push events from the server
  // -----------------------------------------------------------------------
  // We store this in a ref so the STOMP subscription closure always uses the
  // latest version without needing to re-subscribe.
  const handleServerEventRef = useRef(null)

  const handleServerEvent = useCallback((event) => {
    if (event.type === 'MESSAGE') {
      setConversations((prev) => {
        const idx = prev.findIndex((c) => c.id === event.conversationId)
        if (idx === -1) {
          // New direct conversation that the inbox hasn't seen yet — reload
          loadConversations()
          return prev
        }
        const updated = [...prev]
        const conv = { ...updated[idx] }
        conv.lastMessage = event.message
        conv.updatedAt = event.message.createdAt
        // Only increment unread if this message is not from the current user
        if (event.message.author?.id !== window.__alumniConnectUserId) {
          conv.unreadCount = (conv.unreadCount ?? 0) + 1
        }
        updated[idx] = conv
        // Re-sort so the most recently active conversation is at the top
        updated.sort((a, b) => (b.updatedAt ?? '').localeCompare(a.updatedAt ?? ''))
        return updated
      })
      // Recompute total
      setUnreadTotal((prev) => prev + 1)
    } else if (event.type === 'READ') {
      setConversations((prev) =>
        prev.map((c) =>
          c.id === event.conversationId ? { ...c, unreadCount: 0 } : c
        )
      )
      // Recalculate total from scratch to stay accurate
      setConversations((prev) => {
        setUnreadTotal(prev.reduce((s, c) => s + (c.unreadCount ?? 0), 0))
        return prev
      })
    }
  }, [loadConversations])

  // Keep the ref current
  useEffect(() => {
    handleServerEventRef.current = handleServerEvent
  })

  // Patch the subscription closure to use the ref
  // (The STOMP subscription is created once on connect; we route through the ref)
  const handleServerEventStable = useCallback((event) => {
    handleServerEventRef.current?.(event)
  }, [])

  // Store the current user id on window so the event handler (which closes over nothing)
  // can compare author ids without re-subscribing
  useEffect(() => {
    if (user?.id) window.__alumniConnectUserId = user.id
  }, [user?.id])

  // -----------------------------------------------------------------------
  // Send helpers exposed to chat screens
  // -----------------------------------------------------------------------
  const sendMessage = useCallback((conversationId, content, clientId) => {
    const c = clientRef.current
    if (!c?.connected) return false
    c.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ conversationId, content, clientId }),
    })
    return true
  }, [])

  const markRead = useCallback((conversationId, messageId) => {
    const c = clientRef.current
    if (c?.connected) {
      c.publish({
        destination: '/app/chat.read',
        body: JSON.stringify({ conversationId, messageId }),
      })
    }
    // Optimistic local update
    setConversations((prev) =>
      prev.map((conv) =>
        conv.id === conversationId ? { ...conv, unreadCount: 0 } : conv
      )
    )
    setUnreadTotal((prev) => Math.max(0, prev - 1))
  }, [])

  // -----------------------------------------------------------------------
  // Context value
  // -----------------------------------------------------------------------
  const value = useMemo(
    () => ({
      conversations,
      unreadTotal,
      connected,
      sendMessage,
      markRead,
      reloadConversations: loadConversations,
      setConversations,
    }),
    [conversations, unreadTotal, connected, sendMessage, markRead, loadConversations]
  )

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useChat() {
  return useContext(ChatContext)
}
