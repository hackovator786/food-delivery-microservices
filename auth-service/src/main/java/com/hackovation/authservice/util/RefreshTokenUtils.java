package com.hackovation.authservice.util;

import com.hackovation.authservice.exception.AuthException;
import com.hackovation.authservice.exception.AuthFilterException;
import com.hackovation.authservice.service.CustomUserDetails;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

@Component
public class RefreshTokenUtils {
    private static final Logger logger = LoggerFactory.getLogger(AccessTokenUtils.class);

    @Value("${spring.app.jwtRefreshSecret}")
    private String jwtSigningRefreshSecret;

    @Value("${spring.app.jwtRefreshEncryptionSecret}")
    private String jwtRefreshEncryptSecret;

    @Value("${spring.app.jwtRefExpirationMs}")
    private int jwtRefreshExpirationMs;

    public String generateRefreshToken(CustomUserDetails userDetails, String refreshToken, Integer roleId) throws Exception {
        String userId = userDetails.getUserId();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId)
                .claim("refreshToken", refreshToken)
                .claim("role", roleId.toString())
                .issueTime(new Date())
                .expirationTime(new Date(new Date().getTime() + jwtRefreshExpirationMs))
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

    public String getUserIdFromRefToken(String token) throws Exception {
        return validateRefreshToken(token).getSubject();
    }

    public Integer getRoleIdFromRefToken(String token) throws Exception {
        return validateRefreshToken(token).getIntegerClaim("roleId");
    }

    public String getRawTokenFromRefToken(String token) throws Exception {
        return validateRefreshToken(token).getStringClaim("refreshToken");
    }

    public String hashRefreshToken(String token) {
        return BCrypt.hashpw(token, BCrypt.gensalt(12));
    }

    public Boolean verifyRefreshToken(String rawToken, String hashedToken) {
        return BCrypt.checkpw(rawToken, hashedToken);
    }

    private JWTClaimsSet validateRefreshToken(String authToken) throws Exception {
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
            throw new AuthException("Invalid JWT Token", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            throw new Exception("Unknown Error has occurred");
        }
    }


    private SecretKeySpec aesKey() {
        return new SecretKeySpec(jwtRefreshEncryptSecret.getBytes(StandardCharsets.UTF_8), "AES");
    }

    private SecretKeySpec hmacKey() {
        return new SecretKeySpec(jwtSigningRefreshSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
