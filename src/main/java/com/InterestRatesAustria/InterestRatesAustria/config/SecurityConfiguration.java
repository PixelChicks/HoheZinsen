package com.InterestRatesAustria.InterestRatesAustria.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.InterestRatesAustria.InterestRatesAustria.security.JwtAuthenticationEntryPoint;
import com.InterestRatesAustria.InterestRatesAustria.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * Security configuration for the application.
 * Configures JWT-based authentication with role-based access control.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(customizer -> {
                    customizer.authenticationEntryPoint(new JwtAuthenticationEntryPoint(objectMapper));
                })
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize
                        // Public endpoints
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        
                        // Admin-only endpoints for modifications
                        .requestMatchers("/interest-rate/create").hasRole("ADMIN")
                        .requestMatchers("/interest-rate/update/**").hasRole("ADMIN")
                        .requestMatchers("/interest-rate/delete/**").hasRole("ADMIN")
                        .requestMatchers("/field-values/update").hasRole("ADMIN")
                        .requestMatchers("/fields/add").hasRole("ADMIN")
                        .requestMatchers("/fields/update").hasRole("ADMIN")
                        .requestMatchers("/fields/reorder").hasRole("ADMIN")
                        .requestMatchers("/fields/delete/**").hasRole("ADMIN")
                        .requestMatchers("/sections/reorder").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        
                        // Read-only access for everyone (including non-authenticated)
                        .requestMatchers("/", "/api/interest-rates/**", "/api/global-fields").permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(httpSecurityLogoutConfigurer -> {
                    httpSecurityLogoutConfigurer.logoutUrl("/api/auth/logout");
                    httpSecurityLogoutConfigurer.addLogoutHandler(logoutHandler);
                    httpSecurityLogoutConfigurer.logoutSuccessHandler(
                        (request, response, authentication) -> SecurityContextHolder.clearContext()
                    );
                });

        return http.build();
    }
}