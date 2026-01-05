package com.secureteam.auth;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import dev.paseto.jpaseto.Paseto;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityFilter implements ContainerRequestFilter {

    @Inject
    private PasetoService pasetoService;

    @Inject
    private AbacPolicyEngine abacPolicyEngine;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // SecureTeam Access: Allow public access to health and authentication setup
        if (path.contains("/auth/")) {
            return;
        }

        String authHeader = requestContext.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 1. Verify Token (Signature + Replay Prevention + Expiry)
            // Enforce audience "api.yourdomain.me" for internal API
            Paseto paseto = pasetoService.validatePublicToken(token, "api.yourdomain.me");

            // 2. Extract Claims for ABAC (SecureTeam Access Implementation)
            Map<String, Object> subject = new HashMap<>();
            subject.put("subject", paseto.getClaims().getSubject());
            subject.put("department", paseto.getClaims().get("dept", String.class));
            subject.put("access_expiry", paseto.getClaims().get("access_expiry", Long.class));
            subject.put("projects", paseto.getClaims().get("projects", java.util.List.class));
            subject.put("device_id", paseto.getClaims().get("device_id", String.class));

            Map<String, Object> resource = new HashMap<>();
            resource.put("type", "secure_resource");
            // Extract project_id from header or URL path
            String projectId = requestContext.getHeaderString("X-Project-ID");
            resource.put("project_id", projectId);

            Map<String, Object> env = new HashMap<>();
            // Extract real-time device ID from header for comparison
            env.put("device_id", requestContext.getHeaderString("X-Device-Fingerprint"));

            // 3. Enforce ABAC (Temporal & Context-Aware Decision)
            if (!abacPolicyEngine.evaluate(subject, resource, env)) {
                requestContext
                        .abortWith(Response.status(Response.Status.FORBIDDEN)
                                .entity("SecureTeam Access Denial: Context or Time Constraint Violated").build());
            }

        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build());
        }
    }
}
