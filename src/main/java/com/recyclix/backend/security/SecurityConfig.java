//package com.recyclix.backend.security;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableMethodSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final AuthenticationEntryPointImpl authenticationEntryPoint;
//    private final AccessDeniedHandlerImpl accessDeniedHandler;
//
//    // =========================================================================
//    // 1. CONFIGURATION POUR L'APPLICATION MOBILE (API REST - STATELESS / JWT)
//    // =========================================================================
//    @Bean
//    @Order(1)
//    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // Cette chaîne ne s'applique QUE si l'URL commence par /api/
//                .securityMatcher("/api/**")
//                .csrf(csrf -> csrf.disable())
//
//                // Pas de session côté serveur pour l'application mobile (utilisation des JWT)
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                // Gestion personnalisée des erreurs de connexion (401 / 403) pour l'API
//                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint(authenticationEntryPoint)
//                        .accessDeniedHandler(accessDeniedHandler)
//                )
//
//                .authorizeHttpRequests(auth -> auth
//                        // Endpoints publics pour l'authentification/inscription mobile
//                        .requestMatchers("/api/auth/**").permitAll()
//                        // Protection globale des autres endpoints de l'API
//                        .anyRequest().authenticated()
//                )
//
//                // Injection du filtre JWT avant le filtre standard d'authentification par login/password
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    // =========================================================================
//    // 2. CONFIGURATION POUR LE SITE WEB ADMIN (STATEFUL - SESSIONS & FORMULAIRE)
//    // =========================================================================
//    @Bean
//    @Order(2)
//    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // URLs capturées par cette chaîne (Pages d'admin, login, déconnexion et assets globaux)
//                .securityMatcher("/admin/**", "/login", "/logout", "/favicon.ico", "/css/**", "/js/**", "/images/**", "/assets/**", "/vendor/**")
//                .csrf(csrf -> csrf.disable()) // À activer en production via th:action (Thymeleaf gère les jetons automatiquement)
//
//                .authorizeHttpRequests(auth -> auth
//                        // Autoriser l'accès libre à la page de connexion et aux fichiers statiques (évite les erreurs 500/404)
//                        .requestMatchers("/login", "/favicon.ico", "/css/**", "/js/**", "/images/**", "/assets/**", "/vendor/**").permitAll()
//                        // Toutes les autres pages web (comme /admin/**) requièrent d'être connecté
//                        .anyRequest().authenticated()
//                )
//
//                // Configuration du formulaire de connexion web standard
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/admin/dashboard", true)
//                        .permitAll()
//                )
//
//                // Configuration de la déconnexion
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login?logout")
//                        .permitAll()
//                )
//
//                // Pour le site web, le serveur doit créer une session HTTP classique pour retenir l'utilisateur connecté
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
//
//        return http.build();
//    }
//
//    // =========================================================================
//    // 3. GESTIONNAIRE D'AUTHENTIFICATION GLOBAL
//    // =========================================================================
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
//        return cfg.getAuthenticationManager();
//    }
//}







package com.recyclix.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    // =========================================================================
    // 1. CONFIGURATION POUR L'APPLICATION MOBILE (API REST - STATELESS / JWT)
    // =========================================================================
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/public/**", "/api/ai/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // =========================================================================
    // 2. CONFIGURATION POUR LE SITE WEB (ADMIN + COMPTABLE) - STATEFUL
    // =========================================================================
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🔹 Ajout de /accountant/** dans les URLs gérées par cette chaîne
                .securityMatcher("/admin/**", "/accountant/**", "/login", "/logout",
                        "/favicon.ico", "/css/**", "/js/**", "/images/**", "/assets/**", "/vendor/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/favicon.ico", "/css/**", "/js/**", "/images/**",
                                "/assets/**", "/vendor/**").permitAll()
                        .anyRequest().authenticated()
                )
                // 🔹 Utilisation du handler personnalisé pour la redirection
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        return http.build();
    }

    // =========================================================================
    // 3. GESTIONNAIRE D'AUTHENTIFICATION GLOBAL
    // =========================================================================
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}