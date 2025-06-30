package org.example.backendproject.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backendproject.security.core.CustomUserDetailsService;
import org.example.backendproject.threadlocal.TraceIdHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    // 모든 HTTP 요청을 가로 채서 토큰 검사하는 필터
    // 요청 당 단 한번만 실행되는 필터역할

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        log.info("path : {}", path);

        // 정적 파일 경로, 필터
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/")
                || path.equals("/index.html")
                || path.endsWith(".html")
                || path.startsWith("/favicon.ico")
                || path.startsWith("/api/auth/");

        /**
         config에서 인증하라고하고 여기서 인증 무시하라고 하면 401 에러남
         **/
    }


    // 매 HTTP 요청마다 호출
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // HTTP 요청이 시작되는 구간에서 TraceId 발급
            String traceId = UUID.randomUUID().toString().substring(0, 8);
            TraceIdHolder.set(traceId);

            String accessToken = getTokenFromRequest(request);
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {

                UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(accessToken);
                // 토큰에서 사용자를 꺼내서 담은 사용자 인증 객체

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // HTTP 요청으로부터 부가정보(IP, Session 등)를 추출해서 사용자 인증 객체에 넣는다.

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                // 토큰에서 사용자 인증 정보를 조회한 인증 정보를 현재 스레드에 인증된 사용자로 등록


                String url = request.getRequestURI().toString();
                String method = request.getMethod(); // GET, PUT, POST ..


                log.info("현재 들어온 HTTP 요청 = {}", url);
                log.info("✅ 토큰 인증 성공:{} ", accessToken);

            } else {
                log.info("❌ 토큰 없음 또는 유효하지 않음: {}" ,accessToken);
            }

            filterChain.doFilter(request, response); // JwtTokenFilter를 거치고 다음 필터로 넘어감.

            // catch는 따로 하지 않음.
        } finally {
            TraceIdHolder.clear();
            String afterClear = TraceIdHolder.get();
            log.info("TraceIdHolder clear : {}", afterClear);

        }


        /*
        * 필터 종류
         CharacterEncodingFilter: 문자 인코딩 처리
         CorsFilter: CORS 정책 처리
         CsrfFilter: CSRF 보안 처리
         JWTTokenFilter: JWT 토큰 처리(핵심)
         SecurityContextFilter: 인증/인가 정보 저장
         ExceptionFilter: 예외 처리
        * */
    }

    // HTTP 요청 헤더에서 토큰을 추출
    public String getTokenFromRequest(HttpServletRequest request) {
        String token = null;

        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        }

        // 인증 정보에 없으면 쿠키 값 가져옴
        if (!StringUtils.hasText(bearerToken) && request.getCookies() != null) {
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        return token;
    }

    // HTTP 요청에서 사용자 인증 정보를 담는 객체
    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        // JWT 토큰에서 사용자 id 추출
        Long userid = jwtTokenProvider.getUserIdFromToken(token);

        // 추출한 id로 DB에서 사용자 정보 조회
        UserDetails userDetails = customUserDetailsService.loadUserById(userid);

        return new UsernamePasswordAuthenticationToken(
                userDetails
                ,null // 이미 인증된 정보여서 null 처리
                , userDetails.getAuthorities()
        );
    }
}
