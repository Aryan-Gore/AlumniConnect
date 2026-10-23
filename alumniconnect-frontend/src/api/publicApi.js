import { axiosClient } from './axiosClient'

export const publicApi = {
  // { alumniRecords, alumniMembers, studentMembers, institutes, departments, upcomingEvents, openJobs }
  stats: () => axiosClient.get('/public/stats').then((res) => res.data),
}
