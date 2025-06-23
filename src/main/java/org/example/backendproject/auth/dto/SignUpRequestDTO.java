package org.example.backendproject.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * 순환 참조 방지 목적
 * */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDTO {
    private String userid;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String password;
}
