package org.example.backendproject.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.backendproject.auth.dto.LoginRequestDTO;
import org.example.backendproject.auth.dto.LoginResponseDTO;
import org.example.backendproject.auth.dto.SignUpRequestDTO;
import org.example.backendproject.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequestDTO signUpRequestDTO) {
        try {
            authService.signUp(signUpRequestDTO);
            return ResponseEntity.ok("회원가입 성공");
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/loginSecurity")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loginResponseDTO = authService.login(loginRequestDTO);
//        authService.login(loginRequestDTO);

        return ResponseEntity.ok(loginResponseDTO);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestHeader(value = "Authorization", required = false)
                                                   String authorizationHeader, HttpServletRequest request) {

        String refreshToken = null;
        // 쿠키에서 찾기
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null && authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            refreshToken = authorizationHeader.replace("Bearer ", "").trim();
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("리프레시 토큰이 없음");
        }


        String newAccessToken = authService.refreshToken(refreshToken);
        HashMap<String, String> res = new HashMap<>();
        res.put("accessToken", newAccessToken);
        res.put("refreshToken", refreshToken);

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

}
