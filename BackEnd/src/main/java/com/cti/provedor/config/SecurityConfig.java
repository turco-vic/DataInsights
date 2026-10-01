package com.cti.provedor.config;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/login", "/api/consultores/login",
                                "/api/auth/cadastro", "/api/auth/esqueci-senha", "/api/auth/redefinir-senha").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/publico/**").permitAll()
                        // Gestão de usuários: só administradores.
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // Telemetria: qualquer conta registra eventos; só administradores alteram ou apagam.
                        .requestMatchers(HttpMethod.PUT, "/api/telemetrias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/telemetrias/**").hasRole("ADMIN")
                        .requestMatchers("/api/telemetrias/**").authenticated()
                        // Leitura, perfil próprio e renovação de sessão: qualquer conta logada.
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .requestMatchers("/api/me/**", "/api/me", "/api/auth/renovar").authenticated()
                        // Alterações nos dados: administradores e analistas.
                        .requestMatchers("/api/clientes/**", "/api/contratos/**", "/api/insights/**",
                                "/api/servicos/**", "/api/consultores/**", "/api/importacoes/**")
                        .hasAnyRole("ADMIN", "ANALISTA")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> responderErro(res, HttpStatus.UNAUTHORIZED,
                                "Sessão inválida ou expirada. Entre novamente."))
                        .accessDeniedHandler((req, res, e) -> responderErro(res, HttpStatus.FORBIDDEN,
                                "Seu papel não permite esta operação.")))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void responderErro(HttpServletResponse res, HttpStatus status, String mensagem) throws IOException {
        res.setStatus(status.value());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"status\":" + status.value() + ",\"mensagem\":\"" + mensagem + "\"}");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
