import { axiosClient, cleanParams } from './axiosClient'

const data = (res) => res.data

// REST half of chat; live delivery is the STOMP socket (see features/chat).
// Contract: docs/API_CONTRACT.md §8 in the backend project.
export const chatApi = {
  // The user's department room(s) plus every direct thread that has a message, latest activity first.
  conversations: () => axiosClient.get('/chat/conversations').then(data),
  conversation: (conversationId) => axiosClient.get(`/chat/conversations/${conversationId}`).then(data),
  // Find-or-create the one-to-one thread with `userId` (a USER id, not an alumni id).
  startDirect: (userId) => axiosClient.post('/chat/conversations/direct', { userId }).then(data),

  // { items (oldest -> newest within the page), nextBefore }; pass nextBefore as `before` for older messages.
  messages: (conversationId, params) =>
    axiosClient.get(`/chat/conversations/${conversationId}/messages`, { params: cleanParams(params) }).then(data),
  // REST fallback for sending; the socket is the normal path.
  send: (conversationId, content, clientId) =>
    axiosClient.post(`/chat/conversations/${conversationId}/messages`, { content, clientId }).then(data),
  // -> { conversationId, unreadCount, totalUnread }
  markRead: (conversationId, messageId) =>
    axiosClient.post(`/chat/conversations/${conversationId}/read`, { messageId }).then(data),
  // -> { count }
  unreadCount: () => axiosClient.get('/chat/unread-count').then(data),

  // People search for "new chat": q must be at least 2 characters. -> PageResponse<UserSummary>
  searchUsers: (q, params) => axiosClient.get('/chat/users', { params: cleanParams({ q, ...params }) }).then(data),
}
