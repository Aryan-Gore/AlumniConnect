package com.alumniconnect.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Additional education the alumnus reports themselves (e.g. a Master's
 * completed elsewhere after graduating). The degree this institution
 * certifies lives on {@link AlumniRecord} and stays institution-controlled.
 */
@Entity
@Table(name = "education_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumni_id", nullable = false)
    private AlumniRecord alumniRecord;

    @Column(nullable = false, length = 100)
    private String degree;

    @Column(length = 150)
    private String program;

    @Column(name = "institution_name", nullable = false, length = 200)
    private String institutionName;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @Column(precision = 4, scale = 2)
    private BigDecimal cgpa;
}
