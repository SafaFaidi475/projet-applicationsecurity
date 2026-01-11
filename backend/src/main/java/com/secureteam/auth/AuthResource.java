package com.secureteam.auth;

import com.secureteam.model.User;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.PermitAll;
import java.util.HashMap;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @GET
    @Path("/health")
    @PermitAll
    public Response health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "SecureTeam Access Engine is running.");
        return Response.ok(status).build();
    }

    @Inject
    private TotpService totpService;

    @Inject
    private PasetoService pasetoService;

    @Inject
    private RedisClient redisClient;

    @GET
    @Path("/mfa/setup")
    @PermitAll
    public Response setupMfa(@QueryParam("username") String username) {
        String secret = totpService.generateSecret();

        // Store secret in Redis for 10 minutes (TTL for setup)
        String key = "mfa_secret_" + username;
        redisClient.setEx(key, 600, secret);

        String qrUri = totpService.generateQrCodeUri(secret, username, "SecureTeamAccess");
        String qrImage = totpService.generateQrCodeImage(qrUri);

        Map<String, String> response = new HashMap<>();
        response.put("secret", secret);
        response.put("qrUri", qrUri);
        response.put("qrImage", "data:image/png;base64," + qrImage);

        return Response.ok(response).build();
    }

    @POST
    @Path("/mfa/verify")
    @PermitAll
    public Response verifyMfa(MfaVerificationRequest request) {
        String key = "mfa_secret_" + request.getUsername();
        String storedSecret = redisClient.get(key);

        if (storedSecret == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("MFA session expired or not found").build();
        }

        boolean isValid = totpService.validateCode(storedSecret, request.getCode());

        if (isValid) {
            try {
                String token = pasetoService.createPublicToken(request.getUsername(), "secureteam-web",
                        "DEVICE-ID-123");
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                return Response.ok(response).build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Security Engine Error: Token Generation Failed").build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("MFA Invalid").build();
        }
    }

    @Inject
    private jakarta.persistence.EntityManager em;

    @POST
    @Path("/register")
    @PermitAll
    @jakarta.transaction.Transactional
    public Response register(User user) {
        // Enforce department for ABAC
        if (user.getDepartment() == null) {
            user.setDepartment("engineering");
        }
        em.persist(user);
        return Response.status(Response.Status.CREATED).entity("User registered successfully").build();
    }

    public static class MfaVerificationRequest {
        private String username;
        private String code;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
