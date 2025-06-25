package org.example.backendproject.user.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backendproject.user.dto.UserDTO;
import org.example.backendproject.user.dto.UserProfileDTO;
import org.example.backendproject.user.entity.User;
import org.example.backendproject.user.entity.UserProfile;
import org.example.backendproject.user.repository.UserProfileRepository;
import org.example.backendproject.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final EntityManager em;

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
            if (dtoProfile.getEmail() != null) profile.setEmail(dtoProfile.getEmail());
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

        userRepository.save(user);

        return dto;
    }



    //아래는 순환참조가 되는  예제
    public User getProfile2(Long profileId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(()->new RuntimeException("프로필 없음"));

        return profile.getUser();
    }


    //dto로 순환참조 방지
    public UserDTO getProfile(Long profileId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(()->new RuntimeException("프로필 없음"));

        User user =profile.getUser();
        if (user==null) throw new RuntimeException("연결된 유저 없음");

        UserProfileDTO profileDTO = new UserProfileDTO(
                profile.getUsername(),
                profile.getEmail(),
                profile.getPhone(),
                profile.getAddress()
        );

        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getUserid(),
                profileDTO
        );

        return userDTO;
    }



    @Transactional
    public void saveAllUsers(List<User> users) {
        long start = System.currentTimeMillis();

        for (int i = 0; i<users.size(); i++) {
            em.persist(users.get(i));
            if (i % 1000 == 0){
                em.flush();
                em.clear();
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("JPA saveAll 저장 소요 시간(ms): " + (end - start));
    }

}
