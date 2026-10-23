import { axiosClient } from './axiosClient'

export const studentApi = {
  // { userId, fullName, email, profilePhotoUrl, enrollmentNumber, department, batch, skills[], careerInterests, completion }
  getOwn: () => axiosClient.get('/students/me').then((res) => res.data),
  // { batch?, skills?, careerInterests?, departmentId? }
  updateOwn: (payload) => axiosClient.patch('/students/me', payload).then((res) => res.data),
}
