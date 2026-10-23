"""
Statistical analysis — the "Alumni analytics" and "Engagement analytics"
items from the SRS's Python responsibilities list. Deliberately no
ML/DL: every function here is a groupby/aggregate, matching the SRS's
explicit "do not add machine learning unless genuinely needed."
"""
import pandas as pd


def career_analytics(records: list[dict]) -> dict:
    if not records:
        return {"department_industry_breakdown": [], "graduation_year_trends": [], "industry_distribution": {}}

    df = pd.DataFrame(records)

    dept_industry = (
        df.dropna(subset=["industry"])
        .groupby(["department", "industry"])
        .size()
        .reset_index(name="count")
        .sort_values("count", ascending=False)
    )
    breakdown = dept_industry.to_dict(orient="records")

    def top_industry(group: pd.DataFrame):
        modes = group["industry"].mode()
        return modes.iloc[0] if not modes.empty else None

    trends = []
    for year, group in df.groupby("graduation_year"):
        trends.append({
            "graduation_year": int(year),
            "total_alumni": int(len(group)),
            "average_experience_years": (
                round(float(group["experience_years"].dropna().mean()), 1)
                if group["experience_years"].notna().any() else None
            ),
            "top_industry": top_industry(group),
        })
    trends.sort(key=lambda t: t["graduation_year"])

    industry_distribution = df["industry"].dropna().value_counts().to_dict()

    return {
        "department_industry_breakdown": breakdown,
        "graduation_year_trends": trends,
        "industry_distribution": {str(k): int(v) for k, v in industry_distribution.items()},
    }


def engagement_trend(events: list[dict]) -> list[dict]:
    """events: [{"event_type": "mentorship" | "event_registration" | "job_application", "occurred_on": date}, ...]"""
    if not events:
        return []

    df = pd.DataFrame(events)
    df["occurred_on"] = pd.to_datetime(df["occurred_on"])
    df["month"] = df["occurred_on"].dt.to_period("M").astype(str)

    pivot = (
        df.groupby(["month", "event_type"])
        .size()
        .unstack(fill_value=0)
        .reindex(columns=["mentorship", "event_registration", "job_application"], fill_value=0)
        .sort_index()
    )
    pivot["total"] = pivot.sum(axis=1)

    return [
        {
            "month": month,
            "mentorship": int(row["mentorship"]),
            "event_registration": int(row["event_registration"]),
            "job_application": int(row["job_application"]),
            "total": int(row["total"]),
        }
        for month, row in pivot.iterrows()
    ]
