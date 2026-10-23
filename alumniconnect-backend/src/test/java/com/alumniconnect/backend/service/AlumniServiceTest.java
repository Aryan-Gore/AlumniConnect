package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.alumni.AlumniResponse;
import com.alumniconnect.backend.entity.*;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.AlumniRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlumniServiceTest {

    @Mock private AlumniRecordRepository alumniRecordRepository;
    @Mock private DepartmentService departmentService;
    @Mock private AlumniMapper alumniMapper;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;

    private AlumniService alumniService;

    @BeforeEach
    void setUp() {
        alumniService = new AlumniService(
                alumniRecordRepository, departmentService, alumniMapper,
                auditLogService, notificationService);
    }

    private AlumniRecord recordWithVisibility(DirectoryVisibility visibility, Long ownerUserId) {
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("Computer Science");
        dept.setCode("CS");

        Institute inst = new Institute();
        inst.setId(1L);
        inst.setName("SVVV");
        inst.setCode("SVVV");
        dept.setInstitute(inst);

        AlumniRecord.AlumniRecordBuilder builder = AlumniRecord.builder()
                .id(1L).fullName("Some Alumnus").enrollmentNumber("CS2020001")
                .department(dept).graduationYear(2020)
                .directoryVisibility(visibility).status(AlumniStatus.ACTIVE);
        if (ownerUserId != null) {
            builder.user(User.builder().id(ownerUserId).build());
        }
        return builder.build();
    }

    @Test
    void getById_hidesAPrivateRecordFromAnyoneWhoIsNotTheOwnerOrAnAdmin() {
        AlumniRecord privateRecord = recordWithVisibility(DirectoryVisibility.PRIVATE, 55L);
        when(alumniRecordRepository.findById(1L)).thenReturn(Optional.of(privateRecord));

        assertThatThrownBy(() -> alumniService.getById(1L, Role.STUDENT, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_stillShowsAPrivateRecordToItsOwner() {
        AlumniRecord privateRecord = recordWithVisibility(DirectoryVisibility.PRIVATE, 55L);
        when(alumniRecordRepository.findById(1L)).thenReturn(Optional.of(privateRecord));
        when(alumniMapper.toDetail(any(), any(), any())).thenAnswer(inv -> {
            AlumniRecord r = inv.getArgument(0);
            return AlumniResponse.builder().id(r.getId()).build();
        });

        AlumniResponse response = alumniService.getById(1L, Role.ALUMNI, 55L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getById_stillShowsAPrivateRecordToAnAdmin() {
        AlumniRecord privateRecord = recordWithVisibility(DirectoryVisibility.PRIVATE, 55L);
        when(alumniRecordRepository.findById(1L)).thenReturn(Optional.of(privateRecord));
        when(alumniMapper.toDetail(any(), any(), any())).thenAnswer(inv -> {
            AlumniRecord r = inv.getArgument(0);
            return AlumniResponse.builder().id(r.getId()).build();
        });

        AlumniResponse response = alumniService.getById(1L, Role.ADMIN, 1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void adminInviteToClaim_refusesARecordThatIsNotInImportedStatus() {
        AlumniRecord alreadyInvited = recordWithVisibility(DirectoryVisibility.STUDENTS_AND_ALUMNI, null);
        alreadyInvited.setStatus(AlumniStatus.INVITED);
        when(alumniRecordRepository.findById(1L)).thenReturn(Optional.of(alreadyInvited));

        User admin = User.builder().id(1L).role(Role.ADMIN).build();

        assertThatThrownBy(() -> alumniService.adminInviteToClaim(1L, admin))
                .isInstanceOf(BadRequestException.class);
    }
}
