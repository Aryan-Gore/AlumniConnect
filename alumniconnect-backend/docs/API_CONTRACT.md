# AlumniConnect — API contract

This is the single source of truth for what the backend exposes and what the
frontend may rely on. Where code and this document disagree, fix the code or
fix this document in the same change — never let them drift.

* **Base URL:** `http://localhost:8080/api` (Spring `context-path: /api`). Every path below is relative to it.
* **Format:** JSON, UTF-8. Property names are `camelCase`. Enums are upper-case strings.
* **Auth:** `Authorization: Bearer <jwt>` on everything except the public routes marked 🌐. `401` = missing/invalid/expired token. `403` = signed in but not allowed.
* **Dates:** `Instant` → ISO-8601 UTC string (`"2026-10-13T07:49:12.123Z"`). `LocalDateTime` (event dates) → `"2026-11-05T15:30:00"` (campus time, no zone). `LocalDate` → `"2026-11-05"`.
* **IDs:** numbers (`Long`). A *user id* (`users.id`) is not an *alumni id* (`alumni_records.id`): `UserSummaryResponse.id` is a user id, `UserSummaryResponse.alumniId` is the alumni id (only for alumni).
* **Files:** every stored image URL is a path relative to the API base, e.g. `/files/profiles/3f2a….jpg`. Display it as `API_BASE + url` (frontend helper `assetUrl()`); `GET /files/**` is public because `<img>` tags cannot send a bearer token.
* **"Hidden = 404":** when a record exists but the viewer may not see it, the API answers `404`, never `403`, so existence is not leaked.

## 0. Shared shapes

### ErrorResponse (every non-2xx)
```json
{ "timestamp": "…", "status": 400, "message": "Human readable sentence.", "path": "/api/…",
  "fieldErrors": { "email": "Enter a valid email address" } }   // fieldErrors only for validation failures
```
Statuses used: 400 bad input / business rule, 401, 403, 404, 405, 409 duplicate / constraint, 413 upload too large, 415, 500.

### PageResponse<T> (offset paging, `page` is 0-based)
```json
{ "items": [ … ], "page": 0, "size": 20, "totalElements": 134, "totalPages": 7 }
```
Unless an endpoint says otherwise: query params `page` (default 0) and `size` (default 20, max 100).

### CursorPage<T> (feed and chat history)
```json
{ "items": [ … ], "nextBefore": 4812 }     // nextBefore = pass as ?before= to get older items; null = no more
```
Query params: `before` (exclusive id cursor, omit for the newest) and `limit` (default 20, max 50 for the feed; default 30, max 100 for chat).

### DepartmentRef
```json
{ "id": 1, "name": "Computer Science & Engineering", "code": "CSE",
  "instituteId": 1, "instituteName": "Shri Vaishnav Institute of Technology & Science", "instituteCode": "SVITS" }
```

### UserSummaryResponse — "who is this person" (used next to every name in chat, feed, jobs, groups)
```json
{ "id": 7, "fullName": "Priya Sharma", "role": "ALUMNI", "profilePhotoUrl": "/files/profiles/….jpg" | null,
  "headline": "Software Engineer at Acme" | "Student · CSE · Batch 2026" | "Alumni office",
  "department": DepartmentRef | null,        // null for admins
  "alumniId": 31 | null,                     // alumni only — link to /directory/:alumniId
  "verified": true }                         // office-verified alumnus badge
```

### UserResponse (the signed-in user; `/auth/me`, login responses)
`{ id, fullName, email, role, profilePhotoUrl, department (DepartmentRef|null), alumniId|null, verified }`

### UploadResponse `{ "url": "/files/posts/….jpg" }`

### ProfileCompletionResponse
```json
{ "percent": 70, "items": [ { "key": "photo", "label": "Profile photo", "hint": "Upload a clear headshot", "points": 10, "done": true }, … ] }
```

