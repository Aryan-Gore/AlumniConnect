import { axiosClient, cleanParams } from './axiosClient'

const data = (res) => res.data

// Any signed-in user can post a listing; admins moderate everyone else's.
export const jobsApi = {
  // APPROVED and not expired, newest first.
  list: () => axiosClient.get('/jobs').then(data),
  getById: (jobId) => axiosClient.get(`/jobs/${jobId}`).then(data),
  apply: (jobId) => axiosClient.post(`/jobs/${jobId}/apply`).then(data),
  myApplications: () => axiosClient.get('/jobs/applications/me').then(data),

  // { title, company, location?, jobType, description?, expiresAt? ('2026-12-31') }
  post: (payload) => axiosClient.post('/jobs', payload).then(data),
  update: (jobId, payload) => axiosClient.put(`/jobs/${jobId}`, payload).then(data),
  myPostings: () => axiosClient.get('/jobs/mine').then(data),
  applicationsForJob: (jobId) => axiosClient.get(`/jobs/${jobId}/applications`).then(data),

  // Admin moderation — status: PENDING | APPROVED | REJECTED | REMOVED | EXPIRED
  adminModerationQueue: (params) => axiosClient.get('/admin/jobs', { params: cleanParams(params) }).then(data),
  adminSetStatus: (jobId, status) => axiosClient.patch(`/admin/jobs/${jobId}/status`, { status }).then(data),
}
