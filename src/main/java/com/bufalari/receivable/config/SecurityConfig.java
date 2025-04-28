package com.bufalari.receivable.config;

import com.bufalari.receivable.security.JwtAuthenticationFilter; // Import correto
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter; // Injetado

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    private static final String[] PUBLIC_MATCHERS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PUBLIC_MATCHERS).permitAll()
                // --- AJUSTAR REGRAS PARA RECEIVABLE ---
                .requestMatchers(HttpMethod.GET, "/api/receivables/**").hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT", "FINANCIAL_VIEWER", "SALES") // Quem pode ver?
                .requestMatchers(HttpMethod.POST, "/api/receivables").hasAnyRole("ADMIN", "ACCOUNTANT", "SALES") // Quem pode criar?
                .requestMatchers(HttpMethod.PUT, "/api/receivables/**").hasAnyRole("ADMIN", "ACCOUNTANT", "SALES") // Quem pode atualizar?
                .requestMatchers(HttpMethod.PATCH, "/api/receivables/**").hasAnyRole("ADMIN", "ACCOUNTANT", "SALES", "MANAGER") // Quem pode dar patch (status)?
                .requestMatchers(HttpMethod.DELETE, "/api/receivables/**").hasRole("ADMIN") // Quem pode deletar?
                // Os endpoints de resumo (/summary-*) podem precisar de roles específicas se forem apenas internos
                // .requestMatchers("/api/receivables/summary/**").hasRole("INTERNAL_SERVICE") // Exemplo
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Restringir em produção!
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}