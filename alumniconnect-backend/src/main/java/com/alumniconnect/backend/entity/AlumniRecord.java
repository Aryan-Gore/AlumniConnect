package com.alumniconnect.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The permanent alumni record. May exist with {@code user} == null (freshly
 * imported or invited) — see database/ER_DIAGRAM.md for why this table is
 * deliberately not a subtype of {@link User}. Institution-controlled fields
 * are only ever written by admin-facing service methods; alumni-controlled
 * fields are only ever written by the self-service ones. That split is
 * enforced in {@code AlumniService}, not here — this entity just holds both.
 *
 * The profile photo is not stored here: it lives on {@code users} so every
 * role shares one avatar mechanism (see {@link User#getProfilePhotoUrl()}).
 */
@Entity
@Table(name = "alumni_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // ---- Institution-controlled ----
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(name = "enrollment_number", nullable = false, unique = true, length = 30)
    private String enrollmentNumber;

    @Column(length = 100)
    private String degree;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(length = 20)
    private String batch;

    @Column(name = "graduation_year", nullable = false)
    private Integer graduationYear;

    /** Admin-set only — the "verified alumnus" badge. */
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    // ---- Alumni-controlled ----
    @Column(length = 20)
    private String phone;

    @Column(name = "current_location", length = 150)
    private String currentLocation;

    @Column(name = "current_company", length = 150)
    private String currentCompany;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Column(length = 100)
    private String industry;

    @Column(name = "work_location", length = 150)
    private String workLocation;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "professional_summary", columnDefinition = "TEXT")
    private String professionalSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_visibility", nullable = false, length = 20)
    @Builder.Default
    private ContactVisibility emailVisibility = ContactVisibility.ALUMNI_ONLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "phone_visibility", nullable = false, length = 20)
    @Builder.Default
    private ContactVisibility phoneVisibility = ContactVisibility.PRIVATE;

    // ---- Participation flags ----
    @Column(name = "open_to_mentoring", nullable = false)
    @Builder.Default
    private boolean openToMentoring = false;

    @Column(name = "open_to_networking", nullable = false)
    @Builder.Default
    private boolean openToNetworking = false;

    @Column(name = "available_for_hiring", nullable = false)
    @Builder.Default
    private boolean availableForHiring = false;

    @Column(name = "interested_in_events", nullable = false)
    @Builder.Default
    private boolean interestedInEvents = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "directory_visibility", nullable = false, length = 30)
    @Builder.Default
    private DirectoryVisibility directoryVisibility = DirectoryVisibility.STUDENTS_AND_ALUMNI;

    // ---- Lifecycle ----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AlumniStatus status = AlumniStatus.IMPORTED;

    @Column(name = "imported_at")
    private Instant importedAt;

    @Column(name = "invited_at")
    private Instant invitedAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ---- Profile sections ----
    @ManyToMany
    @JoinTable(
            name = "alumni_skills",
            joinColumns = @JoinColumn(name = "alumni_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    @OrderBy("name ASC")
    @Builder.Default
    private Set<Skill> skills = new LinkedHashSet<>();

    @OneToMany(mappedBy = "alumniRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate DESC")
    @Builder.Default
    private List<CareerHistoryEntry> careerHistory = new ArrayList<>();

    @OneToMany(mappedBy = "alumniRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("endYear DESC, startYear DESC")
    @Builder.Default
    private List<EducationRecord> educationRecords = new ArrayList<>();

    @OneToMany(mappedBy = "alumniRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate DESC")
    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "alumniRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("issueDate DESC")
    @Builder.Default
    private List<Certification> certifications = new ArrayList<>();
}
