package com.alumniconnect.backend.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the authenticated {@link com.alumniconnect.backend.entity.User}
 * entity directly into a controller method parameter.
 *
 * <pre>
 * {@code
 * @GetMapping("/me")
 * public ResponseEntity<UserResponse> getMe(@CurrentUser User user) { ... }
 * }
 * </pre>
 *
 * Resolved by {@link CurrentUserArgumentResolver}, which is registered in
 * {@link com.alumniconnect.backend.config.WebMvcConfig}.
 *
 * The parameter type must be {@code User}. If the request is not
 * authenticated (i.e. called on a public endpoint), the resolved value is
 * {@code null}; controllers on authenticated routes never see null here
 * because Spring Security blocks unauthenticated requests before the
 * resolver runs.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
