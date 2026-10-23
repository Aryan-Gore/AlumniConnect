import { axiosClient } from './axiosClient'

const data = (res) => res.data

export const announcementsApi = {
  // Only what the viewer's role / department / batch is targeted by (admins see everything published).
  list: () => axiosClient.get('/announcements').then(data),

  // Admin
  adminList: () => axiosClient.get('/admin/announcements').then(data),
  // { title, content, targetAudience, targetDepartmentId? (DEPARTMENT), targetBatch? (BATCH), priority, publishDate? }
  adminCreate: (payload) => axiosClient.post('/admin/announcements', payload).then(data),
  adminUpdate: (announcementId, payload) =>
    axiosClient.put(`/admin/announcements/${announcementId}`, payload).then(data),
  // status: DRAFT | PUBLISHED | ARCHIVED — sent as a query parameter, not a body.
  adminSetStatus: (announcementId, status) =>
    axiosClient.patch(`/admin/announcements/${announcementId}/status`, null, { params: { status } }).then(data),
}
