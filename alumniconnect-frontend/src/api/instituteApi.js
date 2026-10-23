import { axiosClient } from './axiosClient'

// The Institute -> Department structure never changes while the app is open,
// so it is fetched once and shared by every picker.
let cache = null

export const instituteApi = {
  /** [{ id, name, code, departments: [{ id, name, code }] }] */
  list: () => {
    if (!cache) {
      cache = axiosClient
        .get('/institutes')
        .then((res) => res.data)
        .catch((error) => {
          cache = null // let the next caller retry
          throw error
        })
    }
    return cache
  },
}
