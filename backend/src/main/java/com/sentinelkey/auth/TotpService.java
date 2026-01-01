package com.sentinelkey.auth;

import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TotpService {

    /**
     * Generates a new Base32 secret for TOTP.
     */
    public String generateSecret() {
        return Base32.random();
    }

    /**
     * Validates a TOTP code against the user's secret.
     */
    public boolean validateCode(String secret, String code) {
        try {
            Totp totp = new Totp(secret);
            return totp.verify(code);
        } catch (Exception e) {
            // Log 2FA failure
            return false;
        }
    }

    // Note: QR Code generation would typically happen here or in a separate utility
    // using ZXing, returning a byte[] or base64 image.
}
