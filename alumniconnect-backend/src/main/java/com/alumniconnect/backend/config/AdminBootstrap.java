package com.alumniconnect.backend.config;

import com.alumniconnect.backend.entity.Role;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admins are never created through the public API (that would be a
 * privilege-escalation hole), so a fresh installation needs one seeded
 * account. Set ADMIN_EMAIL and ADMIN_PASSWORD and this creates it on
 * start-up — only if no admin exists yet, so restarting never overwrites a
 * password that has since been changed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.email:}")
    private String email;

    @Value("${app.bootstrap-admin.password:}")
    private String password;

    @Value("${app.bootstrap-admin.full-name:Alumni Office}")
    private String fullName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            if (!userRepository.existsByRole(Role.ADMIN)) {
                log.warn("No admin account exists. Set ADMIN_EMAIL and ADMIN_PASSWORD and restart to create one.");
            }
            return;
        }
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        if (password.length() < 8) {
            log.warn("ADMIN_PASSWORD must be at least 8 characters; no admin account was created.");
            return;
        }
        if (userRepository.existsByEmail(email.trim())) {
            log.warn("{} already belongs to a non-admin account; no admin account was created.", email.trim());
            return;
        }

        userRepository.save(User.builder()
                .email(email.trim())
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(Role.ADMIN)
                .build());
        log.info("Created the initial admin account {} — change its password after the first sign-in.", email.trim());
    }
}
