package com.alumniconnect.backend.config;

import com.alumniconnect.backend.security.CustomUserDetailsService;
import com.alumniconnect.backend.security.JwtAccessDeniedHandler;
import com.alumniconnect.backend.security.JwtAuthFilter;
import com.alumniconnect.backend.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Route-level authorization here is a UX/defense-in-depth layer, not the
 * only one — see the note in the frontend README: the same rules are
 * duplicated in ProtectedRoute on the client, and duplication is
 * intentional, not a shortcut either side is allowed to skip.
 *
 * What is public, and why:
 *  - login / register / claim / Google sign-in: obviously, nobody has a token yet;
 *  - GET /institutes: the registration form needs the department list;
 *  - GET /public/stats: the landing page's headline numbers (counts only, nothing personal);
 *  - GET /files/**: profile photos and post images are loaded by plain
 *    &lt;img&gt; tags, which cannot send an Authorization header. File names are
 *    random UUIDs, so they are unguessable;
 *  - /ws/**: the WebSocket handshake itself. Authentication happens on the
 *    STOMP CONNECT frame instead (see WebSocketAuthInterceptor).
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /** One origin, or several separated by commas (e.g. the Vite dev server plus a deployed site). */
    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/register/**", "/auth/claim/**",
                                "/auth/google", "/auth/google/**", "/auth/config", "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/institutes", "/institutes/**", "/files/**", "/public/stats").permitAll()
                        .requestMatchers("/ws", "/ws/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/alumni/me", "/alumni/me/**").hasRole("ALUMNI")
                        .requestMatchers("/students/me", "/students/me/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/mentorship/requests").hasRole("STUDENT")
                        .requestMatchers("/mentorship/requests/sent").hasRole("STUDENT")
                        .requestMatchers("/mentorship/requests/received").hasRole("ALUMNI")
                        .requestMatchers(HttpMethod.PATCH, "/mentorship/requests/**").hasRole("ALUMNI")
                        // Jobs, the feed and chat are open to every signed-in role; who may
                        // touch a given listing/post/conversation is checked in the service.
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /** The same list is reused by the WebSocket endpoint's origin check. */
    public List<String> allowedOrigins() {
        return Arrays.stream(allowedOrigin.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
