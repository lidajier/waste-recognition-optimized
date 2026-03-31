package com.wasteai.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wasteai.config.AppProperties;
import com.wasteai.domain.UserEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AppProperties properties;

    public JwtService(AppProperties properties) {
        this.properties = properties;
    }

    public String createToken(UserEntity user) {
        try {
            long now = Instant.now().getEpochSecond();
            long exp = now + properties.getAuth().getTokenTtlSeconds();
            String header = URL_ENCODER.encodeToString(OBJECT_MAPPER.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
            String payload = URL_ENCODER.encodeToString(OBJECT_MAPPER.writeValueAsBytes(Map.of(
                    "sub", user.getId().toString(),
                    "username", user.getUsername(),
                    "role", user.getRole(),
                    "iat", now,
                    "exp", exp
            )));
            String signature = sign(header + "." + payload);
            return header + "." + payload + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create token.", e);
        }
    }

    public UUID parseUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token.");
            }
            String data = parts[0] + "." + parts[1];
            String expected = sign(data);
            if (!expected.equals(parts[2])) {
                throw new IllegalArgumentException("Invalid token signature.");
            }
            Map<String, Object> payload = OBJECT_MAPPER.readValue(URL_DECODER.decode(parts[1]), MAP_TYPE);
            Number exp = (Number) payload.get("exp");
            if (exp == null || exp.longValue() < Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("Token expired.");
            }
            return UUID.fromString((String) payload.get("sub"));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token.", e);
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(properties.getAuth().getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        return URL_ENCODER.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
