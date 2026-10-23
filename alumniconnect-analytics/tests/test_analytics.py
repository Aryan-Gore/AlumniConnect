from datetime import date

from fastapi.testclient import TestClient

from app.cleaning import standardize_company_name
from app.main import app

client = TestClient(app)


def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_company_standardization_exact_and_alias_matches():
    # The exact example from the SRS: TCS / tcs / T.C.S. / Tata Consultancy Services
    variants = ["TCS", "tcs", "T.C.S.", "Tata Consultancy Services"]
    for variant in variants:
        standardized, matched = standardize_company_name(variant)
        assert standardized == "Tata Consultancy Services", f"{variant!r} -> {standardized!r}"
        assert matched is True


def test_company_standardization_fuzzy_match():
    standardized, matched = standardize_company_name("Accentur")  # dropped trailing letter
    assert standardized == "Accenture"
    assert matched is True


def test_company_standardization_unknown_company_falls_back_to_title_case():
    standardized, matched = standardize_company_name("some random startup inc")
    assert standardized == "Some Random Startup Inc"
    assert matched is False


def test_clean_companies_endpoint():
    response = client.post("/internal/clean/companies", json={
        "company_names": ["TCS", "tcs", "Infosys Ltd", "a brand new startup"]
    })
    assert response.status_code == 200
    results = response.json()["results"]
    assert results[0]["standardized"] == "Tata Consultancy Services"
    assert results[1]["standardized"] == "Tata Consultancy Services"
    assert results[2]["standardized"] == "Infosys"
    assert results[3]["matched_alias"] is False


def test_career_analytics_endpoint():
    records = [
        {"id": 1, "department": "Computer Science", "graduation_year": 2022, "current_company": "Google",
         "job_title": "SWE", "industry": "Technology", "current_location": "Bengaluru", "experience_years": 3, "status": "ACTIVE"},
        {"id": 2, "department": "Computer Science", "graduation_year": 2022, "current_company": "Razorpay",
         "job_title": "Backend Engineer", "industry": "Fintech", "current_location": "Bengaluru", "experience_years": 2, "status": "ACTIVE"},
        {"id": 3, "department": "Mechanical", "graduation_year": 2021, "current_company": "Tata Motors",
         "job_title": "Design Engineer", "industry": "Automotive", "current_location": "Pune", "experience_years": 4, "status": "ACTIVE"},
    ]
    response = client.post("/internal/analytics/career", json=records)
    assert response.status_code == 200
    body = response.json()

    assert body["industry_distribution"]["Technology"] == 1
    assert body["industry_distribution"]["Fintech"] == 1

    year_2022 = next(t for t in body["graduation_year_trends"] if t["graduation_year"] == 2022)
    assert year_2022["total_alumni"] == 2
    assert year_2022["average_experience_years"] == 2.5


def test_engagement_trend_endpoint():
    events = [
        {"event_type": "mentorship", "occurred_on": str(date(2026, 7, 1))},
        {"event_type": "mentorship", "occurred_on": str(date(2026, 7, 15))},
        {"event_type": "event_registration", "occurred_on": str(date(2026, 7, 20))},
        {"event_type": "job_application", "occurred_on": str(date(2026, 8, 5))},
    ]
    response = client.post("/internal/analytics/engagement", json=events)
    assert response.status_code == 200
    points = response.json()["points"]

    july = next(p for p in points if p["month"] == "2026-07")
    assert july["mentorship"] == 2
    assert july["event_registration"] == 1
    assert july["total"] == 3

    august = next(p for p in points if p["month"] == "2026-08")
    assert august["job_application"] == 1
    assert august["total"] == 1
