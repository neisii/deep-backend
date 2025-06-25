package org.example.backendproject.security.core;

import lombok.RequiredArgsConstructor;
import org.example.backendproject.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    // UserDetails <- 사용자 정보를 담는 인터페이스
    // 로그인한 사용자의 정보를 담아두는 역할
    
    private final User user;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // User의 권한을 반환
        return Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())); // Collections.singleton: 이 사용자는 권한이 한가지만 갖는다.
    }
    
    // 토큰에서 추출한 사용자 정보의 ID를 반환(테이블의 PK 값)
    // User Entity에서 ID 추출
    public Long getId() {
        return user.getId();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() { // 이름이 아니라 사용자를 식별할 수 있는 값을 의미함
        return user.getUserid(); // userID 중복되지 않은 값
    }


    // 아래는 현재 계정 상태를 관리하는 메서드
    @Override // 계정 활성화 여부
    public boolean isEnabled() {
        return true;
    }

    @Override // 계정 만료 여부
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override // 계정 잠김 여부
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override // 자격증명 만료 여부
    public boolean isCredentialsNonExpired() {
        return true;
    }


}

