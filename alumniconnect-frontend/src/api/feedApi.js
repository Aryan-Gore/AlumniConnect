import { axiosClient, cleanParams } from './axiosClient'

const data = (res) => res.data

// Contract: docs/API_CONTRACT.md §9 in the backend project.
export const feedApi = {
  // params: { before, limit, departmentId, authorId } -> { items: Post[], nextBefore }
  feed: (params) => axiosClient.get('/feed', { params: cleanParams(params) }).then(data),

  // { content, imageUrl?, visibility: 'PUBLIC' | 'ALUMNI_ONLY' | 'DEPARTMENT' | 'CONNECTIONS' }
  createPost: (payload) => axiosClient.post('/posts', payload).then(data),
  getPost: (postId) => axiosClient.get(`/posts/${postId}`).then(data),
  deletePost: (postId) => axiosClient.delete(`/posts/${postId}`).then(() => undefined),

  // Both resolve to { liked, likeCount } and are idempotent.
  like: (postId) => axiosClient.post(`/posts/${postId}/like`).then(data),
  unlike: (postId) => axiosClient.delete(`/posts/${postId}/like`).then(data),

  comments: (postId) => axiosClient.get(`/posts/${postId}/comments`).then(data),
  addComment: (postId, content) => axiosClient.post(`/posts/${postId}/comments`, { content }).then(data),
  deleteComment: (postId, commentId) =>
    axiosClient.delete(`/posts/${postId}/comments/${commentId}`).then(() => undefined),
}
