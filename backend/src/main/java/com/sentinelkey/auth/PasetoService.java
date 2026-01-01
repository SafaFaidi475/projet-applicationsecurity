package com.sentinelkey.auth;

import dev.paseto.jpaseto.Paseto;
import dev.paseto.jpaseto.PasetoParser;
import dev.paseto.jpaseto.Pasetos;
import dev.paseto.jpaseto.lang.Keys;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * PASETO Token Service for SentinelKey.
 * Implements PASETO v2 (Public/Local) as strictly required by 'jpaseto 0.7.x'.
 * Note: Spec mentions v4, but jpaseto 0.7.x primarily targets v1/v2. 
 * We use v2 Public (Ed25519) for asymmetric and v2 Local (XChaCha20) for symmetric to meet rigorous security standards.
 */
@ApplicationScoped
public class PasetoService {

    private final KeyPair keyPair; // For v2.public (Asymmetric)
    private final SecretKey secretKey; // For v2.local (Symmetric)

    @Inject
    private RedisClient redisClient;

    public PasetoService() {
        // In production, load these from Vault or Environment Variables via MicroProfile Config
        this.keyPair = Keys.keyPairFor(Pasetos.V2.PUBLIC); 
        this.secretKey = Keys.secretKeyFor(Pasetos.V2.LOCAL);
    }

    public String createPublicToken(String subject, String audience, String deviceId) {
        String jti = UUID.randomUUID().toString();
        
        return Pasetos.V2.PUBLIC.builder()
                .setPrivateKey(keyPair.getPrivate())
                .setSubject(subject)
                .setAudience(audience)
                .setIssuer("iam.yourdomain.me")
                .setExpiration(Instant.now().plus(1, ChronoUnit.HOURS))
                .claim("device_id", deviceId) // Device Fingerprint
                .claim("jti", jti) // Replay Prevention
                .compact();
    }

    public Paseto validatePublicToken(String token, String requiredAudience) {
        PasetoParser parser = Pasetos.parserBuilder()
                .setPublicKey(keyPair.getPublic())
                .requireAudience(requiredAudience)
                .requireIssuer("iam.yourdomain.me")
                .build();

        Paseto paseto = parser.parse(token);

        // Token Replay Prevention using Redis
        String jti = paseto.getClaims().get("jti", String.class);
        if (redisClient.exists("token:jti:" + jti)) {
            throw new SecurityException("Token Replay Detected! JTI: " + jti);
        }
        
        return paseto;
    }
}
