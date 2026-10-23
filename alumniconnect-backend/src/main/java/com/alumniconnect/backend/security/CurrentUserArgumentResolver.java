package com.alumniconnect.backend.security;

import com.alumniconnect.backend.entity.User;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@code @CurrentUser User} parameters in {@code @RestController}
 * methods by reading the {@link CustomUserDetails} principal that
 * {@link JwtAuthFilter} placed in the {@link org.springframework.security.core.context.SecurityContext}.
 *
 * Returns {@code null} for unauthenticated requests (public endpoints) so
 * controllers that genuinely need to handle both anonymous and authenticated
 * callers can do so with a null-check. Controllers guarded by Spring Security
 * rules never reach this path unauthenticated.
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return details.getUser();
        }
        return null;
    }
}
