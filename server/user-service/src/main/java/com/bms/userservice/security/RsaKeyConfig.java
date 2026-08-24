package com.bms.userservice.security;

import com.nimbusds.jose.jwk.RSAKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaKeyConfig {

    @Value("${jwt.private-key}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key}")
    private Resource publicKeyResource;

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {

        String pem = readResource(privateKeyResource);

        String key = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded =
                Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(decoded);

        return (RSAPrivateKey)
                KeyFactory.getInstance("RSA")
                        .generatePrivate(spec);
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {

        String pem = readResource(publicKeyResource);

        String key = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded =
                Base64.getDecoder().decode(key);

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(decoded);

        return (RSAPublicKey)
                KeyFactory.getInstance("RSA")
                        .generatePublic(spec);
    }

    @Bean
    public RSAKey rsaKey(
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey) {

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("bms-key")
                .build();
    }

    private String readResource(Resource resource)
            throws Exception {

        try (InputStream is = resource.getInputStream()) {
            return new String(
                    is.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }
}