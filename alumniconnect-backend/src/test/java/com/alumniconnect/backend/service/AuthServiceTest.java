package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.auth.*;
import com.alumniconnect.backend.entity.*;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.DuplicateResourceException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.AlumniRecordRepository;
import com.alumniconnect.backend.repository.StudentProfileRepository;
import com.alumniconnect.backend.repository.UserRepository;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.GoogleTokenVerifier;
import com.alumniconnect.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests only — no Spring context, no database. Every collaborator is
 * mocked so these run in milliseconds and test AuthService's own logic in
 * isolation. (Written but not run — see the backend README's compile
 * caveat: this sandbox has no path to Maven Central, so JUnit/Mockito
 * themselves were never resolved here either.)
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private AlumniRecordRepository alumniRecordRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private DepartmentService departmentService;
    @Mock private UserSummaryService userSummaryService;
    @Mock private GoogleTokenVerifier googleTokenVerifier;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, studentProfileRepository, alumniRecordRepository,
                passwordEncoder, authenticationManager, jwtService,
                departmentService, userSummaryService, googleTokenVerifier);
    }

    // ------------------------------------------------------------------
    // Claim flow — the security-critical path
    // ------------------------------------------------------------------

    @Test
    void claimProfile_rejectsWhenClientSuppliedIdDoesNotMatchTheServerSideLookup() {
        AlumniRecord actualMatch = unclaimedRecord(99L, "Rahul Sharma", "CS2021001", "rahul@example.com", 2025);
        when(alumniRecordRepository.findByEnrollmentNumberAndEmailIgnoreCaseAndGraduationYearAndUserIsNull(
                "CS2021001", "rahul@example.com", 2025)).thenReturn(Optional.of(actualMatch));

        ClaimConfirmRequest request = new ClaimConfirmRequest();
        request.setAlumniId(1L); // does NOT match actualMatch's id (99)
        request.setFullName("Rahul Sharma");
        request.setEnrollmentNumber("CS2021001");
        request.setEmail("rahul@example.com");
        request.setGraduationYear("2025");
        request.setPassword("a-real-password");

        assertThatThrownBy(() -> authService.claimProfile(request))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
        verify(alumniRecordRepository, never()).save(any());
    }

    @Test
    void claimProfile_rejectsARecordThatIsAlreadyClaimed() {
        AlumniRecord alreadyClaimed = unclaimedRecord(5L, "Rahul Sharma", "CS2021001", "rahul@example.com", 2025);
        alreadyClaimed.setUser(User.builder().id(42L).build()); // already linked

        when(alumniRecordRepository.findByEnrollmentNumberAndEmailIgnoreCaseAndGraduationYearAndUserIsNull(
                "CS2021001", "rahul@example.com", 2025)).thenReturn(Optional.of(alreadyClaimed));

        ClaimConfirmRequest request = claimRequestFor(5L, "Rahul Sharma", "CS2021001", "rahul@example.com", "2025");

        assertThatThrownBy(() -> authService.claimProfile(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void claimProfile_rejectsWhenNameDoesNotMatchEvenIfOtherFieldsDo() {
        AlumniRecord record = unclaimedRecord(7L, "Rahul Sharma", "CS2021001", "rahul@example.com", 2025);
        when(alumniRecordRepository.findByEnrollmentNumberAndEmailIgnoreCaseAndGraduationYearAndUserIsNull(
                "CS2021001", "rahul@example.com", 2025)).thenReturn(Optional.of(record));

        ClaimLookupRequest request = new ClaimLookupRequest();
        request.setFullName("A Completely Different Name");
        request.setEnrollmentNumber("CS2021001");
        request.setEmail("rahul@example.com");
        request.setGraduationYear("2025");

        assertThatThrownBy(() -> authService.findClaimableRecord(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void claimProfile_rejectsANonNumericGraduationYearWithABadRequestNotA500() {
        ClaimLookupRequest request = new ClaimLookupRequest();
        request.setFullName("Rahul Sharma");
        request.setEnrollmentNumber("CS2021001");
        request.setEmail("rahul@example.com");
        request.setGraduationYear("not-a-year");

        assertThatThrownBy(() -> authService.findClaimableRecord(request))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(alumniRecordRepository);
    }

    @Test
    void claimProfile_succeedsAndLinksTheExistingRecordRatherThanCreatingANewOne() {
        AlumniRecord record = unclaimedRecord(5L, "Rahul Sharma", "CS2021001", "rahul@example.com", 2025);
        when(alumniRecordRepository.findByEnrollmentNumberAndEmailIgnoreCaseAndGraduationYearAndUserIsNull(
                "CS2021001", "rahul@example.com", 2025)).thenReturn(Optional.of(record));
        when(userRepository.existsByEmail("rahul@example.com")).thenReturn(false);
        when(passwordEncoder.encode("a-real-password")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(101L);
            return u;
        });
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("fake.jwt.token");

        ClaimConfirmRequest request = claimRequestFor(5L, "Rahul Sharma", "CS2021001", "rahul@example.com", "2025");

        AuthResponse response = authService.claimProfile(request);

        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
        assertThat(response.getUser().getRole()).isEqualTo(Role.ALUMNI);

        verify(alumniRecordRepository, times(1)).save(record);
        assertThat(record.getStatus()).isEqualTo(AlumniStatus.CLAIMED);
        assertThat(record.getUser()).isNotNull();
        assertThat(record.getUser().getId()).isEqualTo(101L);
    }

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

    @Test
    void registerStudent_rejectsADuplicateEmail() {
        RegisterStudentRequest request = new RegisterStudentRequest();
        request.setEmail("student@example.com");
        request.setFullName("A Student");
        request.setEnrollmentNumber("CS2024001");
        request.setPassword("a-real-password");

        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerStudent(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerStudent_rejectsADuplicateEnrollmentNumber() {
        RegisterStudentRequest request = new RegisterStudentRequest();
        request.setEmail("student@example.com");
        request.setFullName("A Student");
        request.setEnrollmentNumber("CS2024001");
        request.setPassword("a-real-password");

        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(studentProfileRepository.existsByEnrollmentNumber("CS2024001")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerStudent(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // ------------------------------------------------------------------

    /** Builds an unclaimed AlumniRecord with a stub Department (no institute needed for these tests). */
    private AlumniRecord unclaimedRecord(Long id, String name, String enrollment, String email, int year) {
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("Computer Science");
        dept.setCode("CS");

        return AlumniRecord.builder()
                .id(id).fullName(name).enrollmentNumber(enrollment).email(email)
                .graduationYear(year).department(dept).status(AlumniStatus.IMPORTED)
                .build();
    }

    private ClaimConfirmRequest claimRequestFor(Long id, String name, String enrollment, String email, String year) {
        ClaimConfirmRequest request = new ClaimConfirmRequest();
        request.setAlumniId(id);
        request.setFullName(name);
        request.setEnrollmentNumber(enrollment);
        request.setEmail(email);
        request.setGraduationYear(year);
        request.setPassword("a-real-password");
        return request;
    }
}
