package com.example.base64service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring Security configuration for the Base64 Service.
 * Configures the application as an OAuth2 Resource Server, validates JWTs,
 * and restricts access to the /decode endpoint to users with the 'admin' role.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true)
public class SecurityConfig {

    /**
     * Configures the security filter chain for the Base64 service.
     *
     * @param http HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception If configuration fails.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers(HttpMethod.POST, "/decode").hasAuthority("ROLE_admin")
                                .anyRequest().denyAll()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    /**
     * Creates a custom JwtAuthenticationConverter to map JWT claims (especially roles)
     * to Spring Security GrantedAuthority objects.
     * Reuses the logic from Phase 1 backend.
     *
     * @return A configured JwtAuthenticationConverter.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return jwtConverter;
    }

    /**
     * Custom converter to extract roles from Keycloak JWT claims (e.g., 'realm_access.roles' or a 'role' claim)
     * and map them to Spring Security GrantedAuthority objects (prefixed with 'ROLE_').
     * Reused from Phase 1 implementation.
     */
    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<String> directRolesList = jwt.getClaimAsStringList("role");
            if (directRolesList != null) {
                return directRolesList.stream()
                        .map(roleName -> "ROLE_" + roleName)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
            String directRoleString = jwt.getClaimAsString("role");
            if (directRoleString != null && !directRoleString.isBlank()) {
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + directRoleString));
            }

            final Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("realm_access", Collections.emptyMap());
            final List<String> realmRoles = (List<String>) realmAccess.getOrDefault("roles", Collections.emptyList());

            if (!realmRoles.isEmpty()) {
                return realmRoles.stream()
                        .map(roleName -> "ROLE_" + roleName)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();
        }
    }
}