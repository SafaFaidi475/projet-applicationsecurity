package com.secureteam.auth;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

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

    @Inject
    private com.secureteam.api.AuditResource auditResource;

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
            // 1. Verify Token and Extract Claims
            Map<String, Object> subject = pasetoService.verifyPublicToken(token);

            // 2. Prepare Context for ABAC
            Map<String, Object> resource = new HashMap<>();
            resource.put("path", path);
            String projectId = requestContext.getHeaderString("X-Project-ID");
            if (projectId != null)
                resource.put("project_id", projectId);

            Map<String, Object> env = new HashMap<>();
            env.put("device_id", requestContext.getHeaderString("X-Device-ID"));
            env.put("ip", requestContext.getHeaderString("X-Forwarded-For"));

            // 3. Enforce ABAC
            if (!abacPolicyEngine.evaluate(subject, resource, env)) {
                auditResource.logAccess((String) subject.get("subject"), path, false, "ABAC Policy Violation");
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("SecureTeam Access Denial: Zero Trust Policy Violation").build());
                return;
            }

            auditResource.logAccess((String) subject.get("subject"), path, true, "Authorized Access");

        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build());
        }
    }
}
