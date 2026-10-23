import { axiosClient } from './axiosClient'

export const notificationsApi = {
  // Newest first: [{ id, category, message, read, createdAt }]
  list: () => axiosClient.get('/notifications').then((res) => res.data),
  unreadCount: () => axiosClient.get('/notifications/unread-count').then((res) => res.data),
  markRead: (notificationId) => axiosClient.patch(`/notifications/${notificationId}/read`).then((res) => res.data),
  markAllRead: () => axiosClient.patch('/notifications/read-all').then((res) => res.data),
}
