import { axiosClient, postFile } from './axiosClient'

export const profileApi = {
  // Any role. JPEG / PNG / GIF / WebP up to 5 MB. Resolves to { url }.
  uploadPhoto: (file) => postFile('/profile/photo', file),
  removePhoto: () => axiosClient.delete('/profile/photo').then(() => undefined),

  // Autocomplete for skill inputs: up to 10 existing skill names starting with `q`.
  suggestSkills: (q) => axiosClient.get('/skills/suggest', { params: { q } }).then((res) => res.data),

  // An image for a feed post. Resolves to { url }; put that url in the post's imageUrl.
  uploadPostImage: (file) => postFile('/files/posts', file),
}
