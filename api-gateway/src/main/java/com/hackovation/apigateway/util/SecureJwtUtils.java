package com.hackovation.apigateway.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;


import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

@Component
public class SecureJwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(SecureJwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSigningSecret;

    @Value("${spring.app.jwtEncryptionSecret}")
    private String jwtEncryptSecret;

//    public String getUserIdFromJwtToken(String token) throws RuntimeException {
//        return validateJwtToken(token).getSubject();
//    }
//
//    public String getRoleFromJwtToken(String token) throws ParseException {
//        return validateJwtToken(token).getStringClaim("role");
//    }

    public JWTClaimsSet validateJwtToken(String authToken)  throws RuntimeException {
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
            logger.error("Invalid JWT Token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT Token");
        } catch (Exception e) {
            logger.error("Unknown Error has occurred: {}", e.getMessage());
            throw new RuntimeException("Unknown Error has occurred");
        }
    }


    private SecretKeySpec aesKey() {
        return new SecretKeySpec(jwtEncryptSecret.getBytes(StandardCharsets.UTF_8), "AES");
    }

    private SecretKeySpec hmacKey() {
        return new SecretKeySpec(jwtSigningSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

}
