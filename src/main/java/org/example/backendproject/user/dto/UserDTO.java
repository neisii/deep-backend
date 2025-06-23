package org.example.backendproject.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String userid;
    private String username;
    private String phone;
    private String address;
    private String email;
}
