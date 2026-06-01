package com.formulario.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf
                // Excluir endpoints llamados desde servidores externos (sin CSRF token)
                .ignoringRequestMatchers(
                    "/api/**",      // Bondarea webhook - llamada de servidor a servidor
                    "/examen/**"    // Flujo del candidato - puede usar AJAX
                )
            )
            .authorizeHttpRequests(auth -> auth
                // API con su propio sistema de tokens
                .requestMatchers("/api/**").permitAll()
                // Rutas públicas para candidatos
                .requestMatchers("/", "/login", "/logout").permitAll()
                .requestMatchers("/examen/**").permitAll()
                .requestMatchers("/resultado/**").permitAll()
                .requestMatchers("/recomendaciones/**").permitAll()
                .requestMatchers("/pdf/**").permitAll()
                // Endpoints de diagnóstico (bajo riesgo, sin datos sensibles)
                .requestMatchers("/basic", "/hello", "/test/**").permitAll()
                // Recursos estáticos
                .requestMatchers("/static/**", "/css/**", "/js/**",
                                 "/images/**", "/uploads/**", "/favicon.ico").permitAll()
                // Todo lo demás requiere autenticación (dashboard, admin, configuracion, etc.)
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .userDetailsService(userDetailsService);

        return http.build();
    }
} 