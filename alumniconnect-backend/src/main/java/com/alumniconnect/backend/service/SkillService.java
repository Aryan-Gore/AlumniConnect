package com.alumniconnect.backend.service;

import com.alumniconnect.backend.entity.Skill;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Skills are shared rows, not per-person strings: "Java" typed by two alumni
 * is the same Skill, which is what lets the directory answer "who knows
 * Spring Boot" with a join. Names are matched case-insensitively and keep the
 * casing of whoever added them first.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillService {

    public static final int MAX_SKILLS = 30;
    public static final int MAX_NAME_LENGTH = 50;

    private final SkillRepository skillRepository;

    /** Finds or creates a Skill for each distinct, non-blank name; blank entries and case-duplicates are dropped. */
    @Transactional
    public Set<Skill> resolve(Collection<String> names) {
        Set<Skill> result = new LinkedHashSet<>();
        Set<String> seen = new HashSet<>();
        if (names == null) return result;

        for (String raw : names) {
            String name = normalize(raw);
            if (name == null || !seen.add(name.toLowerCase())) continue;
            if (seen.size() > MAX_SKILLS) {
                throw new BadRequestException("You can list at most " + MAX_SKILLS + " skills.");
            }
            Skill skill = skillRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> skillRepository.save(Skill.builder().name(name).build()));
            result.add(skill);
        }
        return result;
    }

    /** Autocomplete for the skill picker. */
    public List<String> suggest(String prefix) {
        if (prefix == null || prefix.isBlank()) return List.of();
        return skillRepository.findByPrefix(prefix.trim()).stream()
                .limit(10)
                .map(Skill::getName)
                .collect(Collectors.toList());
    }

    /** Trims and collapses inner whitespace; returns null for blank input. */
    public static String normalize(String raw) {
        if (raw == null) return null;
        String name = raw.trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) return null;
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BadRequestException("Skill names can be at most " + MAX_NAME_LENGTH + " characters.");
        }
        return name;
    }
}
