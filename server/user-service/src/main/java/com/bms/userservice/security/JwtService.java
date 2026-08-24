package com.bms.userservice.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final RSAKey rsaKey;

    public String generateToken(
            Long userId,
            String username,
            String role) {

        try {

            Date issuedAt = new Date();

            Date expiration = new Date(
                    issuedAt.getTime()
                            + 60 * 60 * 1000
            );

            JWTClaimsSet claims =
                    new JWTClaimsSet.Builder()
                            .subject(username)
                            .issuer("bms-user-service")
                            .audience("bms-library-service")
                            .claim("userId", userId)
                            .claim("role", role)
                            .issueTime(issuedAt)
                            .expirationTime(expiration)
                            .build();

            SignedJWT signedJWT =
                    new SignedJWT(
                            new com.nimbusds.jose.JWSHeader.Builder(
                                    JWSAlgorithm.RS256
                            )
                                    .keyID(rsaKey.getKeyID())
                                    .build(),
                            claims
                    );

            signedJWT.sign(
                    new RSASSASigner(
                            rsaKey.toRSAPrivateKey()
                    )
            );

            return signedJWT.serialize();

        } catch (JOSEException e) {

            throw new IllegalStateException(
                    "Unable to generate JWT",
                    e
            );
        }
    }
}