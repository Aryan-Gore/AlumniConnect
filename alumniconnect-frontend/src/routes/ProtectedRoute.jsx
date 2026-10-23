import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { ROUTES } from '@/utils/constants'
import { Spinner } from '@/components/ui/Spinner'

// allowedRoles omitted -> any authenticated role may pass.
// allowedRoles=['ADMIN'] -> only admins; everyone else bounces to /dashboard.
export function ProtectedRoute({ allowedRoles }) {
  const { isAuthenticated, isInitializing, role } = useAuth()
  const location = useLocation()

  if (isInitializing) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-bg">
        <Spinner label="Checking your session…" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />
  }

  if (allowedRoles && !allowedRoles.includes(role)) {
    return <Navigate to={ROUTES.DASHBOARD} replace />
  }

  return <Outlet />
}
