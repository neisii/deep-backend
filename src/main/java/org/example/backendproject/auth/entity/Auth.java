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
}
