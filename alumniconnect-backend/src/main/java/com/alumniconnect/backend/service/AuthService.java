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
import com.alumniconnect.backend.security.GoogleIdentity;
import com.alumniconnect.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Sign-in and sign-up for every role:
 *  - students register themselves (password, or Google + enrollment number);
 *  - alumni never "register" — they CLAIM the record the institution imported;
 *  - admins are created by the AdminBootstrap runner, never through the API.
 *
 * Class-level readOnly transaction because building a UserResponse or a
 * ClaimLookupResponse reads lazy associations; the writers override it.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String NO_MATCH_MESSAGE =
            "No matching alumni record was found. Double-check your details or ask the admin office to invite you.";

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AlumniRecordRepository alumniRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final DepartmentService departmentService;
    private final UserSummaryService userSummaryService;
    private final GoogleTokenVerifier googleTokenVerifier;

    // ------------------------------------------------------------------
    // Password login
    // ------------------------------------------------------------------

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        // authenticate() throws BadCredentialsException / DisabledException on
        // failure — GlobalExceptionHandler turns that into a clean 401/403.

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account with that email."));
        return issue(user);
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account with that email."));
        return userSummaryService.currentUser(user);
    }

    public AuthConfigResponse authConfig() {
        return AuthConfigResponse.builder()
                .googleEnabled(googleTokenVerifier.isConfigured())
                .googleClientId(googleTokenVerifier.clientId())
                .build();
    }

    // ------------------------------------------------------------------
    // Student registration
    // ------------------------------------------------------------------

    @Transactional
    public AuthResponse registerStudent(RegisterStudentRequest request) {
        String email = request.getEmail().trim();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with that email already exists.");
        }
        String enrollment = request.getEnrollmentNumber().trim();
        requireEnrollmentFreeForStudent(enrollment);
        Department department = departmentService.require(request.getDepartmentId());

        User user = userRepository.save(User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .build());

        studentProfileRepository.save(StudentProfile.builder()
                .user(user)
                .enrollmentNumber(enrollment)
                .department(department)
                .batch(blankToNull(request.getBatch()))
                .build());

        return issue(user);
    }

    // ------------------------------------------------------------------
    // Alumni claim flow
    // ------------------------------------------------------------------

    public ClaimLookupResponse findClaimableRecord(ClaimLookupRequest request) {
        AlumniRecord record = matchRecord(request.getEnrollmentNumber(), request.getEmail(),
                request.getGraduationYear(), request.getFullName());
        return ClaimLookupResponse.from(record);
    }

    @Transactional
    public AuthResponse claimProfile(ClaimConfirmRequest request) {
        // Re-run the same match server-side rather than trusting the alumniId
        // the client sent — the lookup step is not an authorization check.
        AlumniRecord record = matchRecord(request.getEnrollmentNumber(), request.getEmail(),
                request.getGraduationYear(), request.getFullName());

        if (!record.getId().equals(request.getAlumniId())) {
            throw new BadRequestException("That record no longer matches — please look it up again.");
        }
        if (record.getUser() != null) {
            throw new DuplicateResourceException("This profile has already been claimed.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with that email already exists.");
        }

        User user = userRepository.save(User.builder()
                .fullName(record.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ALUMNI)
                .build());

        record.setUser(user);
        record.setStatus(AlumniStatus.CLAIMED);
        record.setClaimedAt(Instant.now());
        alumniRecordRepository.save(record);

        return issue(user);
    }

    // ------------------------------------------------------------------
    // Google sign-in (students only; alumni use the claim flow above)
    // ------------------------------------------------------------------

    /**
     * Step 1. Known Google account → signed in. Unknown → Google vouches for
     * the email, we still need an enrollment number and department, so hand
     * back a short-lived registration token. An existing password account with
     * the same email is NOT silently merged: our own sign-up never verified
     * that email, so merging would let whoever registered it first ride along.
     */
    public GoogleLoginResponse googleLogin(GoogleLoginRequest request) {
        GoogleIdentity identity = googleTokenVerifier.verify(request.getCredential());

        User existing = userRepository.findByGoogleSub(identity.getSub()).orElse(null);
        if (existing != null) {
            if (!existing.isActive()) {
                throw new DisabledException("Account deactivated");
            }
            return GoogleLoginResponse.builder()
                    .status(GoogleLoginResponse.AUTHENTICATED)
                    .auth(issue(existing))
                    .build();
        }

        if (userRepository.existsByEmail(identity.getEmail())) {
            throw new DuplicateResourceException(
                    "An account with this email already exists. Sign in with your email and password instead.");
        }

        return GoogleLoginResponse.builder()
                .status(GoogleLoginResponse.REGISTRATION_REQUIRED)
                .registrationToken(jwtService.generateGoogleRegistrationToken(identity))
                .email(identity.getEmail())
                .fullName(identity.getFullName())
                .pictureUrl(identity.getPictureUrl())
                .build();
    }

    /** Step 2. The person supplied their enrollment number and department; create the student account. */
    @Transactional
    public AuthResponse googleRegister(GoogleRegisterRequest request) {
        GoogleIdentity identity = jwtService.parseGoogleRegistrationToken(request.getRegistrationToken());

        // The world may have changed since step 1 (double-click, second tab).
        if (userRepository.findByGoogleSub(identity.getSub()).isPresent() || userRepository.existsByEmail(identity.getEmail())) {
            throw new DuplicateResourceException("An account for this Google user already exists. Try signing in again.");
        }
        String enrollment = request.getEnrollmentNumber().trim();
        requireEnrollmentFreeForStudent(enrollment);
        Department department = departmentService.require(request.getDepartmentId());

        User user = userRepository.save(User.builder()
                .fullName(identity.getFullName())
                .email(identity.getEmail())
                // Nobody knows this password; the account can only be entered through Google.
                .passwordHash(passwordEncoder.encode(UUID.randomUUID() + "-" + UUID.randomUUID()))
                .role(Role.STUDENT)
                .googleSub(identity.getSub())
                .build());

        studentProfileRepository.save(StudentProfile.builder()
                .user(user)
                .enrollmentNumber(enrollment)
                .department(department)
                .batch(blankToNull(request.getBatch()))
                .build());

        return issue(user);
    }

    // ------------------------------------------------------------------

    private AuthResponse issue(User user) {
        String token = jwtService.generateToken(new CustomUserDetails(user));
        return AuthResponse.builder().token(token).user(userSummaryService.currentUser(user)).build();
    }

    /**
     * An enrollment number that already belongs to an imported alumni record
     * is a graduate's — they should claim that record, not register as a student.
     */
    private void requireEnrollmentFreeForStudent(String enrollmentNumber) {
        if (studentProfileRepository.existsByEnrollmentNumber(enrollmentNumber)) {
            throw new DuplicateResourceException("That enrollment number is already registered.");
        }
        if (alumniRecordRepository.existsByEnrollmentNumber(enrollmentNumber)) {
            throw new BadRequestException(
                    "That enrollment number belongs to an alumni record. Use \"Claim your alumni profile\" instead.");
        }
    }

    private AlumniRecord matchRecord(String enrollmentNumber, String email, String graduationYearRaw, String fullName) {
        int graduationYear;
        try {
            graduationYear = Integer.parseInt(graduationYearRaw.trim());
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Graduation year should be a number, e.g. 2023.");
        }

        AlumniRecord record = alumniRecordRepository
                .findByEnrollmentNumberAndEmailIgnoreCaseAndGraduationYearAndUserIsNull(enrollmentNumber, email, graduationYear)
                .orElseThrow(() -> new ResourceNotFoundException(NO_MATCH_MESSAGE));

        if (!record.getFullName().trim().equalsIgnoreCase(fullName.trim())) {
            throw new ResourceNotFoundException(NO_MATCH_MESSAGE);
        }

        return record;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
