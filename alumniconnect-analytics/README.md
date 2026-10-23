# AlumniConnect — Analytics Service

Internal Python/FastAPI service for the two things the SRS asks Python to
do that Spring Boot doesn't attempt to duplicate: data-cleaning transforms
(company-name standardization) and statistical analysis (career-growth and
engagement trends), via Pandas.

**This service is never called by the frontend.** Spring Boot is the only
thing that talks to it — see `ANALYTICS_SERVICE_URL` in the backend's
`application.yml`. It doesn't touch MySQL either: every endpoint takes the
data it needs as a request body (already queried by Spring Boot) and
returns a computed result. One component owns the database; this one just
computes on what it's handed.

## Setup

```bash
python3 -m venv venv
source venv/bin/activate       # venv\Scripts\activate on Windows
pip install -r requirements.txt

uvicorn app.main:app --reload --port 8000
```

Interactive docs at `http://localhost:8000/docs` once it's running.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/health` | Liveness check |
| POST | `/internal/clean/companies` | Standardizes a batch of raw company-name strings |
| POST | `/internal/analytics/career` | Department×industry breakdown, per-graduation-year trends |
| POST | `/internal/analytics/engagement` | Monthly mentorship/event/job-application counts |

## What's actually verified

Unlike the Spring Boot backend (this sandbox has no network path to Maven
Central, so that code is written but not compiled here), this service's
dependencies come from PyPI, which *is* reachable — so this part is for
real:

- `pip install -r requirements.txt` — actually run, actually resolved
- `pytest tests/` — 7/7 passing, including the exact standardization
  example from the SRS itself (TCS / tcs / T.C.S. / Tata Consultancy
  Services → all resolve to the same canonical name)
- The server was actually started with `uvicorn` and hit over real HTTP
  (not just FastAPI's in-process `TestClient`) to confirm it boots and
  serves correctly

One real bug surfaced this way, the same kind of thing the MySQL schema
check caught: an initial fuzzy-match test used "Wipr0" (a zero for the
letter O) expecting it to resolve to "Wipro" — it doesn't, because that's
only 80% similar at the character level, below the 0.87 cutoff chosen
specifically to avoid false-positive merges (a wrong auto-merge silently
corrupts alumni company data, which is worse than missing an occasional
typo). The cutoff was correct; the test's example wasn't a fair one, so it
was replaced with a genuine dropped-letter typo ("Accentur" → "Accenture").

## Extending the alias table

`app/cleaning.py` has a starter `_COMPANY_ALIASES` dict covering the
example from the SRS plus a handful of common employers. A real deployment
would grow this from actual import data rather than hardcoding entries —
worth revisiting once the CSV import has run against real alumni records
and you can see what variants actually show up.

## Deliberately not here

No machine learning, per the SRS's explicit instruction ("do not add
machine learning or deep learning unless it is genuinely needed"). Every
function in `app/analytics.py` is a Pandas groupby/aggregate — nothing
here needs a model.
