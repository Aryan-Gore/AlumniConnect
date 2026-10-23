# AlumniConnect — Frontend

React + Vite frontend for the AlumniConnect major project: a centralized
alumni data management and engagement platform for SVVV.

This app talks directly to the Spring Boot REST API (`alumniconnect-backend`).
It does **not** call the Python/FastAPI analytics service — that runs as an
internal dependency of the Spring Boot backend, which exposes its results as
ordinary REST endpoints.

## Tech stack

- **React 18** + **Vite** (rolldown-powered build)
- **React Router v6** for routing and role-gated route protection
- **Tailwind CSS v4** for styling — theme tokens live in `src/styles/tokens.css`
- **Axios** for the API client, with a JWT request interceptor and centralized 401 handling
- **Recharts** for the admin analytics charts
- **lucide-react** for icons

## Getting started

```bash
npm install
cp .env.example .env      # set VITE_API_BASE_URL=http://localhost:8080/api
npm run dev                # start the dev server at http://localhost:5173
npm run build              # production build — output in dist/
npm run lint               # oxlint
```

The fastest way to run the full stack (MySQL + Spring Boot + frontend) is via
Docker Compose from the `alumniconnect-backend` directory:

```bash
docker compose up --build
```

This starts MySQL (with schema + demo data loaded automatically), the Spring
Boot backend, the Python analytics service, and an Nginx-served production
build of this frontend — all at once.

## Folder structure

```
src/
├── api/            One file per resource (authApi, alumniApi, eventsApi, ...).
│                   All requests go through axiosClient.js — the only place
│                   the base URL and JWT Authorization header are configured.
├── app/            App.jsx composition root + router.jsx (full route table).
├── components/
│   ├── ui/         Design-system primitives: Button, Card, Badge/StatusBadge,
│   │               Input/Select, StatCard, EmptyState, Spinner.
│   ├── layout/     AppShell, Sidebar (role-aware nav), Topbar, AuthLayout.
│   └── common/     PageHeader.
├── context/        AuthContext — session state and login/register/claim/logout.
├── routes/         ProtectedRoute — auth + role gating for the router.
├── features/       One folder per module (auth, landing, dashboard, directory,
│                   mentorship, events, jobs, announcements, notifications,
│                   profile, admin, errors) — each owns its own page(s).
├── hooks/          useAuth (thin AuthContext accessor).
└── utils/          constants.js (roles/statuses/routes), formatters.js,
                    cn.js, chartTheme.js (Recharts color tokens).
```

## What's built

Every screen in the SRS is built, wired into the router, and connected to
the real API — no mock data or placeholders remain:

- **Auth** — login, student registration, and the two-step claim-profile flow
- **Public** — landing page with live platform stats
- **Dashboards** — Student (every widget from the SRS), Alumni (mentorship
  requests, own job postings, registered events), Admin (stat tiles + charts)
- **Alumni directory** — search/filter/paginate, profile detail with the
  institution-vs-alumni field split, mentorship request action
- **Mentorship** — student's sent-request tracker; alumni's accept/reject inbox
- **Events** — browse, register/cancel, capacity bar
- **Jobs** — browse/apply for everyone; post + manage own listings for alumni
- **Announcements** — role-filtered feed; inline composer for admins
- **Notifications** — full history, read/unread, mark-all-read
- **Profile settings** — role-aware form (student vs. alumni field sets),
  institution-controlled fields shown read-only with lock icon
- **Admin** — Alumni Records (search/filter/activate/invite/paginate), CSV Import
  (upload → column mapping → preview → confirm), Event Management,
  Job Moderation, Analytics (industry/location/department/batch charts), Audit Log

## Architecture notes

- **One backend from the browser's point of view.** `src/api/axiosClient.js`
  is the only place a base URL or auth header is configured. Spring Boot
  invokes the Python analytics service internally and exposes its results as
  ordinary REST endpoints — the frontend never communicates with FastAPI.
- **Auth** is JWT-based, stored in `sessionStorage` (not `localStorage`, so a
  closed tab doesn't leave a live session behind). A 401 from any request
  clears the session and redirects to `/login` exactly once, centrally.
- **Role gating** happens at two levels: `ProtectedRoute` (route-level — an
  unauthenticated user or wrong role never renders the page) and `navConfig.js`
  (nav items only show what that role can reach). The backend still enforces
  this independently — this is UX, not security.
- Alumni records visually separate **institution-controlled** fields (lock icon,
  read-only) from **alumni-controlled** ones (editable), matching the
  data-ownership split in the SRS and the backend DTOs.

## Design system

Light and clean — white cards on a soft warm-gray canvas, one confident
blue accent. Tokens live in `src/styles/tokens.css`; change them there and
they propagate everywhere (`bg-surface`, `text-accent`, `font-display`, etc.
are all generated from that file via Tailwind v4's `@theme`). IBM Plex Sans
for UI text, IBM Plex Mono for records (enrollment numbers, batch years, stat
counts). Recharts cannot consume CSS variables directly, so `utils/chartTheme.js`
mirrors the relevant token values as a plain object — update it alongside
`tokens.css` if the palette changes.

## Demo credentials

When the database is seeded with `demo_data.sql` (done automatically by
Docker Compose), these accounts are ready:

| Role    | Email                        | Password    |
|---------|------------------------------|-------------|
| Admin   | admin@svvv.edu.in            | Password@123 |
| Alumni  | rahul.sharma@gmail.com       | Password@123 |
| Alumni  | vikram.singh@hotmail.com     | Password@123 |
| Student | arjun.meena@svvv.edu.in      | Password@123 |
| Student | kavya.patel@svvv.edu.in      | Password@123 |

## Known follow-ups

- The production bundle is ~818 KB uncompressed (~220 KB gzipped), mostly
  Recharts. Route-level code-splitting (`React.lazy`) on the four admin
  screens that pull it in is straightforward and would help first-load.
- `AuthContext` and its `useContext` hook currently live in the same file
  (`context/AuthContext.jsx`); a Vite fast-refresh lint rule flags this.
  Harmless, but a two-file split is easy if it becomes noisy.
