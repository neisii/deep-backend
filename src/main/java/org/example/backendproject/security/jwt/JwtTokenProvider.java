package org.example.backendproject.security.jwt;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.example.backendproject.security.core.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    /* JWT 토큰 생성 및 추출을 검증하는 클래스 */
    private final SecretKey secretKey; // 토큰 생성 시 서명하는 키

    // 현재 로그인된 사용자 정보를 기반으로 access, refresh token 발급
    public String generateToken(Authentication authentication, Long expiirationMills) {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Date expiryDate = new Date(new Date().getTime() + expiirationMills); // 토큰 만료시간 생성

        Claims claims = Jwts.claims();
        claims.put("user-id", customUserDetails.getId());
        claims.put("username", customUserDetails.getUsername());

        return Jwts.builder()
                .setSubject(customUserDetails.getUsername()) // 토큰 주체
                .setClaims(claims) // payload
                .setIssuedAt(new Date()) // 토큰 발급 시간
                .setExpiration(expiryDate) // 토큰 만료 시간
                .signWith(secretKey, SignatureAlgorithm.HS512) // 시크릿키와 알고리즘으로 암호화
                .compact(); // 최종적으로 문자열로 만듦을 의미
    }

    // JWT 토큰에서 사용자 ID를 추출
    public Long getUserIdFromToken(String token) {
        return Jwts.parserBuilder() // JWT 토큰을 해석하겠다고 선언
                .setSigningKey(secretKey) // 토큰 검증위해 시크릿 키 사용
                .build() // 해석 준비 완료
                .parseClaimsJws(token) // 전달 받은 토큰 파싱 (parseClaimsJws: 서명이 포함된 토큰 / parseClaimsJwt: 서명이 없는 토큰(키를 동적으로 찾아야 할 때))
                .getBody() // 파싱한 토큰의 payload 부분을 꺼내서 
                .get("user-id", Long.class); // user-id를 반환
    }

    // 토큰이 유효한지 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;

        } catch (MalformedJwtException e) {
            // 잘못된 토큰 형식
            return false;
        } catch (ExpiredJwtException e) {
            // 만료된 토큰
            return false;
        } catch (UnsupportedJwtException e) {
            // 미지원 토큰
            return false;
        } catch (IllegalArgumentException e) {
            // 토큰 문자열이 비어있거나 이상할 때
            return false;
        } catch (JwtException e) {
            // 기타 예외
            return false;
        }
    }
    
}
