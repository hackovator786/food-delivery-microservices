package com.hackovation.menu_service.util;

import com.hackovation.menu_service.exception.AuthFilterException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

@Component
public class AccessTokenUtils {
    private static final Logger logger = LoggerFactory.getLogger(AccessTokenUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSigningSecret;

    @Value("${spring.app.jwtEncryptionSecret}")
    private String jwtEncryptSecret;

    public String getAccessTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public JWTClaimsSet validateAccessToken(String authToken) throws Exception {
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
            throw new AuthFilterException("Invalid JWT Token");
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
