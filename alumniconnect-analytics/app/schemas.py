from datetime import date
from typing import Optional

from pydantic import BaseModel, Field


class CompanyCleanRequest(BaseModel):
    company_names: list[str] = Field(..., description="Raw company name strings, as typed by admins or imported from CSV.")


class CompanyCleanResult(BaseModel):
    original: str
    standardized: str
    matched_alias: bool


class CompanyCleanResponse(BaseModel):
    results: list[CompanyCleanResult]


class AlumniRecordInput(BaseModel):
    """
    One alumni record, shaped to match the fields Spring Boot's
    AlumniResponse DTO exposes. The analytics endpoints take a batch of
    these rather than touching MySQL directly — Spring Boot owns the
    database; this service is stateless and only ever sees what it's sent.
    """
    id: int
    department: str
    graduation_year: int
    current_company: Optional[str] = None
    job_title: Optional[str] = None
    industry: Optional[str] = None
    current_location: Optional[str] = None
    experience_years: Optional[int] = None
    status: str


class DepartmentIndustryBreakdown(BaseModel):
    department: str
    industry: str
    count: int


class GraduationYearTrend(BaseModel):
    graduation_year: int
    total_alumni: int
    average_experience_years: Optional[float] = None
    top_industry: Optional[str] = None


class CareerAnalyticsResponse(BaseModel):
    department_industry_breakdown: list[DepartmentIndustryBreakdown]
    graduation_year_trends: list[GraduationYearTrend]
    industry_distribution: dict[str, int]


class EngagementEventInput(BaseModel):
    """One dated engagement event — a mentorship request, event registration,
    or job application — used to compute the monthly trend line."""
    event_type: str  # "mentorship" | "event_registration" | "job_application"
    occurred_on: date


class EngagementMonthPoint(BaseModel):
    month: str  # "2026-07"
    mentorship: int
    event_registration: int
    job_application: int
    total: int


class EngagementTrendResponse(BaseModel):
    points: list[EngagementMonthPoint]
