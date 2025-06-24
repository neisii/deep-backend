package org.example.backendproject.user.service;

import lombok.RequiredArgsConstructor;
import org.example.backendproject.user.dto.UserDTO;
import org.example.backendproject.user.dto.UserProfileDTO;
import org.example.backendproject.user.entity.User;
import org.example.backendproject.user.entity.UserProfile;
import org.example.backendproject.user.repository.UserProfileRepository;
import org.example.backendproject.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public UserDTO getMyInfo(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUserid(user.getUserid());

        UserProfile profile = user.getUserProfile();

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setUsername(profile.getUsername());
        profileDTO.setPhone(profile.getPhone());
        profileDTO.setAddress(profile.getAddress());
        profileDTO.setEmail(profile.getEmail());

        dto.setProfile(profileDTO);

        return dto;
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        UserProfile profile = user.getUserProfile(); // 변경 전 프로필


        if (profile != null && userDto.getProfile() != null) { // 전후 데이터 있는지 체크
            UserProfileDTO dtoProfile = userDto.getProfile(); // 덮어 씌울 데이터

            // 전달 받은 데이터로 변경
            if (dtoProfile.getUsername() != null) profile.setUsername(dtoProfile.getUsername());
            if (dtoProfile.getPhone() != null) profile.setPhone(dtoProfile.getPhone());
            if (dtoProfile.getAddress() != null) profile.setAddress(dtoProfile.getAddress());
        }

        UserDTO dto = new UserDTO();
        dto.setId(id);
        dto.setUserid(user.getUserid());

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setUsername(profile.getUsername());
        profileDTO.setPhone(profile.getPhone());
        profileDTO.setAddress(profile.getAddress());
        profileDTO.setEmail(profile.getEmail());

        dto.setProfile(profileDTO);

        // userRepository.save(user);

        return dto;
    }
}