### Enumerations
| Enum | Values |
|---|---|
| Role | `STUDENT`, `ALUMNI`, `ADMIN` |
| AlumniStatus | `IMPORTED`, `INVITED`, `CLAIMED`, `ACTIVE`, `INACTIVE` |
| DirectoryVisibility | `PUBLIC`, `STUDENTS_AND_ALUMNI`, `ALUMNI_ONLY`, `PRIVATE` |
| ContactVisibility | `PUBLIC`, `ALUMNI_ONLY`, `PRIVATE` |
| CertificationType | `CERTIFICATION`, `ACHIEVEMENT`, `AWARD`, `PUBLICATION` |
| MentorshipStatus | `PENDING`, `ACCEPTED`, `REJECTED`, `COMPLETED` |
| JobType | `FULL_TIME`, `INTERNSHIP`, `PART_TIME`, `CONTRACT` |
| JobStatus | `PENDING`, `APPROVED`, `REJECTED`, `REMOVED`, `EXPIRED` |
| ApplicationStatus | `SUBMITTED`, `REVIEWED`, `SHORTLISTED`, `REJECTED` |
| EventMode / EventStatus | `ONLINE`, `ON_CAMPUS` / `DRAFT`, `PUBLISHED`, `CANCELLED` |
| TargetAudience | `ALL`, `STUDENTS`, `ALUMNI`, `DEPARTMENT`, `BATCH` |
| Priority / AnnouncementStatus | `LOW`, `MEDIUM`, `HIGH` / `DRAFT`, `PUBLISHED`, `ARCHIVED` |
| PostVisibility | `PUBLIC`, `ALUMNI_ONLY`, `DEPARTMENT`, `CONNECTIONS` |
| ConversationType | `DIRECT`, `DEPARTMENT` |
| NotificationCategory | `MENTORSHIP`, `EVENT`, `JOB`, `ANNOUNCEMENT`, `ACCOUNT`, `MESSAGE`, `POST` |
| AuditAction | `ADD_ALUMNI`, `UPDATE_ALUMNI`, `IMPORT_ALUMNI`, `EXPORT_ALUMNI`, `INVITE_ALUMNI`, `DEACTIVATE_ALUMNI`, `CREATE_EVENT`, `UPDATE_EVENT`, `APPROVE_JOB`, `REJECT_JOB`, `REMOVE_POST`, `VERIFY_ALUMNI`, `REMOVE_MESSAGE`, `DEACTIVATE_USER`, `REACTIVATE_USER` |

---

## 1. Authentication

| Method & path | Auth | Body → Response |
|---|---|---|
| `POST /auth/login` 🌐 | – | `{email, password}` → `AuthResponse` |
| `POST /auth/register/student` 🌐 | – | `{fullName, email, enrollmentNumber, departmentId, batch?, password (8–100)}` → `AuthResponse` |
| `POST /auth/claim/lookup` 🌐 | – | `{fullName, enrollmentNumber, email, graduationYear (string "2023")}` → `ClaimLookupResponse` |
| `POST /auth/claim/confirm` 🌐 | – | `{alumniId, fullName, enrollmentNumber, email, graduationYear, password (≥8)}` → `AuthResponse` |
| `GET /auth/config` 🌐 | – | → `{ googleEnabled: boolean, googleClientId: string }` |
| `POST /auth/google` 🌐 | – | `{credential}` (Google ID token) → `GoogleLoginResponse` |
| `POST /auth/google/register` 🌐 | – | `{registrationToken, enrollmentNumber, departmentId, batch?}` → `AuthResponse` |
| `GET /auth/me` | any | → `UserResponse` |
| `POST /auth/logout` 🌐 | – | → `200` empty (stateless; the client just drops its token) |

```json
// AuthResponse
{ "token": "<jwt>", "user": UserResponse }
// ClaimLookupResponse
{ "id": 31, "fullName": "…", "enrollmentNumber": "0827CS201001", "department": DepartmentRef, "graduationYear": 2023 }
// GoogleLoginResponse
{ "status": "AUTHENTICATED", "auth": AuthResponse }                       // known Google account
{ "status": "REGISTRATION_REQUIRED", "registrationToken": "<15-min jwt>",  // new person: ask for enrollment no. + department
  "email": "…", "fullName": "…", "pictureUrl": "https://…" }
```
Rules: Google sign-in creates **student** accounts only (alumni use the claim flow). An existing password account with the same email is never silently merged (`409`). Bad credentials → `401 "Incorrect email or password."`; deactivated → `403`.

## 2. Reference data and public data

| Method & path | Auth | Response |
|---|---|---|
| `GET /institutes` 🌐 | – | `InstituteResponse[]` = `[{ id, name, code, departments: [{ id, name, code }] }]` (8 institutes / 23 departments of SVVV) |
| `GET /public/stats` 🌐 | – | `{ alumniRecords, alumniMembers, studentMembers, institutes, departments, upcomingEvents, openJobs }` (counts only) |
| `GET /skills/suggest?q=` | any | `string[]` — up to 10 existing skill names starting with `q` |

