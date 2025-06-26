package org.example.backendproject.security.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backendproject.oauth2.OAuth2LoginSuccessHandler;
import org.example.backendproject.oauth2.OAuth2LogoutSuccessHandler;
import org.example.backendproject.oauth2.OAuth2UserService;
import org.example.backendproject.oauth2.RedisOAuth2AuthorizationRequestRepository;
import org.example.backendproject.security.jwt.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    private final OAuth2LogoutSuccessHandler oAuth2LogoutSuccessHandler;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2UserService oAuth2UserService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new RedisOAuth2AuthorizationRequestRepository(redisTemplate);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable) // csrf: 사용자 모르게 악성 요청 보내는 것
                .authorizeHttpRequests((auth)
                        -> auth
                        // 인증 불필요 경로
                        .requestMatchers("/","/index.html", "/*.html", "/favicon.ico",
                                "/css/**", "/fetchWithAuth.js","/js/**", "/images/**",
                                "/.well-known/**", "/websocket.html").permitAll() // 정적 리소스 누구나 접근
                        .requestMatchers("/boards/**",  "/boards", "/api/auth/**", "/api/comments", "/api/comments/**", "/api/rooms", "/api/rooms/**", "/boards/elasticsearch").permitAll()
                        // 인증 필요 경로
                        .requestMatchers("/api/user/**").authenticated() //인증이 필요한 경로
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


                .oauth2Login(oauth2->oauth2
                        .loginPage("/index.html")
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)

                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(authorizationRequestRepository()))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oAuth2LogoutSuccessHandler)
                        .permitAll()
                )

                .build();
    }

    //회원가입시에 비밀번호를 암호화해주는 메서드
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
