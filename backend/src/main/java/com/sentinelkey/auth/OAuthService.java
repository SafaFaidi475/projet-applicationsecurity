package com.sentinelkey.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OAuthService {

    @Inject
    private RedisClient redisClient;

    /**
     * Generates a cryptographically secure CSRF state token.
     */
    public String generateState(String sessionId) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] seed = new byte[32];
        secureRandom.nextBytes(seed);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(seed);

        // Store in Redis with 10 min TTL (Strict Requirement)
        redisClient.setEx("oauth:state:" + sessionId, 600, state);
        return state;
    }

    /**
     * Validates the state parameter to prevent CSRF.
     */
    public boolean validateState(String sessionId, String receivedState) {
        String expectedState = redisClient.get("oauth:state:" + sessionId);
        if (expectedState == null)
            return false;

        // Constant-time comparison
        return MessageDigest.isEqual(
                receivedState.getBytes(StandardCharsets.UTF_8),
                expectedState.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates PKCE Code Verifier against Code Challenge.
     * Enforces S256 (SHA-256) method.
     */
    public boolean validatePkce(String codeVerifier, String codeChallenge) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            String calculatedChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            return calculatedChallenge.equals(codeChallenge);
        } catch (Exception e) {
            return false;
        }
    }
}
