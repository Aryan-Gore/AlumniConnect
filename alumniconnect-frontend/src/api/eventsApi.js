import { axiosClient } from './axiosClient'

const data = (res) => res.data

export const eventsApi = {
  // Published events, soonest first. Each has `registeredByViewer` and `registeredCount`.
  list: () => axiosClient.get('/events').then(data),
  getById: (eventId) => axiosClient.get(`/events/${eventId}`).then(data),
  register: (eventId) => axiosClient.post(`/events/${eventId}/register`).then(data),
  cancelRegistration: (eventId) => axiosClient.delete(`/events/${eventId}/register`).then(data),
  myRegistrations: () => axiosClient.get('/events/me').then(data),

  // Admin — every status, including drafts.
  adminList: () => axiosClient.get('/admin/events').then(data),
  // { title, description?, eventDate ('2026-11-05T15:30:00', must be in the future), mode, location?, capacity (>=1) }
  adminCreate: (payload) => axiosClient.post('/admin/events', payload).then(data),
  adminUpdate: (eventId, payload) => axiosClient.put(`/admin/events/${eventId}`, payload).then(data),
  adminPublish: (eventId) => axiosClient.patch(`/admin/events/${eventId}/publish`).then(data),
  adminCancel: (eventId) => axiosClient.patch(`/admin/events/${eventId}/cancel`).then(data),
}
