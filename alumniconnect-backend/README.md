# AlumniConnect — Backend

Spring Boot REST API for the AlumniConnect major project — implements
every endpoint the frontend's `src/api/*.js` modules already call.

## A limitation worth stating plainly

This was written in a sandboxed environment with no network path to Maven
Central, so **it has not been compiled here** — unlike the database schema
(loaded into a real MariaDB instance) and the Python analytics service
(dependencies actually installed from PyPI, tests actually run). The Java
is written as carefully as the rest of this project, but it doesn't carry
the same "proven to build" guarantee. The very first thing to do anywhere
with normal internet access:

```bash
mvn clean compile
```

and fix whatever that surfaces. Straightforward Spring Boot / Lombok /
JPA code like this usually compiles clean on the first real attempt, but
"usually" isn't "verified," and this README won't pretend otherwise.

## Setup

**Fastest path — Docker Compose** (brings up MySQL, this backend, the
Python analytics service, and the frontend together; see `docker-compose.yml`
for the expected sibling-folder layout):

```bash
docker compose up --build
```

**Manual path:**

```bash
# 1. Create the database and load demo data
mysql -u root < database/schema.sql
mysql -u root alumniconnect < database/demo_data.sql

# 2. Configure environment (see application.yml for the full list)
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export JWT_SECRET=$(openssl rand -base64 64)
export FRONTEND_ORIGIN=http://localhost:5173
export ANALYTICS_SERVICE_URL=http://localhost:8000   # run alumniconnect-analytics separately

# 3. Run
mvn spring-boot:run
```

API base URL: `http://localhost:8080/api` — matches `VITE_API_BASE_URL`
in the frontend's `.env.example` exactly.
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

## Architecture

```
React (frontend)  →  Spring Boot (this)  →  MySQL
                           ↓
                   Python/FastAPI analytics service
                   (internal only — see alumniconnect-analytics/)
```

This resolves the Python-data-flow ambiguity flagged during the SRS
review: the frontend has exactly one backend. Spring Boot is responsible
for calling the analytics service internally (`ANALYTICS_SERVICE_URL`)
when it needs Pandas-computed statistics or company-name cleanup — that
call never happens from the browser.

### Security

JWT-based, stateless (`SecurityConfig` + `JwtAuthFilter` +
`CustomUserDetailsService`). `/auth/**` is public; `/admin/**` requires
`ROLE_ADMIN`; everything else requires any authenticated role. This
duplicates the route guards already in the frontend's `ProtectedRoute` —
intentionally. The frontend's rules are UX; these are the actual
enforcement, and one is not a substitute for the other.

### The institution-controlled / alumni-controlled split

Enforced by DTO shape, not just a runtime check: `AlumniSelfUpdateRequest`
structurally cannot carry `enrollmentNumber`, `department`, or any other
institution-controlled field — the class doesn't have those properties.
`AdminAlumniUpdateRequest` is the mirror image. See
`database/ER_DIAGRAM.md` for the full reasoning.

### Claim flow

`AuthService.claimProfile()` re-runs the exact same match query the
lookup step used, rather than trusting the `alumniId` the client sent —
the lookup step is informational, not an authorization decision. This is
also what guarantees a claim never creates a second `alumni_records` row:
it only ever sets `user_id` on an existing one.

## What's actually implemented

Every module the frontend calls has a real controller behind it:

| Module | Status |
|---|---|
| Auth (login, register, claim) | Complete |
| Alumni directory + self-service | Complete, with a real visibility rule (`DirectoryVisibility`) |
| Admin alumni management | Complete, audit-logged |
| CSV/Excel import (upload → map → preview → confirm) | Complete — real Apache Commons CSV parsing, not naive string-splitting |
| Events (browse/register/cancel + admin lifecycle) | Complete, capacity-enforced |
| Mentorship (request/accept/reject/complete) | Complete |
| Jobs (browse/apply/post/moderate) | Complete |
| Announcements (role+department+batch filtered feed) | Complete |
| Notifications (list/unread-count/mark-read) | Complete |
| Admin dashboard + analytics | Complete — SQL aggregates; heavier stats delegate to the Python service |
| Audit log | Complete, read-only by construction (no update/delete method exists) |

## Known follow-ups

- **Not compiled** — see above. Run `mvn clean compile` first thing.
  (I checked whether Ubuntu's apt repos could substitute for Maven Central:
  they package Spring Framework 4.3, from the pre-Boot, pre-Jakarta-EE era —
  not usable for a Spring Boot 3.3 / Spring Security 6 project. No shortcut
  there.)
- **Unit tests exist now** (`AuthServiceTest`, `EventServiceTest`,
  `AlumniServiceTest` — the claim-flow re-verification, capacity
  enforcement, and directory-visibility rules) **but have never run**, for
  the same compilation reason. Writing them without a compiler did catch
  one real bug by hand: `RowVerdict` is a Java record, and record
  components are accessor *methods* (`verdict.flag()`), not fields
  (`verdict.flag`) — I'd written the field form in three places in
  `AlumniImportService` and only caught it re-reading the code with that
  specific question in mind. Worth remembering that "written carefully"
  and "compiled" are different things, which is the whole reason this
  section keeps repeating itself.
- **Company-name cleanup is now wired into CSV import** — confirm() batches
  every row's company name into one call to the Python service before
  inserting, and degrades to the raw names if that service is unreachable
  (`AnalyticsClient`). This was a follow-up last time; it isn't anymore.
- **CSV import stages the raw file in a database column**
  (`alumni_import_batches.staged_content`), cleared after confirm. Fine
  for the file sizes an academic alumni import realistically involves;
  a larger deployment would use object storage instead.
- **Email delivery is out of scope**, matching the SRS's own "Optional:
  Email service" — `adminInviteToClaim` changes status but doesn't send
  anything.
