package com.wasteai.auth;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordService {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public String hash(String rawPassword, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), Base64.getDecoder().decode(salt), 65536, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return Base64.getEncoder().encodeToString(skf.generateSecret(spec).getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash password.", e);
        }
    }

    public boolean matches(String rawPassword, String salt, String expectedHash) {
        return hash(rawPassword, salt).equals(expectedHash);
    }
}
