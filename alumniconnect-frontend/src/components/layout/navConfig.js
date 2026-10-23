import {
  LayoutDashboard,
  Newspaper,
  MessageSquare,
  UsersRound,
  Users,
  Handshake,
  CalendarDays,
  Briefcase,
  Megaphone,
  Bell,
  UserCog,
  UploadCloud,
  BarChart3,
  ScrollText,
  ShieldCheck,
  Flag,
  IdCard,
} from 'lucide-react'
import { ROLES, ROUTES } from '@/utils/constants'

// `badgeKey` ties an item to a live counter (see useNavBadges in NavItem.jsx).
const shared = [
  { to: ROUTES.DASHBOARD, label: 'Dashboard', icon: LayoutDashboard },
  { to: ROUTES.FEED, label: 'Feed', icon: Newspaper },
  { to: ROUTES.MESSAGES, label: 'Messages', icon: MessageSquare, badgeKey: 'chat' },
  { to: ROUTES.GROUPS, label: 'Groups', icon: UsersRound },
  { to: ROUTES.DIRECTORY, label: 'Alumni Directory', icon: Users },
  { to: ROUTES.MENTORSHIP, label: 'Mentorship', icon: Handshake },
  { to: ROUTES.EVENTS, label: 'Events', icon: CalendarDays },
  { to: ROUTES.JOBS, label: 'Jobs', icon: Briefcase },
  { to: ROUTES.ANNOUNCEMENTS, label: 'Announcements', icon: Megaphone },
]

export const NAV_BY_ROLE = {
  [ROLES.STUDENT]: shared,
  [ROLES.ALUMNI]: shared,
  [ROLES.ADMIN]: [
    { to: ROUTES.DASHBOARD, label: 'Dashboard', icon: LayoutDashboard },
    { to: ROUTES.ADMIN_ALUMNI, label: 'Alumni Records', icon: UserCog },
    { to: ROUTES.ADMIN_IMPORT, label: 'CSV / Excel Import', icon: UploadCloud },
    { to: ROUTES.ADMIN_USERS, label: 'Accounts', icon: IdCard },
    { to: ROUTES.ADMIN_EVENTS, label: 'Events', icon: CalendarDays },
    { to: ROUTES.ADMIN_JOBS, label: 'Job Moderation', icon: Briefcase },
    { to: ROUTES.ANNOUNCEMENTS, label: 'Announcements', icon: Megaphone },
    { to: ROUTES.ADMIN_POSTS, label: 'Feed Moderation', icon: Flag },
    { to: ROUTES.MESSAGES, label: 'Department Rooms', icon: MessageSquare, badgeKey: 'chat' },
    { to: ROUTES.GROUPS, label: 'Groups', icon: UsersRound },
    { to: ROUTES.ADMIN_ANALYTICS, label: 'Analytics', icon: BarChart3 },
    { to: ROUTES.ADMIN_AUDIT_LOG, label: 'Audit Log', icon: ScrollText },
  ],
}

export const FOOTER_NAV = [
  { to: ROUTES.NOTIFICATIONS, label: 'Notifications', icon: Bell },
  { to: ROUTES.PROFILE, label: 'Profile Settings', icon: ShieldCheck },
]
