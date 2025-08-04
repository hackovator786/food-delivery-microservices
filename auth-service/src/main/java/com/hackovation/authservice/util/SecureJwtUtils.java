package com.hackovation.authservice.util;

import com.hackovation.authservice.exception.AuthFilterException;
import com.hackovation.authservice.service.CustomUserDetails;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.text.ParseException;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class SecureJwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(SecureJwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSigningSecret;

    @Value("${spring.app.jwtEncryptionSecret}")
    private String jwtEncryptSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String generateTokenFromUserId(CustomUserDetails userDetails) throws Exception {
        String userId = userDetails.getUserId();
        System.out.println("User ID: " + userId);
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        System.out.println("User Role: " + role);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId)
                .claim("role", role)
                .issueTime(new Date())
                .expirationTime(new Date(new Date().getTime() + jwtExpirationMs))
                .build();

        EncryptedJWT jwe = new EncryptedJWT(
                new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                        .contentType("JWT").build(),
                claimsSet
        );

        jwe.encrypt(new DirectEncrypter(aesKey()));

        JWSObject jws = new JWSObject(
                new JWSHeader.Builder(JWSAlgorithm.HS256)
                        .contentType("JWT").build(),
                new Payload(jwe.serialize())
        );

        jws.sign(new MACSigner(hmacKey()));

        return jws.serialize();
    }

    public String getUserIdFromJwtToken(String token) throws AuthFilterException {
        return validateJwtToken(token).getSubject();
    }

    public String getRoleFromJwtToken(String token) throws AuthFilterException, ParseException {
        return validateJwtToken(token).getStringClaim("role");
    }

    private JWTClaimsSet validateJwtToken(String authToken)  throws AuthFilterException {
        try {
            JWSObject jws = JWSObject.parse(authToken);

            if (!jws.verify(new MACVerifier(hmacKey()))) {
                throw new SecurityException("Invalid signature");
            }

            EncryptedJWT jwe = EncryptedJWT.parse(jws.getPayload().toString());
            jwe.decrypt(new DirectDecrypter(aesKey()));

            Date now = new Date();
            if (jwe.getJWTClaimsSet().getExpirationTime().before(now)) {
                throw new SecurityException("Token expired");
            }

            return jwe.getJWTClaimsSet();
        } catch (ParseException | JOSEException |
                 IllegalArgumentException | NullPointerException | SecurityException e){
            throw new AuthFilterException("Invalid JWT Token", e.getCause() != null ? e.getCause() : e);
        } catch (Exception e) {
            throw new AuthFilterException("Unknown Error has occurred", e.getCause() != null ? e.getCause() : e);
        }
    }


    private SecretKeySpec aesKey() {
        return new SecretKeySpec(jwtEncryptSecret.getBytes(StandardCharsets.UTF_8), "AES");
    }

    private SecretKeySpec hmacKey() {
        return new SecretKeySpec(jwtSigningSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

}
