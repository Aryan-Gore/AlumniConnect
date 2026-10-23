package com.alumniconnect.backend.config;

import com.alumniconnect.backend.security.CurrentUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * MVC-level configuration.
 *
 * Registers {@link CurrentUserArgumentResolver} so that
 * {@code @CurrentUser User user} parameters in {@code @RestController}
 * methods are resolved from the JWT-authenticated security context without
 * any boilerplate in each controller.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
