//package org.example.backendproject;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.example.backendproject.user.entity.User;
//import org.example.backendproject.user.entity.UserProfile;
//import org.example.backendproject.user.repository.UserRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class UserDataGenerator {
//
//    private final UserRepository userRepository;
//
//    @Transactional
//    public void generateSampleUsers(int count) {
//        List<User> users = new ArrayList<>();
//
//        for (int i = 1; i <= count; i++) {
//            // User 생성
//            User user = new User();
//            user.setUserid("user" + i);
//            user.setPassword("password" + i);
//
//            // UserProfile 생성
//            UserProfile profile = new UserProfile();
//            profile.setUsername("username" + i);
//            profile.setEmail("user" + i + "@example.com");
//            profile.setPhone("010-0000-" + String.format("%04d", i));
//            profile.setAddress("Seoul City " + i + "번지");
//
//            // 연관관계 설정
//            profile.setUser(user);
//            user.setUserProfile(profile);
//
//            users.add(user);
//
//            // flush/clear 튜닝 (옵션)
//            // if (i % 100 == 0) {
//            //     userRepository.saveAll(users);
//            //     users.clear();
//            // }
//        }
//
//        userRepository.saveAll(users);
//    }
//}
//