## 3. Alumni directory and profile

### Directory (any signed-in role)
`GET /alumni` → `PageResponse<AlumniCardResponse>` (default `size` 12, max 50). Query params: `query` (name / company / title / skill), `instituteId`, `departmentId`, `batch`, `graduationYear`, `company`, `industry`, `location`, `skills` (repeat: `skills=Java&skills=Spring` = must know both), `openToMentoring`, `availableForHiring`, `sort` (`name` default | `year` | `recent`), `page`, `size`.

`AlumniCardResponse`: `{ id, userId|null, fullName, profilePhotoUrl, verified, headline, department, batch, graduationYear, currentCompany, jobTitle, currentLocation, industry, skills[] (first few), openToMentoring, openToNetworking, availableForHiring }`

`GET /alumni/{id}` → `AlumniResponse` (below). `404` if the record is hidden from the viewer by its `directoryVisibility` (`PRIVATE` is never listed; `ALUMNI_ONLY` hides from students; owner and admins always see it).

```json
// AlumniResponse
{ "id": 31, "userId": 7 | null, "fullName": "…", "profilePhotoUrl": … | null, "verified": true,
  "status": "ACTIVE", "headline": "Software Engineer at Acme",
  "enrollmentNumber": "…" | null,                      // owner/admin only
  "degree": "B.Tech", "department": DepartmentRef, "batch": "2019-2023", "graduationYear": 2023,
  "currentCompany": …, "jobTitle": …, "industry": …, "workLocation": …, "currentLocation": …, "experienceYears": 3,
  "professionalSummary": …, "skills": ["Java","Spring"],
  "email": … | null, "phone": … | null,               // honour emailVisibility / phoneVisibility
  "linkedinUrl": …, "githubUrl": …, "portfolioUrl": …,
  "emailVisibility": "ALUMNI_ONLY" | null, "phoneVisibility": … | null,   // owner/admin only
  "openToMentoring": true, "openToNetworking": true, "availableForHiring": false, "interestedInEvents": true,
  "directoryVisibility": "STUDENTS_AND_ALUMNI",
  "careerHistory": [ { "id", "company", "jobTitle", "startDate", "endDate"|null, "description" } ],
  "education":      [ { "id", "degree", "program", "institutionName", "startYear", "endYear", "cgpa" } ],
  "projects":       [ { "id", "name", "description", "technologies": [..], "projectUrl", "githubUrl", "startDate", "endDate" } ],
  "certifications": [ { "id", "type", "title", "issuer", "issueDate", "credentialUrl" } ],
  "ownProfile": false,
  "completion": ProfileCompletionResponse | null }     // owner/admin only
```
Contact privacy: `PUBLIC` = any signed-in viewer; `ALUMNI_ONLY` = viewers whose role is `ALUMNI` (plus owner/admin); `PRIVATE` = owner/admin only. A hidden value is returned as `null`.

### The alumnus' own profile (role `ALUMNI`)
| Method & path | Body → Response |
|---|---|
| `GET /alumni/me` | → `AlumniResponse` |
| `PATCH /alumni/me` | `AlumniSelfUpdateRequest` (only the fields sent are changed; `skills` replaces the whole list) → `AlumniResponse` |
| `POST /alumni/me/career-history`, `PUT …/{id}`, `DELETE …/{id}` | `{company, jobTitle, startDate, endDate?, description?}` → `CareerHistoryResponse` / `204` |
| `POST /alumni/me/education`, `PUT …/{id}`, `DELETE …/{id}` | `{degree, program?, institutionName, startYear?, endYear?, cgpa?}` → `EducationResponse` / `204` |
| `POST /alumni/me/projects`, `PUT …/{id}`, `DELETE …/{id}` | `{name, description?, technologies?[≤20], projectUrl?, githubUrl?, startDate?, endDate?}` → `ProjectResponse` / `204` |
| `POST /alumni/me/certifications`, `PUT …/{id}`, `DELETE …/{id}` | `{type, title, issuer?, issueDate?, credentialUrl?}` → `CertificationResponse` / `204` |

`AlumniSelfUpdateRequest` (all optional): `phone, currentLocation, currentCompany, jobTitle, industry, workLocation, experienceYears (0–70), skills[≤30], linkedinUrl, githubUrl, portfolioUrl (empty string or full http(s) URL), professionalSummary (≤4000), openToMentoring, openToNetworking, availableForHiring, interestedInEvents, directoryVisibility, emailVisibility, phoneVisibility`.
**Institution-controlled** fields (name, enrollment number, degree, department, batch, graduation year, email, status, verified) are *not* in this request and cannot be changed by the alumnus.

