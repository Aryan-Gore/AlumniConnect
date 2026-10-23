package com.alumniconnect.backend.entity;

/** One table covers all four — the SRS examples (AWS cert, Employee of the Year, a paper, a hackathon win) differ only by label. */
public enum CertificationType {
    CERTIFICATION,
    ACHIEVEMENT,
    AWARD,
    PUBLICATION
}
