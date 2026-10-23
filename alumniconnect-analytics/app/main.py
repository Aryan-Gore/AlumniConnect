"""
AlumniConnect analytics service.

Internal only: per the architecture note resolved during the SRS review,
the frontend talks to Spring Boot exclusively. Spring Boot calls this
service (see ANALYTICS_SERVICE_URL in the backend's application.yml) and
relays JSON back to React. This service never touches MySQL directly — it
receives data Spring Boot already queried, computes on it, and returns
results. That keeps exactly one component owning the database.
"""
from fastapi import FastAPI

from app.analytics import career_analytics, engagement_trend
from app.cleaning import standardize_company_names
from app.schemas import (
    AlumniRecordInput,
    CareerAnalyticsResponse,
    CompanyCleanRequest,
    CompanyCleanResponse,
    CompanyCleanResult,
    EngagementEventInput,
    EngagementTrendResponse,
)

app = FastAPI(
    title="AlumniConnect Analytics Service",
    description="Internal data-cleaning and statistics service, called by the Spring Boot backend.",
    version="0.1.0",
)


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.post("/internal/clean/companies", response_model=CompanyCleanResponse)
def clean_companies(request: CompanyCleanRequest) -> CompanyCleanResponse:
    df = standardize_company_names(request.company_names)
    return CompanyCleanResponse(
        results=[
            CompanyCleanResult(original=row.original, standardized=row.standardized, matched_alias=row.matched_alias)
            for row in df.itertuples()
        ]
    )


@app.post("/internal/analytics/career", response_model=CareerAnalyticsResponse)
def career(records: list[AlumniRecordInput]) -> CareerAnalyticsResponse:
    result = career_analytics([r.model_dump(by_alias=False) for r in records])
    return CareerAnalyticsResponse(**result)


@app.post("/internal/analytics/engagement", response_model=EngagementTrendResponse)
def engagement(events: list[EngagementEventInput]) -> EngagementTrendResponse:
    points = engagement_trend([e.model_dump() for e in events])
    return EngagementTrendResponse(points=points)
