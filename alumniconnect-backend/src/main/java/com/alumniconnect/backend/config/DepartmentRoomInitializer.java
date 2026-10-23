package com.alumniconnect.backend.config;

import com.alumniconnect.backend.service.chat.DepartmentRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Ensures that every department has exactly one group conversation row at startup.
 * Runs once after the application context is fully initialized. Idempotent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentRoomInitializer implements ApplicationRunner {

    private final DepartmentRoomService departmentRoomService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("DepartmentRoomInitializer: ensuring department group rooms exist…");
        departmentRoomService.ensureAll();
    }
}
