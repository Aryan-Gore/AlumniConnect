package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.alumni.AlumniSelfUpdateRequest;
import com.alumniconnect.backend.entity.*;
import com.alumniconnect.backend.repository.AlumniRecordRepository;
import com.alumniconnect.backend.repository.CareerHistoryEntryRepository;
import com.alumniconnect.backend.repository.CertificationRepository;
import com.alumniconnect.backend.repository.EducationRecordRepository;
import com.alumniconnect.backend.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AlumniProfileService} — the alumni-controlled
 * (self-service) side of profile editing.
 */
@ExtendWith(MockitoExtension.class)
class AlumniProfileServiceTest {

    @Mock private AlumniRecordRepository alumniRecordRepository;
    @Mock private CareerHistoryEntryRepository careerHistoryRepository;
    @Mock private EducationRecordRepository educationRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private CertificationRepository certificationRepository;
    @Mock private SkillService skillService;
    @Mock private AlumniMapper alumniMapper;

    private AlumniProfileService alumniProfileService;

    @BeforeEach
    void setUp() {
        alumniProfileService = new AlumniProfileService(
                alumniRecordRepository,
                careerHistoryRepository,
                educationRepository,
                projectRepository,
                certificationRepository,
                skillService,
                alumniMapper);
    }

    private AlumniRecord recordOwnedBy(Long userId) {
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("Computer Science");
        dept.setCode("CS");

        Institute inst = new Institute();
        inst.setId(1L);
        inst.setName("SVVV");
        inst.setCode("SVVV");
        dept.setInstitute(inst);

        return AlumniRecord.builder()
                .id(1L)
                .fullName("Some Alumnus")
                .enrollmentNumber("CS2020001")
                .department(dept)
                .graduationYear(2020)
                .directoryVisibility(DirectoryVisibility.STUDENTS_AND_ALUMNI)
                .status(AlumniStatus.ACTIVE)
                .user(User.builder().id(userId).build())
                .build();
    }

    @Test
    void updateOwnProfile_onlyOverwritesFieldsThatWerePresentInTheRequest() {
        AlumniRecord record = recordOwnedBy(55L);
        record.setCurrentCompany("Old Company");
        record.setJobTitle("Old Title");

        when(alumniRecordRepository.findByUserId(55L)).thenReturn(Optional.of(record));
        when(alumniRecordRepository.save(record)).thenReturn(record);
        when(alumniMapper.toDetail(any(), any(), any())).thenAnswer(inv -> {
            // return a minimal stub — the real assertion is on the mutated record fields
            return com.alumniconnect.backend.dto.alumni.AlumniResponse.builder()
                    .id(((AlumniRecord) inv.getArgument(0)).getId())
                    .build();
        });

        AlumniSelfUpdateRequest request = new AlumniSelfUpdateRequest();
        request.setCurrentCompany("New Company"); // jobTitle deliberately left null

        alumniProfileService.updateOwnProfile(55L, request);

        assertThat(record.getCurrentCompany()).isEqualTo("New Company");
        assertThat(record.getJobTitle()).isEqualTo("Old Title"); // untouched — PATCH semantics
    }
}
