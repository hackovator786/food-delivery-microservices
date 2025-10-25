package com.hackovation.authservice.util;

import com.hackovation.authservice.exception.AuthException;
import com.hackovation.authservice.service.CustomUserDetails;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.text.ParseException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class AccessTokenUtils {
    private static final Logger logger = LoggerFactory.getLogger(AccessTokenUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSigningSecret;

    @Value("${spring.app.jwtEncryptionSecret}")
    private String jwtEncryptSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String getAccessTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String generateAccessToken(CustomUserDetails userDetails, Object... keyValues) throws Exception {
        String userId = userDetails.getUserId();
        JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder()
                .subject(userId)
                .issueTime(new Date())
                .expirationTime(new Date(new Date().getTime() + jwtExpirationMs));

        for (int i = 0; i < keyValues.length; i += 2) {
            String key = keyValues[i].toString();
            Object value = keyValues[i + 1];
            claimsSetBuilder.claim(key, value);
        }

        JWTClaimsSet claimsSet = claimsSetBuilder.build();

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

    @Deprecated
    public String getUserIdFromAccessToken(String token) throws Exception {
        return validateAccessToken(token).getSubject();
    }

    @Deprecated
    public Integer getRoleFromAccessToken(String token) throws Exception {
        return validateAccessToken(token).getIntegerClaim("role");
    }

    public JWTClaimsSet validateAccessToken(String authToken) throws Exception {
        try {
            JWSObject jws = JWSObject.parse(authToken);

            if (!jws.verify(new MACVerifier(hmacKey()))) {
                throw new SecurityException("Invalid signature");
            }

            EncryptedJWT jwe = EncryptedJWT.parse(jws.getPayload().toString());
            jwe.decrypt(new DirectDecrypter(aesKey()));

//            Date now = new Date();
//            if (jwe.getJWTClaimsSet().getExpirationTime().before(now)) {
//                throw new SecurityException("Token expired");
//            }

            return jwe.getJWTClaimsSet();
        } catch (ParseException | JOSEException |
                 IllegalArgumentException | NullPointerException | SecurityException e){
            throw new AuthException("Invalid JWT Token", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            throw new Exception("Unknown Error has occurred");
        }
    }


    private SecretKeySpec aesKey() {
        return new SecretKeySpec(jwtEncryptSecret.getBytes(StandardCharsets.UTF_8), "AES");
    }

    private SecretKeySpec hmacKey() {
        return new SecretKeySpec(jwtSigningSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

}
