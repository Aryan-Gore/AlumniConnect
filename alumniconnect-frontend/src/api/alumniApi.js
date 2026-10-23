import { axiosClient, cleanParams } from './axiosClient'

const data = (res) => res.data

// POST/PUT/DELETE helpers for one repeating section of the alumnus' profile
// (career-history, education, projects, certifications).
function section(path) {
  return {
    add: (payload) => axiosClient.post(`/alumni/me/${path}`, payload).then(data),
    update: (id, payload) => axiosClient.put(`/alumni/me/${path}/${id}`, payload).then(data),
    remove: (id) => axiosClient.delete(`/alumni/me/${path}/${id}`).then(() => undefined),
  }
}

export const alumniApi = {
  // ---- Directory (any signed-in role; privacy rules are applied server-side) ----
  // params: query, instituteId, departmentId, batch, graduationYear, company, industry,
  // location, skills[], openToMentoring, availableForHiring, sort ('name'|'year'|'recent'), page, size
  // -> { items: AlumniCard[], page, size, totalElements, totalPages }
  search: (params) => axiosClient.get('/alumni', { params: cleanParams(params) }).then(data),
  getById: (alumniId) => axiosClient.get(`/alumni/${alumniId}`).then(data),

  // ---- The alumnus' own profile (role ALUMNI) ----
  getOwn: () => axiosClient.get('/alumni/me').then(data),
  // PATCH semantics: only the fields you send change; `skills` replaces the whole list.
  updateOwn: (payload) => axiosClient.patch('/alumni/me', payload).then(data),
  careerHistory: section('career-history'),
  education: section('education'),
  projects: section('projects'),
  certifications: section('certifications'),

  // ---- Admin: alumni records ----
  adminList: (params) => axiosClient.get('/admin/alumni', { params: cleanParams(params) }).then(data),
  adminCreate: (payload) => axiosClient.post('/admin/alumni', payload).then(data),
  adminUpdate: (alumniId, payload) => axiosClient.put(`/admin/alumni/${alumniId}`, payload).then(data),
  adminSetActive: (alumniId, isActive) =>
    axiosClient.patch(`/admin/alumni/${alumniId}/status`, { isActive }).then(data),
  adminSetVerified: (alumniId, verified) =>
    axiosClient.patch(`/admin/alumni/${alumniId}/verify`, { verified }).then(data),
  adminInviteToClaim: (alumniId) => axiosClient.post(`/admin/alumni/${alumniId}/invite`).then(data),
  // Resolves to a Blob (text/csv).
  adminExport: (params) =>
    axiosClient.get('/admin/alumni/export', { params: cleanParams(params), responseType: 'blob' }).then(data),

  // ---- Admin: CSV bulk import — upload -> map columns -> preview -> confirm ----
  uploadImportFile: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return axiosClient
      .post('/admin/alumni/import/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then(data)
  },
  submitColumnMapping: (importId, mapping) =>
    axiosClient.post(`/admin/alumni/import/${importId}/mapping`, { mapping }).then(data),
  getImportPreview: (importId) => axiosClient.get(`/admin/alumni/import/${importId}/preview`).then(data),
  confirmImport: (importId) => axiosClient.post(`/admin/alumni/import/${importId}/confirm`).then(data),
  getImportHistory: () => axiosClient.get('/admin/alumni/import/history').then(data),
}