### The student's own profile (role `STUDENT`)
`GET /students/me` → `{ userId, fullName, email, profilePhotoUrl, enrollmentNumber, department: DepartmentRef, batch, skills[], careerInterests, completion }`
`PATCH /students/me` `{ batch?, skills?[≤30], careerInterests?(≤2000), departmentId? }` → same response.

### Profile photo (any role)
`POST /profile/photo` (multipart, field `file`; JPEG/PNG/GIF/WebP by content, ≤5 MB) → `UploadResponse`. `DELETE /profile/photo` → `204`.

### Files
`GET /files/{folder}/{filename}` 🌐 serves `profiles` / `posts` images. `POST /files/posts` (multipart `file`, any signed-in user) → `UploadResponse` — put the URL in a post's `imageUrl`.

## 4. Mentorship

| Method & path | Role | Body → Response |
|---|---|---|
| `POST /mentorship/requests` | STUDENT | `{alumniId, message}` → `MentorshipResponse` |
| `GET /mentorship/requests/sent` | STUDENT | → `MentorshipResponse[]` |
| `GET /mentorship/requests/received` | ALUMNI | → `MentorshipResponse[]` |
| `PATCH /mentorship/requests/{id}` | ALUMNI | `{decision: "ACCEPTED"\|"REJECTED"}` → `MentorshipResponse` |
| `PATCH /mentorship/requests/{id}/complete` | ALUMNI | → `MentorshipResponse` |

```json
{ "id", "message", "status", "requestedAt", "respondedAt"|null,
  "student": { "id": <USER id>, "fullName", "profilePhotoUrl", "department": DepartmentRef|null, "batch" },
  "alumni":  { "id": <ALUMNI id>, "userId": <USER id>|null, "fullName", "profilePhotoUrl", "jobTitle", "currentCompany" } }
```
Use `student.id` / `alumni.userId` with `POST /chat/conversations/direct` to offer a "Message" button.

## 5. Jobs — any signed-in user can post, admins moderate

