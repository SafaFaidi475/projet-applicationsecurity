package com.sentinelkey.auth;

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

            // 2. Extract Claims for ABAC
            Map<String, Object> subject = new HashMap<>();
            subject.put("subject", paseto.getClaims().getSubject());
            // In reality, fetch roles/dept from DB using subject
            subject.put("department", "engineering"); // Mocked for this phase

            Map<String, Object> resource = new HashMap<>();
            resource.put("type", "audit_logs"); // Should be derived from URL path

            Map<String, Object> env = new HashMap<>();
            // Env populated in engine

            // 3. Enforce ABAC
            if (!abacPolicyEngine.evaluate(subject, resource, env)) {
                requestContext
                        .abortWith(Response.status(Response.Status.FORBIDDEN).entity("ABAC Policy Denial").build());
            }

        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build());
        }
    }
}
