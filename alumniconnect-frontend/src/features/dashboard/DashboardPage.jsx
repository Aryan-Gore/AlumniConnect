import { useAuth } from '@/hooks/useAuth'
import { ROLES } from '@/utils/constants'
import { StudentDashboard } from './StudentDashboard'
import { AlumniDashboard } from './AlumniDashboard'
import { AdminDashboard } from './AdminDashboard'

export function DashboardPage() {
  const { role } = useAuth()

  if (role === ROLES.ADMIN) return <AdminDashboard />
  if (role === ROLES.ALUMNI) return <AlumniDashboard />
  return <StudentDashboard />
}
