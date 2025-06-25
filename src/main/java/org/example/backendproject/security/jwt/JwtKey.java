package org.example.backendproject.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtKey {

    @Value("${jwt.secretKey}")
    private String secretKey; // 반드시 512byte 이상이어야 함.

    
    // 서명키를 만들어서 반환
    @Bean
    public SecretKey secretKey() {
        byte[] keyBytes = secretKey.getBytes();
        return new SecretKeySpec(keyBytes, "HmacSHA256"); // 바이트 배열을 HmacSHA256용 Secutity 객체로 매핑

    }


}
