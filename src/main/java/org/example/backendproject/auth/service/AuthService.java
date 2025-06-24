package org.example.backendproject.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backendproject.auth.dto.LoginRequestDTO;
import org.example.backendproject.auth.dto.SignUpRequestDTO;
import org.example.backendproject.user.dto.UserDTO;
import org.example.backendproject.user.dto.UserProfileDTO;
import org.example.backendproject.user.entity.User;
import org.example.backendproject.user.entity.UserProfile;
import org.example.backendproject.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;


    @Transactional
    public void signUp(SignUpRequestDTO dto) {

        if (userRepository.findByUserid(dto.getUserid()).isPresent()) {
            throw new RuntimeException("사용자가 이미 존재합니다.");
        }

        User user = new User();

        user.setUserid(dto.getUserid());
        user.setPassword(dto.getPassword());

        UserProfile userProfile = new UserProfile();
        userProfile.setUsername(dto.getUsername());
        userProfile.setPhone(dto.getPhone());
        userProfile.setAddress(dto.getAddress());
        userProfile.setEmail(dto.getEmail());

        userProfile.setUser(user);
        user.setUserProfile(userProfile);

        userRepository.save(user);

    }

    public UserDTO findByUserid(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByUserid(loginRequestDTO.getUserid())
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없음."));
        if (!loginRequestDTO.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않음.");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUserid(user.getUserid());

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setUsername(user.getUserProfile().getUsername());
        profileDTO.setPhone(user.getUserProfile().getPhone());
        profileDTO.setAddress(user.getUserProfile().getAddress());
        profileDTO.setEmail(user.getUserProfile().getEmail());

        userDTO.setProfile(profileDTO);
        return userDTO;
    }
}
