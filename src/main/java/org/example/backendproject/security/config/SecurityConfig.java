package org.example.backendproject.security.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backendproject.security.jwt.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable) // csrf: 사용자 모르게 악성 요청 보내는 것
                .authorizeHttpRequests((auth)
                        -> auth
                        .requestMatchers("/","/index.html", "/*.html", "/favicon.ico",
                                "/css/**", "/fetchWithAuth.js","/js/**", "/images/**",
                                "/.well-known/**").permitAll() // 정적 리소스 누구나 접근

                        .requestMatchers("/boards/**",  "/boards").permitAll()

                        .requestMatchers("/api/auth/**").permitAll() //인증이 필요한 경로
                )
                //인증 실패시 예외처리Add commentMore actions
                .exceptionHandling(e -> e
                        //인증이 안된 사용자가 접근하려고할 때
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        //인증은 되었지만 권한이 없을 때
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                        })
                )
                // 스프링 시큐리티에서 세션 관리 정책 성정

                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    //회원가입시에 비밀번호를 암호화해주는 메서드
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
