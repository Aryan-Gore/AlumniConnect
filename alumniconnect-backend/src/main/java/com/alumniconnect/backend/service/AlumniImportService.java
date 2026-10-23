package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.alumni.*;
import com.alumniconnect.backend.entity.*;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.AlumniImportBatchRepository;
import com.alumniconnect.backend.repository.AlumniImportErrorRepository;
import com.alumniconnect.backend.repository.AlumniRecordRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements the exact workflow diagrammed in the SRS:
 * upload -> validate -> column mapping -> preview -> clean/transform ->
 * detect duplicates -> show errors -> admin confirms -> insert -> summary.
 *
 * The raw file is held in AlumniImportBatch.stagedContent between steps
 * (a staging column, not permanent storage — see the schema comment) and
 * cleared once CONFIRMED. This keeps the wizard stateless from the
 * frontend's point of view: every step just needs the importId.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlumniImportService {

    // Canonical field keys the frontend's column-mapping dropdown offers.
    private static final Set<String> SYSTEM_FIELDS = Set.of(
            "fullName", "email", "enrollmentNumber", "department", "institute", "degree", "batch",
            "graduationYear", "phone", "company", "jobTitle", "industry", "location", "ignore");

    private static final List<String> REQUIRED_FIELDS =
            List.of("fullName", "email", "enrollmentNumber", "department", "graduationYear");

    private static final int PREVIEW_SAMPLE_SIZE = 20;

    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final AlumniImportBatchRepository batchRepository;
    private final AlumniImportErrorRepository importErrorRepository;
    private final AlumniRecordRepository alumniRecordRepository;
    private final AuditLogService auditLogService;
    private final AnalyticsClient analyticsClient;
    private final DepartmentService departmentService;

    @Transactional
    public ImportUploadResponse upload(MultipartFile file, User admin) {
        if (file.isEmpty()) {
            throw new BadRequestException("The uploaded file is empty.");
        }
        String content = readAsUtf8(file);

        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader().setSkipHeaderRecord(true).setTrim(true)
                .build().parse(new StringReader(content))) {

            List<String> headers = parser.getHeaderNames();
            if (headers.isEmpty()) {
                throw new BadRequestException("Couldn't find a header row in that file.");
            }
            int rowCount = (int) parser.stream().count();

            AlumniImportBatch batch = batchRepository.save(AlumniImportBatch.builder()
                    .admin(admin)
                    .originalFilename(file.getOriginalFilename())
                    .stagedContent(content)
                    .status(ImportBatchStatus.UPLOADED)
                    .totalRows(rowCount)
                    .build());

            return ImportUploadResponse.builder()
                    .importId(batch.getId())
                    .detectedColumns(headers)
                    .totalRows(rowCount)
                    .build();

        } catch (IOException ex) {
            throw new BadRequestException("Couldn't parse that file as CSV: " + ex.getMessage());
        }
    }

    @Transactional
    public void submitMapping(Long importId, Map<String, String> mapping) {
        AlumniImportBatch batch = findOrThrow(importId);

        for (String target : mapping.values()) {
            if (!SYSTEM_FIELDS.contains(target)) {
                throw new BadRequestException("Unknown mapping target: " + target);
            }
        }
        Set<String> mappedTargets = new HashSet<>(mapping.values());
        List<String> missing = REQUIRED_FIELDS.stream().filter(f -> !mappedTargets.contains(f)).toList();
        if (!missing.isEmpty()) {
            throw new BadRequestException("Map a column to each required field: " + String.join(", ", missing));
        }

        batch.setColumnMapping(mapping);
        batch.setStatus(ImportBatchStatus.MAPPED);
        batchRepository.save(batch);
    }

    @Transactional
    public ImportPreviewResponse preview(Long importId) {
        AlumniImportBatch batch = findOrThrow(importId);
        requireMapping(batch);

        List<Map<String, String>> mappedRows = parseRows(batch);
        RowEvaluator evaluator = new RowEvaluator(departmentService.importResolver());
        List<ImportPreviewRowResponse> sample = new ArrayList<>();
        long ready = 0, duplicate = 0, error = 0;

        for (int i = 0; i < mappedRows.size(); i++) {
            Map<String, String> row = mappedRows.get(i);
            RowVerdict verdict = evaluator.evaluate(row);
            switch (verdict.flag()) {
                case "ERROR" -> error++;
                case "DUPLICATE" -> duplicate++;
                default -> ready++;
            }
            if (sample.size() < PREVIEW_SAMPLE_SIZE) {
                sample.add(ImportPreviewRowResponse.builder()
                        .rowIndex(i + 1).values(row).flag(verdict.flag()).errorMessage(verdict.message()).build());
            }
        }

        batch.setStatus(ImportBatchStatus.PREVIEWED);
        batchRepository.save(batch);

        return ImportPreviewResponse.builder()
                .totalRows(mappedRows.size())
                .readyCount(ready).duplicateCount(duplicate).errorCount(error)
                .sampleRows(sample)
                .build();
    }

    @Transactional
    public ImportBatchSummaryResponse confirm(Long importId, User admin) {
        AlumniImportBatch batch = findOrThrow(importId);
        requireMapping(batch);

        List<Map<String, String>> mappedRows = parseRows(batch);
        RowEvaluator evaluator = new RowEvaluator(departmentService.importResolver());
        List<RowVerdict> verdicts = new ArrayList<>();
        for (Map<String, String> row : mappedRows) {
            verdicts.add(evaluator.evaluate(row));
        }

        // One batch call to the Python service for every row that will
        // actually be inserted — not one call per row, and not for rows
        // we're about to skip as duplicates or errors anyway.
        List<String> companiesToClean = new ArrayList<>();
        for (int i = 0; i < mappedRows.size(); i++) {
            if ("READY".equals(verdicts.get(i).flag())) {
                String company = mappedRows.get(i).get("company");
                companiesToClean.add(company != null ? company : "");
            }
        }
        List<String> cleanedCompanies = analyticsClient.cleanCompanyNames(companiesToClean);
        int cleanedIndex = 0;

        int successful = 0, failed = 0, duplicates = 0;
        importErrorRepository.deleteAll(importErrorRepository.findByBatchId(batch.getId()));

        for (int i = 0; i < mappedRows.size(); i++) {
            Map<String, String> row = mappedRows.get(i);
            RowVerdict verdict = verdicts.get(i);

            switch (verdict.flag()) {
                case "ERROR" -> {
                    failed++;
                    importErrorRepository.save(AlumniImportError.builder()
                            .batch(batch).rowIndex(i + 1).errorMessage(verdict.message()).rawRowData(row).build());
                }
                case "DUPLICATE" -> duplicates++;
                default -> {
                    String standardizedCompany = cleanedCompanies.get(cleanedIndex++);
                    alumniRecordRepository.save(AlumniRecord.builder()
                            .fullName(row.get("fullName").trim())
                            .email(row.get("email").trim())
                            .enrollmentNumber(row.get("enrollmentNumber").trim())
                            .department(verdict.department())
                            .degree(row.get("degree"))
                            .batch(row.get("batch"))
                            .graduationYear(Integer.parseInt(row.get("graduationYear").trim()))
                            .phone(row.get("phone"))
                            .currentCompany(standardizedCompany.isBlank() ? null : standardizedCompany)
                            .jobTitle(row.get("jobTitle"))
                            .industry(row.get("industry"))
                            .currentLocation(row.get("location"))
                            .status(AlumniStatus.IMPORTED)
                            .importedAt(Instant.now())
                            .build());
                    successful++;
                }
            }
        }

        batch.setTotalRows(mappedRows.size());
        batch.setSuccessfulRows(successful);
        batch.setFailedRows(failed);
        batch.setDuplicateRows(duplicates);
        batch.setStatus(ImportBatchStatus.CONFIRMED);
        batch.setConfirmedAt(Instant.now());
        batch.setStagedContent(null); // staging only — no longer needed once confirmed
        batchRepository.save(batch);

        auditLogService.record(admin, AuditAction.IMPORT_ALUMNI,
                mappedRows.size() + " records — " + successful + " succeeded, " + failed + " failed, " + duplicates + " duplicates skipped");

        return ImportBatchSummaryResponse.from(batch);
    }

    public List<ImportBatchSummaryResponse> history() {
        return batchRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ImportBatchSummaryResponse::from).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------

    private record RowVerdict(String flag, String message, Department department) {
        static RowVerdict error(String message) {
            return new RowVerdict("ERROR", message, null);
        }

        static RowVerdict duplicate(String message) {
            return new RowVerdict("DUPLICATE", message, null);
        }

        static RowVerdict ready(Department department) {
            return new RowVerdict("READY", null, department);
        }
    }

    /**
     * Judges rows one after another, remembering which enrollment numbers and
     * emails earlier rows of the SAME file already used — otherwise two rows
     * for one person would both look "ready" and the second insert would hit
     * the database's unique constraint halfway through the import.
     */
    private class RowEvaluator {

        private final DepartmentResolver departments;
        private final Set<String> seenEnrollments = new HashSet<>();
        private final Set<String> seenEmails = new HashSet<>();

        RowEvaluator(DepartmentResolver departments) {
            this.departments = departments;
        }

        RowVerdict evaluate(Map<String, String> row) {
            for (String field : REQUIRED_FIELDS) {
                if (isBlank(row.get(field))) {
                    return RowVerdict.error("Missing required field: " + field);
                }
            }
            String fullName = row.get("fullName").trim();
            String email = row.get("email").trim();
            String enrollment = row.get("enrollmentNumber").trim();

            try {
                Integer.parseInt(row.get("graduationYear").trim());
            } catch (NumberFormatException ex) {
                return RowVerdict.error("Graduation year is not a number: " + row.get("graduationYear"));
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return RowVerdict.error("Email doesn't look valid: " + email);
            }
            if (fullName.length() > 150 || enrollment.length() > 30 || email.length() > 255) {
                return RowVerdict.error("A value is too long (name 150, enrollment number 30, email 255 characters).");
            }

            Department department;
            try {
                department = departments.resolve(row.get("department"), row.get("institute"));
            } catch (BadRequestException ex) {
                return RowVerdict.error(ex.getMessage());
            }
            if (department == null) {
                return RowVerdict.error("Unknown department: " + row.get("department").trim());
            }

            String enrollmentKey = enrollment.toLowerCase();
            String emailKey = email.toLowerCase();
            if (seenEnrollments.contains(enrollmentKey) || seenEmails.contains(emailKey)) {
                return RowVerdict.duplicate("Repeats an earlier row in this file");
            }
            if (alumniRecordRepository.existsByEnrollmentNumber(enrollment)
                    || alumniRecordRepository.existsByEmailIgnoreCase(email)) {
                return RowVerdict.duplicate("Matches an existing enrollment number or email");
            }

            seenEnrollments.add(enrollmentKey);
            seenEmails.add(emailKey);
            return RowVerdict.ready(department);
        }
    }

    /** Re-parses the staged CSV and applies the stored column mapping to every row. */
    private List<Map<String, String>> parseRows(AlumniImportBatch batch) {
        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader().setSkipHeaderRecord(true).setTrim(true)
                .build().parse(new StringReader(batch.getStagedContent()))) {

            Map<String, String> mapping = batch.getColumnMapping();
            List<Map<String, String>> result = new ArrayList<>();

            for (CSVRecord record : parser) {
                Map<String, String> mapped = new HashMap<>();
                for (var entry : mapping.entrySet()) {
                    String csvColumn = entry.getKey();
                    String systemField = entry.getValue();
                    if ("ignore".equals(systemField) || !record.isSet(csvColumn)) continue;
                    mapped.put(systemField, record.get(csvColumn));
                }
                result.add(mapped);
            }
            return result;
        } catch (IOException ex) {
            throw new BadRequestException("Couldn't re-read the staged file: " + ex.getMessage());
        }
    }

    private void requireMapping(AlumniImportBatch batch) {
        if (batch.getColumnMapping() == null || batch.getColumnMapping().isEmpty()) {
            throw new BadRequestException("Submit a column mapping before previewing or confirming.");
        }
        if (batch.getStagedContent() == null) {
            throw new BadRequestException("This import has already been confirmed — there's nothing staged to re-process.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String readAsUtf8(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BadRequestException("Couldn't read the uploaded file.");
        }
    }

    private AlumniImportBatch findOrThrow(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch " + id + " not found."));
    }
}
