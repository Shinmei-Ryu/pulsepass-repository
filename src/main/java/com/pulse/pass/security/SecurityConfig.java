package com.pulse.pass.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) throws Exception {

        // Instancias locales, no beans: Boot registraría un Filter-bean también como filtro de servlet
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(resolver);
        RestAccessDeniedHandler accessDeniedHandler = new RestAccessDeniedHandler(resolver);
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService, entryPoint);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth

                        // Públicas
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/venues/**", "/api/artists/**").permitAll()

                        // Eventos: lectura pública (los no publicados se filtran en el Service, Etapa 9);
                        // escritura solo ADMIN. La ruta más específica va primero.
                        .requestMatchers(HttpMethod.GET, "/api/events/*/tickets/paid").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/events/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/events", "/api/events/*/artists/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/events/*/publish").hasRole("ADMIN")

                        // Tickets
                        .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/use").hasAnyRole("STAFF", "ADMIN")

                        // Todo lo demás (consultas de tickets y usuarios, cancelar, /api/auth/me):
                        // autenticado. La propiedad se valida en el Service (Etapa 9).
                        .anyRequest().authenticated());

        return http.build();
    }
}