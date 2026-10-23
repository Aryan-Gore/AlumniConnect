import { Routes, Route } from 'react-router-dom'
import { ProtectedRoute } from '@/routes/ProtectedRoute'
import { AppShell } from '@/components/layout/AppShell'
import { AuthLayout } from '@/components/layout/AuthLayout'

import { LandingPage } from '@/features/landing/LandingPage'
import { LoginPage } from '@/features/auth/LoginPage'
import { RegisterPage } from '@/features/auth/RegisterPage'
import { ClaimProfilePage } from '@/features/auth/ClaimProfilePage'
import { DashboardPage } from '@/features/dashboard/DashboardPage'
import { FeedPage } from '@/features/feed/FeedPage'
import { MessagesPage } from '@/features/chat/MessagesPage'
import { GroupsPage } from '@/features/groups/GroupsPage'
import { GroupDetailPage } from '@/features/groups/GroupDetailPage'
import { AlumniDirectoryPage } from '@/features/directory/AlumniDirectoryPage'
import { AlumniProfileDetailPage } from '@/features/directory/AlumniProfileDetailPage'
import { MentorshipPage } from '@/features/mentorship/MentorshipPage'
import { EventsPage } from '@/features/events/EventsPage'
import { JobsPage } from '@/features/jobs/JobsPage'
import { AnnouncementsPage } from '@/features/announcements/AnnouncementsPage'
import { NotificationsPage } from '@/features/notifications/NotificationsPage'
import { ProfileSettingsPage } from '@/features/profile/ProfileSettingsPage'
import { AlumniManagementPage } from '@/features/admin/AlumniManagementPage'
import { CsvImportPage } from '@/features/admin/CsvImportPage'
import { EventManagementPage } from '@/features/admin/EventManagementPage'
import { JobModerationPage } from '@/features/admin/JobModerationPage'
import { PostModerationPage } from '@/features/admin/PostModerationPage'
import { UserManagementPage } from '@/features/admin/UserManagementPage'
import { AnalyticsPage } from '@/features/admin/AnalyticsPage'
import { AuditLogPage } from '@/features/admin/AuditLogPage'
import { NotFoundPage } from '@/features/errors/NotFoundPage'
import { ROUTES, ROLES } from '@/utils/constants'

export function AppRoutes() {
  return (
    <Routes>
      <Route path={ROUTES.HOME} element={<LandingPage />} />

      <Route element={<AuthLayout />}>
        <Route path={ROUTES.LOGIN} element={<LoginPage />} />
        <Route path={ROUTES.REGISTER} element={<RegisterPage />} />
        <Route path={ROUTES.CLAIM_PROFILE} element={<ClaimProfilePage />} />
      </Route>

      {/* Any authenticated role */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
          <Route path={ROUTES.FEED} element={<FeedPage />} />
          <Route path={ROUTES.MESSAGES} element={<MessagesPage />} />
          <Route path={ROUTES.MESSAGE_THREAD} element={<MessagesPage />} />
          <Route path={ROUTES.GROUPS} element={<GroupsPage />} />
          <Route path={ROUTES.GROUP_DETAIL} element={<GroupDetailPage />} />
          <Route path={ROUTES.DIRECTORY} element={<AlumniDirectoryPage />} />
          <Route path={ROUTES.DIRECTORY_DETAIL} element={<AlumniProfileDetailPage />} />
          <Route path={ROUTES.MENTORSHIP} element={<MentorshipPage />} />
          <Route path={ROUTES.EVENTS} element={<EventsPage />} />
          <Route path={ROUTES.JOBS} element={<JobsPage />} />
          <Route path={ROUTES.ANNOUNCEMENTS} element={<AnnouncementsPage />} />
          <Route path={ROUTES.NOTIFICATIONS} element={<NotificationsPage />} />
          <Route path={ROUTES.PROFILE} element={<ProfileSettingsPage />} />

          {/* Admin-only */}
          <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN]} />}>
            <Route path={ROUTES.ADMIN_ALUMNI} element={<AlumniManagementPage />} />
            <Route path={ROUTES.ADMIN_IMPORT} element={<CsvImportPage />} />
            <Route path={ROUTES.ADMIN_USERS} element={<UserManagementPage />} />
            <Route path={ROUTES.ADMIN_EVENTS} element={<EventManagementPage />} />
            <Route path={ROUTES.ADMIN_JOBS} element={<JobModerationPage />} />
            <Route path={ROUTES.ADMIN_POSTS} element={<PostModerationPage />} />
            <Route path={ROUTES.ADMIN_ANALYTICS} element={<AnalyticsPage />} />
            <Route path={ROUTES.ADMIN_AUDIT_LOG} element={<AuditLogPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
