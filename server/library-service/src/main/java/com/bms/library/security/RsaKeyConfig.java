package com.bms.library.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaKeyConfig {

    @Value("${jwt.public-key}")
    private Resource publicKeyResource;

    @Bean
    public RSAPublicKey rsaPublicKey() {

        try (InputStream inputStream =
                     publicKeyResource.getInputStream()) {

            String pem = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

            String encoded = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded =
                    Base64.getDecoder().decode(encoded);

            X509EncodedKeySpec keySpec =
                    new X509EncodedKeySpec(decoded);

            return (RSAPublicKey) KeyFactory
                    .getInstance("RSA")
                    .generatePublic(keySpec);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to load JWT public key",
                    e
            );
        }
    }
}
