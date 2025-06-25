package org.example.backendproject.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backendproject.user.entity.User;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tokenType;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private String refreshToken;

    @OneToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "user_id") // auth.getUser()에 실제 접근 시 User 쿼리 발생
    private User user;


    public Auth(User user, String refreshToken, String accessToken, String tokenType) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    // updateAccessToken 메서드 추가
    //토큰값을 업데이트 해주는 메서드
    public void updateAccessToken(String newAccessToken) {
        this.accessToken = newAccessToken;
    }

    // updateRefreshToken 메서드 추가
    public void updateRefreshToken(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
    }
}
