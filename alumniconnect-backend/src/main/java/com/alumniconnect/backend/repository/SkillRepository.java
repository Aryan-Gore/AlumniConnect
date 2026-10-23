package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByNameIgnoreCase(String name);

    /** Autocomplete for the skill picker: skills whose name starts with the prefix, alphabetically. */
    @Query("select s from Skill s where lower(s.name) like lower(concat(:prefix, '%')) order by s.name asc")
    List<Skill> findByPrefix(@Param("prefix") String prefix);
}
