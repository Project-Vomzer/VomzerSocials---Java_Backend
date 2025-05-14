package org.vomzersocials.user.springSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint entryPoint;
    private final JwtUtil jwtUtil;

    public SecurityConfig(CustomAuthenticationEntryPoint entryPoint,
                          JwtUtil jwtUtil) {
        this.entryPoint = entryPoint;
        this.jwtUtil = jwtUtil;
    }


//    @Bean
//    public @NonNull SecurityWebFilterChain(
//            ServerHttpSecurity http,
//            JwtAuthenticationFilter
//    ) {
//        return http
//                // Disable CSRF since we’re stateless
//                .csrf(ServerHttpSecurity.CsrfSpec::disable)
//
//                // Use our custom entry point for unauthorized requests
//                .exceptionHandling(exceptions ->
//                        exceptions.authenticationEntryPoint(customAuthenticationEntryPoint)
//                )
//
//                // Don’t use the default WebFlux SecurityContextRepository (stateless)
//                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
//
//                // Stateless session management
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//
//                // Route security rules
//                .authorizeExchange(exchanges -> exchanges
//                        .pathMatchers("/api/auth/**").permitAll()    // open endpoints
//                        .anyExchange().authenticated()                // all others require auth
//                )
//
//                // Add our JWT filter at the AUTHENTICATION phase
//                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
//
//                .build();
//    }

    @Bean
    public @NonNull SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                                  JwtAuthenticationFilter jwtFilter
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(authorization -> authorization
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("api/auth/admin/**").hasAuthority("ZKSocials")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public @NonNull JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    @Bean
    public @NonNull BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

