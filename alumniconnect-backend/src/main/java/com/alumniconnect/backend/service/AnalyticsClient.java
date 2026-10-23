package com.alumniconnect.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * The only class in this codebase that talks to the Python/FastAPI
 * analytics service. If that service is down, callers degrade gracefully
 * (see {@link #cleanCompanyNames}) rather than failing the whole request —
 * company-name standardization is a nice-to-have during import, not a
 * correctness requirement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsClient {

    private final RestClient analyticsRestClient;

    @SuppressWarnings("unchecked")
    public List<String> cleanCompanyNames(List<String> rawNames) {
        if (rawNames.isEmpty()) return rawNames;

        try {
            Map<String, Object> response = analyticsRestClient.post()
                    .uri("/internal/clean/companies")
                    .body(Map.of("company_names", rawNames))
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            return results.stream().map(r -> (String) r.get("standardized")).toList();

        } catch (Exception ex) {
            // Analytics service unreachable/erroring — import still proceeds
            // with the raw, unstandardized names rather than failing entirely.
            log.warn("Analytics service unavailable during company-name cleanup; using raw values. {}", ex.getMessage());
            return rawNames;
        }
    }
}