| Method & path | Who | Body → Response |
|---|---|---|
| `GET /jobs` | any | → `JobPostingResponse[]` — `APPROVED` and not expired, newest first |
| `GET /jobs/{id}` | any | → `JobPostingResponse` (non-approved listings are visible only to the poster and admins; otherwise `404`) |
| `POST /jobs` | any | `{title, company, location?, jobType, description?, expiresAt?}` → `JobPostingResponse` (`PENDING`; an admin's own listing is `APPROVED` at once) |
| `PUT /jobs/{id}` | poster | same body → `JobPostingResponse` (sent back to `PENDING`, except an admin's) |
| `GET /jobs/mine` | any | → `JobPostingResponse[]` (all statuses) |
| `GET /jobs/{id}/applications` | poster | → `JobApplicationResponse[]` (with `applicant` summary) |
| `POST /jobs/{id}/apply` | STUDENT, ALUMNI | → `JobApplicationResponse` (not your own listing; once per job) |
| `GET /jobs/applications/me` | any | → `JobApplicationResponse[]` |

```json
// JobPostingResponse
{ "id", "title", "company", "location", "jobType", "description", "status", "postedAt", "expiresAt"|null,
  "postedBy": UserSummaryResponse, "ownedByViewer": false, "hasAppliedByViewer": false }
// JobApplicationResponse
{ "id", "jobId", "jobTitle", "company", "applicantName", "applicant": UserSummaryResponse | null, "status", "appliedAt" }
```
Admin moderation: `GET /admin/jobs?status=` → `JobPostingResponse[]`; `PATCH /admin/jobs/{id}/status` `{status}` → `JobPostingResponse` (poster is notified; `APPROVED`/`REJECTED` are audit-logged).

## 6. Events

| Method & path | Role | Response |
|---|---|---|
| `GET /events` | any | `EventResponse[]` (published, soonest first) |
| `GET /events/me` | any | `EventResponse[]` (my registrations) |
| `GET /events/{id}` | any | `EventResponse` |
| `POST /events/{id}/register`, `DELETE /events/{id}/register` | any | `EventResponse` |

`EventResponse`: `{ id, title, description, eventDate (LocalDateTime), mode, location, capacity, registeredCount, status, registeredByViewer }`.
Admin: `GET /admin/events` → `EventResponse[]`; `POST /admin/events` and `PUT /admin/events/{id}` with `{title, description?, eventDate (future), mode, location?, capacity (≥1)}`; `PATCH /admin/events/{id}/publish`, `PATCH /admin/events/{id}/cancel` → `EventResponse`.

## 7. Announcements and notifications

* `GET /announcements` (any) → `AnnouncementResponse[]` — only what the viewer's role / department / batch is targeted by (admins see all published).
  `AnnouncementResponse`: `{ id, title, content, targetAudience, targetDepartment: DepartmentRef|null, targetBatch, priority, publishDate, status }`
* Admin: `GET /admin/announcements` (all statuses); `POST /admin/announcements` and `PUT /admin/announcements/{id}` with `{title, content, targetAudience, targetDepartmentId? (required for DEPARTMENT), targetBatch? (required for BATCH), priority, publishDate?}`; `PATCH /admin/announcements/{id}/status?status=PUBLISHED|DRAFT|ARCHIVED` (**query param**, not body).
* `GET /notifications` → `NotificationResponse[]` `{ id, category, message, read, createdAt }` (newest first); `GET /notifications/unread-count` → `{ "count": 3 }`; `PATCH /notifications/{id}/read` → `NotificationResponse`; `PATCH /notifications/read-all` → `200`.
* Notifications are created for: mentorship request/decision (`MENTORSHIP`), event registration (`EVENT`), job application / moderation (`JOB`), new announcement targeted at you (`ANNOUNCEMENT`), verification & account changes (`ACCOUNT`), comments on your posts and admin removals of your posts (`POST`). **Chat messages do not create notifications** — the Messages badge is the unread indicator.

---

## 8. Chat (REST + STOMP over WebSocket)

Every signed-in user — student, alumnus or admin — can chat. Two kinds of conversation share the same tables:

* **`DIRECT`** — one-to-one between any two active users. Find-or-create: the same two people always get the same thread.
* **`DEPARTMENT`** — exactly one group room per department (23 rooms). Members: that department's students and claimed alumni. **Admins are members of every room** (they moderate). Membership is maintained lazily: whenever a user calls `GET /chat/conversations` or `GET /chat/unread-count` the server makes sure their department room (all rooms for an admin) exists and has them as a participant, and removes them from a room of a department they no longer belong to. A newly added participant starts with *no* unread backlog (their read cursor is set to the room's latest message) but can scroll the whole history.

Privacy: direct messages are readable only by the two participants — **no admin endpoint exposes them**. Department rooms are group spaces that admins can read and moderate.

### 8.1 REST

| Method & path | Body / query → Response |
|---|---|
| `GET /chat/conversations` | → `ConversationResponse[]` — the user's department room(s) (always) and every DIRECT thread that has at least one message; ordered by latest activity, newest first |
| `POST /chat/conversations/direct` | `{ userId }` → `ConversationResponse` (find-or-create). `400` for yourself or a deactivated user, `404` unknown user. An empty thread is *not* listed in either person's inbox until someone sends a message |
| `GET /chat/conversations/{id}` | → `ConversationResponse` (`404` if you are not a participant) |
| `GET /chat/conversations/{id}/messages?before=&limit=` | → `CursorPage<MessageResponse>`; `items` are **oldest → newest** within the page; `nextBefore` is the id of the oldest item when older messages exist, else `null` |
| `POST /chat/conversations/{id}/messages` | `{ content, clientId? }` → `MessageResponse` — REST fallback; same code path as the socket (persists, then pushes to all participants) |
| `POST /chat/conversations/{id}/read` | `{ messageId? }` (default: latest) → `{ conversationId, unreadCount, totalUnread }`. The cursor never moves backwards |
| `GET /chat/unread-count` | → `{ "count": 5 }` (sum over all conversations) |
| `GET /chat/users?q=&page=&size=` | → `PageResponse<UserSummaryResponse>` — people search for "new chat". `q` ≥ 2 chars (else `400`), never includes yourself, only active accounts. **Privacy:** alumni whose `directoryVisibility` the viewer may not see (see `AlumniSpecifications.visibleStatesFor`) are excluded; students and admins are always findable |
| `DELETE /admin/chat/messages/{messageId}` | ADMIN only; department rooms only (`400` for a direct message) → `204`; audit `REMOVE_MESSAGE`; pushes a `DELETED` event |

Rules: `content` is trimmed, must be 1–2000 characters. A user can only read or send in conversations they participate in (`404` otherwise). Messages are plain text — the client must render them as text (React does this by default), never as HTML.

```json
// ConversationResponse
{ "id": 12, "type": "DIRECT" | "DEPARTMENT",
  "title": "Priya Sharma" | "Computer Science & Engineering",      // DIRECT: the other person; DEPARTMENT: department name
  "subtitle": "Software Engineer at Acme" | "SVITS · 142 members",
  "avatarUrl": "/files/profiles/….jpg" | null,                      // DIRECT: peer photo; DEPARTMENT: null (UI draws the department code)
  "peer": UserSummaryResponse | null,                               // DIRECT only
  "department": DepartmentRef | null,                               // DEPARTMENT only
  "memberCount": 142 | null,                                        // DEPARTMENT only (participants)
  "lastMessage": MessageResponse | null,
  "unreadCount": 2,
  "updatedAt": "…Z" }                                               // lastMessage.sentAt, else the conversation's createdAt

// MessageResponse
{ "id": 99, "conversationId": 12, "sender": UserSummaryResponse, "content": "hello",
  "sentAt": "…Z",
  "clientId": "b1e5…" | null }     // echoed only to the sender's own copy, so an optimistic bubble can be replaced
```

### 8.2 Real-time (STOMP over a plain WebSocket)

* **Endpoint:** `ws://localhost:8080/api/ws` (use `wss://` behind TLS). Native WebSocket — **no SockJS**. The frontend derives it from `VITE_API_BASE_URL` (`http→ws`, `https→wss`, append `/ws`). The handshake only accepts the origins in `app.cors.allowed-origin`.
* **Authentication:** the STOMP `CONNECT` frame must carry the header `Authorization: Bearer <jwt>`. A missing/invalid/expired token or a deactivated account gets an `ERROR` frame and the socket is closed. The principal's name is the **user id as a string**. Every other frame requires a prior successful `CONNECT`.
* **Subscribe** (and nothing else — any other destination is rejected):
  * `/user/queue/messages` — all chat events for this user (below)
  * `/user/queue/errors` — rejected sends
* **Send:**
  * `/app/chat.send` body `{ "conversationId": 12, "content": "hi", "clientId": "uuid" }`
  * `/app/chat.read` body `{ "conversationId": 12, "messageId": 99 }` (`messageId` optional = latest)
* **Heartbeat:** 10 s both ways. The client reconnects with back-off (1 s → 30 s) and, once reconnected, re-fetches `GET /chat/conversations` and the open thread's newest page to close any gap.
* **Server → client events on `/user/queue/messages`:**
```json
{ "type": "MESSAGE", "conversationId": 12, "message": MessageResponse }   // delivered to every participant, including the sender's other tabs
{ "type": "READ",    "conversationId": 12 }                               // you read it on another tab/device → set that thread's unread to 0
{ "type": "DELETED", "conversationId": 12, "messageId": 99 }              // an admin removed a room message
```
  The sender's own copy of a `MESSAGE` carries `message.clientId`; everyone else's has `clientId: null`.
* **Unread bookkeeping on the client:** on a `MESSAGE` from someone else, if that thread is not open (or the tab is hidden) add 1 to its `unreadCount` and to the global badge; if it is open and visible, call `/app/chat.read` (or the REST equivalent). A `MESSAGE` for an unknown `conversationId` → `GET /chat/conversations/{id}` and insert it at the top.
* **Errors:** a rejected `/app/chat.send` produces `{ "status": 400, "message": "Message can't be empty.", "clientId": "…" }` on `/user/queue/errors` so the optimistic bubble can be marked failed.
* **Security notes:** the token's expiry is remembered at `CONNECT`; once it has passed, the next frame from that socket is answered with an `ERROR` frame and the socket is closed (the client then reconnects — which fails with `401`-style `ERROR` until the user signs in again). A user who is deactivated *after* connecting cannot send any more (`ChatService` re-checks that the sender is active on every send), although an already-open socket may keep *receiving* pushes until it closes — a documented limitation. Direct-message and room permissions are enforced per frame by `ChatService` — knowing a conversation id is not enough.

---

## 9. Social feed

Every signed-in user (including admins) can publish posts; there is one open feed filtered by visibility.

### 9.1 Who can see a post
| `visibility` | Visible to |
|---|---|
| `PUBLIC` | any signed-in user |
| `ALUMNI_ONLY` | alumni and admins |
| `DEPARTMENT` | people in the **author's** department (author included). Rejected with `400` if the author has no department (admins) |
| `CONNECTIONS` | people the author has a *mutual* direct conversation with (each side has sent at least one message) |

The author always sees their own posts. In `/feed` an admin sees `PUBLIC`, `ALUMNI_ONLY` and their own posts; `/admin/posts` lists everything for moderation. A post you may not see is `404`. Posts written by a deactivated account are not shown in `/feed` and answer `404` on `GET /posts/{id}` (moderators still see them under `/admin/posts`); their comments stay until a moderator removes them.

### 9.2 Endpoints
| Method & path | Body / query → Response |
|---|---|
| `GET /feed?before=&limit=&departmentId=&authorId=` | → `CursorPage<PostResponse>`, newest first, visibility-filtered. `departmentId` = only posts by members of that department (the group feed); `authorId` = only that user's posts (profile "Posts" tab). `nextBefore` is the id of the oldest post *scanned*, so paging never loops even when many posts are filtered out |
| `POST /posts` | `{ content (1–5000), imageUrl?, visibility }` → `PostResponse`. `imageUrl` must be a URL returned by `POST /files/posts` (`/files/posts/<uuid>.<jpg\|png\|gif\|webp>`) and the file must exist |
| `GET /posts/{id}` | → `PostResponse` |
| `DELETE /posts/{id}` | author only → `204` (the image file is deleted after commit) |
| `POST /posts/{id}/like` / `DELETE /posts/{id}/like` | → `{ "liked": true, "likeCount": 5 }` (idempotent) |
| `GET /posts/{id}/comments` | → `CommentResponse[]` oldest first (max 200) |
| `POST /posts/{id}/comments` | `{ content (1–2000) }` → `CommentResponse`; notifies the post's author (category `POST`) unless they wrote it |
| `DELETE /posts/{postId}/comments/{commentId}` | comment author or post author → `204` |

```json
// PostResponse
{ "id": 5, "author": UserSummaryResponse, "content": "…", "imageUrl": "/files/posts/….jpg" | null,
  "visibility": "PUBLIC", "createdAt": "…Z",
  "likeCount": 4, "commentCount": 2, "likedByViewer": true, "ownedByViewer": false }
// CommentResponse
{ "id": 11, "postId": 5, "author": UserSummaryResponse, "content": "…", "createdAt": "…Z", "ownedByViewer": false }
```
Posts and comments are plain text; the client must not render them as HTML. Images that were uploaded but never attached to a post stay on disk (documented limitation — a periodic clean-up job would remove them).

## 10. Groups (department-based)

One group per department, organised Institute → Department. A group is: a **member list**, a **feed** (`GET /feed?departmentId=`) and a **chat room** (the `DEPARTMENT` conversation).

| Method & path | Response |
|---|---|
| `GET /groups` | `InstituteGroups[]` = `[{ "institute": { "id", "name", "code" }, "groups": [GroupSummary] }]` ordered by institute name, then department name |
| `GET /groups/{departmentId}` | `GroupDetailResponse` — `GroupSummary` fields plus `instituteName` and `conversationId` (the room id, present when the viewer is a member or an admin, else `null`). `404` for an unknown department |
| `GET /groups/{departmentId}/members?query=&page=&size=` | `PageResponse<UserSummaryResponse>` — the department's students and claimed alumni, sorted by name; alumni hidden by their directory visibility are left out for viewers who may not see them |

```json
// GroupSummary
{ "departmentId": 1, "name": "Computer Science & Engineering", "code": "CSE",
  "instituteId": 1, "instituteCode": "SVITS",
  "memberCount": 142, "studentCount": 96, "alumniCount": 46,
  "member": true }          // the viewer's own department (or any, for an admin)
```
`studentCount` counts active student accounts of the department; `alumniCount` counts active alumni accounts (claimed records) of it.

---

## 11. Administration (role `ADMIN`; every `/admin/**` route)

| Area | Endpoints |
|---|---|
| Alumni records | `GET /admin/alumni?query=&departmentId=&status=&page=&size=` → `PageResponse<AlumniAdminRowResponse>`; `POST /admin/alumni` / `PUT /admin/alumni/{id}` `{fullName, email, enrollmentNumber, degree?, departmentId, batch?, graduationYear}`; `PATCH /admin/alumni/{id}/status` `{isActive}`; `PATCH /admin/alumni/{id}/verify` `{verified}`; `POST /admin/alumni/{id}/invite`; `GET /admin/alumni/export?departmentId=&status=` → `text/csv` |
| Bulk import | `POST /admin/alumni/import/upload` (multipart `file`) → `{importId, detectedColumns[], totalRows}`; `POST …/{importId}/mapping` `{mapping: {csvColumn: systemField}}`; `GET …/{importId}/preview` → `{totalRows, readyCount, duplicateCount, errorCount, sampleRows: [{rowIndex, values{}, flag: READY\|DUPLICATE\|ERROR, errorMessage}]}`; `POST …/{importId}/confirm` → `ImportBatchSummaryResponse`; `GET …/history` → `ImportBatchSummaryResponse[]` |
| Jobs / events / announcements | see §5, §6, §7 |
| Feed moderation | `GET /admin/posts?query=&page=&size=` → `PageResponse<PostResponse>` (everything, any visibility, any author state; newest first; `query` matches the content or the author's name); `GET /admin/posts/{id}/comments` → `CommentResponse[]` (oldest first, max 200; works for every post, which `GET /posts/{id}/comments` does not for an admin and a department-only post); `DELETE /admin/posts/{id}` → `204` (audit `REMOVE_POST`, author notified); `DELETE /admin/posts/{postId}/comments/{commentId}` → `204` (audit `REMOVE_POST`, comment author notified) |
| Users | `GET /admin/users?query=&role=&active=&page=&size=` → `PageResponse<AdminUserRowResponse>`; `PATCH /admin/users/{id}/status` `{isActive}` → `AdminUserRowResponse` (audit `DEACTIVATE_USER`/`REACTIVATE_USER`; you cannot deactivate yourself). An inactive account cannot sign in (`403`), is hidden from chat user search and group member lists, and — for an alumnus — the linked alumni record becomes `INACTIVE` (reactivating makes an `INACTIVE` record `ACTIVE` again). The reverse also holds: `PATCH /admin/alumni/{id}/status` switches the linked account's sign-in on or off |
| Chat moderation | `DELETE /admin/chat/messages/{messageId}` (§8) |
| Dashboard & analytics | `GET /admin/dashboard/summary` → `{totalAlumni, activeAlumni, claimedProfiles, unclaimedProfiles, upcomingEvents, activeMentorships, publishedJobs, pendingJobApprovals, byDepartment: LabelCount[], byGraduationYear: LabelCount[]}`; `GET /admin/analytics/alumni-by-{department\|institute\|batch\|industry\|location}` → `LabelCount[]` = `[{label, value}]` |
| Audit log | `GET /admin/audit-log?action=` → `[{ id, adminEmail, action, details, timestamp }]` (newest first, read-only) |

```json
// AlumniAdminRowResponse
{ "id", "userId"|null, "fullName", "email", "enrollmentNumber", "degree", "department": DepartmentRef, "batch",
  "graduationYear", "status", "verified", "currentCompany", "jobTitle", "invitedAt"|null, "claimedAt"|null }
// AdminUserRowResponse
{ "id", "fullName", "email", "role", "active": true, "profilePhotoUrl"|null, "department": DepartmentRef|null,
  "alumniId"|null, "createdAt" }
// Import system fields (mapping targets)
fullName*, email*, enrollmentNumber*, department*, graduationYear*, institute, degree, batch, phone, company, jobTitle, industry, location, ignore    (* required)
```
Import notes: `department` accepts the department's name or code; add the optional `institute` column to disambiguate a name shared by two institutes.

## 12. Role matrix (summary)

| Capability | Student | Alumnus | Admin |
|---|:--:|:--:|:--:|
| Browse directory / profiles (privacy applies) | ✔ | ✔ | ✔ (all) |
| Edit own profile, photo | ✔ | ✔ | photo only |
| Mentorship: request / respond | request | respond | – |
| Post / browse / apply to jobs | ✔ (moderated) | ✔ (moderated) | post (auto-approved), moderate |
| Events: register | ✔ | ✔ | create/publish/cancel |
| Chat (DM + department room) | ✔ | ✔ | ✔ (all rooms; delete room messages) |
| Feed: post / like / comment | ✔ | ✔ | ✔ (+ remove any post/comment) |
| Groups | browse all, join own | browse all, join own | browse all, join all |
| Import / records / analytics / audit / user management | – | – | ✔ |
