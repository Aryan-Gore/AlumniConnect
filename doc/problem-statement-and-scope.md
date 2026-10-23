# Alumni Connect — Problem Statement & Scope

**Project:** Digital Platform for Centralized Alumni Data Management and Engagement
**Author:** Aryan
**Institution:** Shree Vaishnav Vidyapeeth Vishwavidyalay, B.Tech CSE (Final Year)
**Document version:** 1.0
**Status:** Living document — revisit at the end of each sprint and update if scope shifts

---

## 1. Problem Statement

Most educational institutions do not have a reliable centralized system to manage alumni data.
Once a student graduates, the institution effectively loses its single reliable channel to that
person. Contact information, academic records, and career updates are scattered across
admission forms, placement cell spreadsheets, personal LinkedIn profiles, and informal
WhatsApp groups — with no reconciliation between them.

This creates four concrete, recurring failures:

1. **Data fragmentation** — alumni records exist in multiple disconnected places with no
   single source of truth.
2. **Data decay** — phone numbers, emails, and employers change and are never updated,
   so institutional outreach becomes less effective every year.
3. **No verification layer** — anyone can claim to be an alumnus in an open WhatsApp
   group or public forum; there is no way to confirm identity or graduation status.
4. **No engagement infrastructure** — there is no structured way to run mentorship
   programs, post jobs, organize reunions, or measure alumni engagement over time.

**Alumni Connect** solves this by giving the institution one centralized, verified,
searchable platform for alumni data and engagement — replacing ad-hoc spreadsheets and
group chats with a real system of record.

---

## 2. Objectives

- Provide a single, verified, centralized database of alumni records.
- Let alumni self-serve profile updates instead of relying on manual data collection.
- Give the institution a searchable directory to reconnect with alumni by batch, branch,
  company, or location.
- Create structured channels for events, job postings, and mentorship — replacing informal
  WhatsApp coordination.
- Give administrators visibility into engagement through basic analytics.

---

## 3. Scope

### 3.1 In Scope (v1)

| Area | What's included |
|---|---|
| Accounts | Student & Alumnus registration, admin-verified onboarding, role-based login (Student / Alumnus / Admin / Super Admin) |
| Directory | Centralized, searchable alumni directory with filters (batch, branch, company, location) |
| Profiles | Career/profile update workflow, resume & photo uploads |
| Events | Event creation, RSVP, attendance tracking |
| Jobs | Job/internship postings board, moderated by admins |
| Notifications | Email on approval, event reminders, job digests |
| Admin tools | Verification queue, analytics dashboard, audit logging |
| Mentorship | Basic opt-in — alumni mark themselves available, students can request |

### 3.2 Out of Scope (v1 — explicitly deferred, not forgotten)

| Item | Why it's deferred | Revisit as |
|---|---|---|
| Native mobile apps | A responsive web app covers the need for v1; native apps add build/release overhead without a proven demand yet | Roadmap v2.1 (PWA) |
| Payment gateway / donations | Adds compliance and security surface disproportionate to a first release | Roadmap v2.0 |
| AI-based mentor-mentee matching | Basic opt-in matching is sufficient for v1; algorithmic matching needs real usage data first | Roadmap v1.2 |
| Multi-institution / multi-tenant support | Single-institution deployment is the actual requirement right now | Roadmap v3.0 |
| Real-time chat between alumni | Email/notification-based engagement is sufficient; chat adds infrastructure complexity (websockets, moderation) with unclear payoff | Not currently planned |

Keeping this table honest matters — it's the difference between "we ran out of time" and
"we made a deliberate call." Both the report and the viva benefit from the second framing.

---

## 4. Stakeholders & Roles

| Role | Description |
|---|---|
| **Super Admin** | Institution's IT/placement cell owner. Manages admin accounts, global settings, data exports. |
| **Admin** (Alumni Cell / Faculty Coordinator) | Verifies new alumni sign-ups, manages events and job postings, moderates content, views analytics. |
| **Alumnus** | Maintains own profile, browses the directory, applies to postings, RSVPs to events, opts into mentorship. |
| **Student** | Restricted read access to directory and job board; can request mentorship connections. |
| **Guest** | Sees only public landing content — no directory access. |

---

## 5. Success Criteria (how we'll know v1 is "done")

- [ ] An institution can onboard alumni from an existing spreadsheet or fresh registration.
- [ ] A verified alumnus can update their own profile without admin involvement.
- [ ] An admin can approve/reject a new sign-up in under 30 seconds using the queue.
- [ ] The directory search returns filtered results in under 500ms for a realistic dataset
      (~5,000–10,000 records).
- [ ] At least one full event lifecycle (create → RSVP → attendance export) works end-to-end.
- [ ] The system is deployed, publicly reachable over HTTPS, and passes a basic security
      review (role boundaries enforced server-side, no leaked secrets).

---

## 6. Assumptions & Constraints

- Single institution, single deployment — no multi-tenancy required for v1.
- Admin accounts are provisioned manually by the institution, not via public self-registration.
- Development is solo, part-time, alongside coursework — scope is sized accordingly
  (see the 12-week sprint plan for the corresponding time budget).
- Cloud hosting will use free/low-cost tiers suitable for an academic deployment, not
  enterprise-scale infrastructure.

---

## 7. Change Log

| Date | Change | Reason |
|---|---|---|
| — | Initial version | Baseline scope before Sprint 1 |

*(Add a row here any time scope changes mid-project — this table is what turns scope creep
into a documented, defensible decision instead of an unexplained gap in your final report.)*
