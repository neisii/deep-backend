package org.example.backendproject.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backendproject.auth.dto.LoginRequestDTO;
import org.example.backendproject.auth.dto.LoginResponseDTO;
import org.example.backendproject.auth.dto.SignUpRequestDTO;
import org.example.backendproject.auth.entity.Auth;
import org.example.backendproject.auth.repository.AuthRespository;
import org.example.backendproject.security.core.CustomUserDetails;
import org.example.backendproject.security.core.Role;
import org.example.backendproject.security.jwt.JwtTokenProvider;
import org.example.backendproject.user.entity.User;
import org.example.backendproject.user.entity.UserProfile;
import org.example.backendproject.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRespository authRespository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.accessTokenExpirationTime}")
    private Long jwtAccessTokenExpirationTime;
    @Value("${jwt.refreshTokenExpirationTime}")
    private Long jwtRefreshTokenExpirationTime;


    @Transactional
    public void signUp(SignUpRequestDTO dto) {

        if (userRepository.findByUserid(dto.getUserid()).isPresent()) {
            throw new RuntimeException("사용자가 이미 존재합니다.");
        }

        User user = new User();

        user.setUserid(dto.getUserid());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // 암호화하여 저장
        user.setRole(Role.ROLE_USER);

        UserProfile userProfile = new UserProfile();
        userProfile.setUsername(dto.getUsername());
        userProfile.setPhone(dto.getPhone());
        userProfile.setAddress(dto.getAddress());
        userProfile.setEmail(dto.getEmail());

        // 연관 관계 설정
        userProfile.setUser(user);
        user.setUserProfile(userProfile);

        userRepository.save(user);
    }

    
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) { // findByUserid -> login
        User user = userRepository.findByUserid(loginRequestDTO.getUserid())
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없음."));

        // 입력한 비밀번호가 암호화된 비밀번호와 일치하는지 확인
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않음.");
            // 시큐리티 로그인 과정에서 비밀번호가 일치하지 않으면 던져주는 예외
        }

        // 상기 비밀번호가 일치하면 기존 토큰 정보를 비교하고 토큰 정보가 일치하면 업데이트, 불일치 시 새로 발급
        String accessToken = jwtTokenProvider.generateToken(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), user.getPassword())
                , jwtAccessTokenExpirationTime);

        String refreshToken = jwtTokenProvider.generateToken(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), user.getPassword())
                , jwtRefreshTokenExpirationTime);

        // 현재 로그인한 사람이 디비에 있는지 확인
        if (authRespository.existsByUser(user)) {
            Auth auth = user.getAuth();
            auth.setRefreshToken(refreshToken);
            auth.setAccessToken(accessToken);
            authRespository.save(auth);

            return new LoginResponseDTO(auth);
        }

        // 위에서 디비에 사용자 정보가 없으면 아래와 같이 새로 생성해서 로그인 처리
        Auth auth = new Auth(user, refreshToken, accessToken, "Bearer");
        authRespository.save(auth);
        return new LoginResponseDTO(auth);
    }
}
