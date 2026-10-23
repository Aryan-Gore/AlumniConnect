"""
Data cleaning utilities — the piece of the SRS's "Python Data Processing"
section that Spring Boot doesn't attempt to duplicate. The CSV import
workflow itself (upload/mapping/preview/duplicate-detection against the
database) lives in the Java backend, since it needs live access to the
alumni table; this module handles the part that's pure data
transformation and doesn't need the database at all — exactly the kind of
"useful analytics rather than unnecessary processing" the SRS asks for.
"""
import difflib
import re

import pandas as pd

# A starter alias table. Real deployments would grow this from actual
# import data rather than hardcoding it — this demonstrates the mechanism
# with the exact example the SRS gives.
_COMPANY_ALIASES: dict[str, str] = {
    "tcs": "Tata Consultancy Services",
    "t.c.s.": "Tata Consultancy Services",
    "tata consultancy services": "Tata Consultancy Services",
    "tata consultancy services ltd": "Tata Consultancy Services",
    "infosys": "Infosys",
    "infosys ltd": "Infosys",
    "infosys limited": "Infosys",
    "wipro": "Wipro",
    "wipro ltd": "Wipro",
    "wipro limited": "Wipro",
    "google": "Google",
    "google inc": "Google",
    "google llc": "Google",
    "alphabet": "Google",
    "microsoft": "Microsoft",
    "msft": "Microsoft",
    "amazon": "Amazon",
    "amazon.com": "Amazon",
    "aws": "Amazon",
    "accenture": "Accenture",
    "cognizant": "Cognizant",
    "cts": "Cognizant",
    "hcl": "HCLTech",
    "hcl technologies": "HCLTech",
    "hcltech": "HCLTech",
    "deloitte": "Deloitte",
    "razorpay": "Razorpay",
}


def _normalize_for_lookup(raw: str) -> str:
    """Lowercase, strip periods/commas, collapse whitespace — turns
    'T.C.S.', 'tcs', and 'TCS ' into the same lookup key."""
    cleaned = re.sub(r"[.,]", "", raw.strip().lower())
    cleaned = re.sub(r"\s+", " ", cleaned)
    return cleaned


def _title_case_fallback(raw: str) -> str:
    """For names with no known alias: trim, collapse whitespace, title-case
    — better than leaving inconsistent capitalization untouched, without
    inventing a canonical name we can't actually verify."""
    cleaned = re.sub(r"\s+", " ", raw.strip())
    return cleaned.title() if cleaned else cleaned


def standardize_company_name(raw: str, fuzzy_cutoff: float = 0.87) -> tuple[str, bool]:
    """Returns (standardized_name, matched_alias). matched_alias is True
    only when we're confident — an exact or near-exact match against the
    known alias table — not for the title-case fallback."""
    if not raw or not raw.strip():
        return raw, False

    key = _normalize_for_lookup(raw)
    if key in _COMPANY_ALIASES:
        return _COMPANY_ALIASES[key], True

    close = difflib.get_close_matches(key, _COMPANY_ALIASES.keys(), n=1, cutoff=fuzzy_cutoff)
    if close:
        return _COMPANY_ALIASES[close[0]], True

    return _title_case_fallback(raw), False


def standardize_company_names(raw_names: list[str]) -> pd.DataFrame:
    """Batch version — the one actually used by the API endpoint. Returns a
    DataFrame so the same function is directly reusable from a notebook or
    a script during data exploration, not just from FastAPI."""
    df = pd.DataFrame({"original": raw_names})
    results = df["original"].apply(standardize_company_name)
    df["standardized"] = results.apply(lambda r: r[0])
    df["matched_alias"] = results.apply(lambda r: r[1])
    return df
