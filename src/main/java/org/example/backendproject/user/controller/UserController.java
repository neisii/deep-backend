package org.example.backendproject.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.backendproject.security.core.CustomUserDetails;
import org.example.backendproject.user.dto.UserDTO;
import org.example.backendproject.user.entity.User;
import org.example.backendproject.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

//    @Value("${PROJECT_NAME:web Server}")
//    private String instansName;
//
//    @GetMapping
//    public String test() {
//        return instansName;
//    }

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getId();
        return ResponseEntity.ok(userService.getMyInfo(id));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserDTO userDTO) {
        Long id = userDetails.getId();
        UserDTO updated = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/jpaSaveAll")
    public String saveAll(@RequestBody List<User> users) {
        userService.saveAllUsers(users);
        return "ok";
    }
}
