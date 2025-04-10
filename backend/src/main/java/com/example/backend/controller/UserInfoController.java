package com.example.backend.controller;

import com.example.backend.dto.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing user-related endpoints.
 * <p>
 * Implements:
 * - GET /private/userinfo – returns information about the current authenticated user.
 */
@RestController
public class UserInfoController {

    private static final Logger log = LoggerFactory.getLogger(UserInfoController.class);

    /**
     * GET /private/userinfo
     * Returns information of the current authenticated user, extracting name and roles
     * from the JWT token provided by Spring Security.
     *
     * @param jwt The JWT object representing the authenticated user's token.
     * @return A UserInfo object containing the user's name and roles.
     */
    @GetMapping("/private/userinfo")
    public UserInfo getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            log.warn("Attempted to access /private/userinfo without a valid JWT principal.");
            return new UserInfo("anonymous", Collections.emptyList());
        }

        String username = jwt.getClaimAsString("preferred_username");
        if (username == null) {
            username = jwt.getSubject();
        }


        List<String> roles = extractRolesFromJwt(jwt);

        log.debug("User Info requested for: {}, Roles found: {}", username, roles);

        return new UserInfo(username, roles);
    }

    /**
     * Helper method to extract roles from the JWT.
     * Keycloak might place roles in different claims depending on configuration
     * (e.g., top-level 'role', 'realm_access.roles', 'resource_access.<client-id>.roles').
     * This method checks common locations based on the requirement for a "role" claim.
     *
     * @param jwt The JWT token.
     * @return A list of roles extracted from the token, or an empty list if none found.
     */
    private List<String> extractRolesFromJwt(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("role");
        if (roles != null) {
            log.trace("Found roles in direct 'role' claim list: {}", roles);
            return roles;
        }
        String singleRole = jwt.getClaimAsString("role");
        if (singleRole != null) {
            log.trace("Found single role in direct 'role' claim string: {}", singleRole);
            return Collections.singletonList(singleRole);
        }

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List) {
            try {
                List<?> rawRoles = (List<?>) realmAccess.get("roles");
                roles = rawRoles.stream().map(Object::toString).toList();
                if (!roles.isEmpty()) {
                    log.trace("Found roles in 'realm_access.roles': {}", roles);
                    return roles;
                }
            } catch (Exception e) {
                log.warn("Error casting roles from realm_access", e);
            }
        }

        log.trace("No roles found in standard claims ('role', 'realm_access.roles').");
        return Collections.emptyList();
    }
}

