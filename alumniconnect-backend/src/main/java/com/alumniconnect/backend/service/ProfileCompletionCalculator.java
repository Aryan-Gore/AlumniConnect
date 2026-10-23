package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.common.ProfileCompletionResponse;
import com.alumniconnect.backend.entity.AlumniRecord;
import com.alumniconnect.backend.entity.StudentProfile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns "how complete is this profile?" into a weighted checklist. The
 * weights live here, in one place, and add up to exactly 100 so the
 * percentage is just the sum of the finished items.
 *
 * Reads lazy associations (skills, sections, the linked user's photo) —
 * call it inside a transaction.
 */
@Component
public class ProfileCompletionCalculator {

    private static final int SUMMARY_MIN_LENGTH = 50;
    private static final int SKILLS_MIN = 3;

    public ProfileCompletionResponse forAlumni(AlumniRecord r) {
        boolean hasPhoto = r.getUser() != null && notBlank(r.getUser().getProfilePhotoUrl());
        boolean hasLink = notBlank(r.getLinkedinUrl()) || notBlank(r.getGithubUrl()) || notBlank(r.getPortfolioUrl());

        List<ProfileCompletionResponse.Item> items = new ArrayList<>();
        items.add(item("photo", "Profile photo", "Upload a clear headshot", 10, hasPhoto));
        items.add(item("summary", "Professional summary",
                "Write at least " + SUMMARY_MIN_LENGTH + " characters about what you do",
                10, r.getProfessionalSummary() != null && r.getProfessionalSummary().trim().length() >= SUMMARY_MIN_LENGTH));
        items.add(item("position", "Current position", "Add your current company and job title",
                15, notBlank(r.getCurrentCompany()) && notBlank(r.getJobTitle())));
        items.add(item("location", "Location", "Add the city you're based in", 5, notBlank(r.getCurrentLocation())));
        items.add(item("skills", "Skills", "List at least " + SKILLS_MIN + " skills", 10, r.getSkills().size() >= SKILLS_MIN));
        items.add(item("experience", "Work experience", "Add a role to your career history",
                10, !r.getCareerHistory().isEmpty()));
        items.add(item("education", "Education", "Add a degree you've completed", 10, !r.getEducationRecords().isEmpty()));
        items.add(item("projects", "Projects", "Showcase a project you're proud of", 10, !r.getProjects().isEmpty()));
        items.add(item("certifications", "Certifications & achievements", "Add a certification, award or publication",
                5, !r.getCertifications().isEmpty()));
        items.add(item("phone", "Phone number", "Add a contact number (you control who sees it)", 5, notBlank(r.getPhone())));
        items.add(item("links", "Professional links", "Add your LinkedIn, GitHub or portfolio", 10, hasLink));

        return summarise(items);
    }

    public ProfileCompletionResponse forStudent(StudentProfile s) {
        boolean hasPhoto = s.getUser() != null && notBlank(s.getUser().getProfilePhotoUrl());

        List<ProfileCompletionResponse.Item> items = new ArrayList<>();
        items.add(item("photo", "Profile photo", "Upload a clear headshot", 30, hasPhoto));
        items.add(item("batch", "Batch", "Add your batch, e.g. 2022-2026", 10, notBlank(s.getBatch())));
        items.add(item("skills", "Skills", "List at least " + SKILLS_MIN + " skills",
                30, s.getSkills() != null && s.getSkills().size() >= SKILLS_MIN));
        items.add(item("interests", "Career interests", "Tell alumni what roles and fields interest you",
                30, notBlank(s.getCareerInterests())));

        return summarise(items);
    }

    private ProfileCompletionResponse summarise(List<ProfileCompletionResponse.Item> items) {
        int percent = 0;
        for (ProfileCompletionResponse.Item item : items) {
            if (item.isDone()) percent += item.getPoints();
        }
        return ProfileCompletionResponse.builder().percent(percent).items(items).build();
    }

    private ProfileCompletionResponse.Item item(String key, String label, String hint, int points, boolean done) {
        return ProfileCompletionResponse.Item.builder()
                .key(key).label(label).hint(hint).points(points).done(done).build();
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
