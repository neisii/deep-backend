package org.example.backendproject.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.backendproject.user.dto.UserDTO;
import org.example.backendproject.user.entity.User;
import org.example.backendproject.user.service.UserService;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/me/{id}")
    public ResponseEntity<UserDTO> getMyInfo(@PathVariable("id") Long userId) {
        UserDTO myInfo = userService.getMyInfo(userId);
        return ResponseEntity.ok(myInfo);
    }

    @PutMapping("/me/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable("id") Long userId, @RequestBody UserDTO dto) {
        UserDTO updated = userService.updateUser(userId, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/jpaSaveAll")
    public String saveAll(@RequestBody List<User> users) {
        userService.saveAllUsers(users);
        return "ok";
    }
}
