package org.example.backendproject.auth.repository;

import org.example.backendproject.auth.entity.Auth;
import org.example.backendproject.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthRespository extends JpaRepository<Auth, Long> {

    boolean existsByUser(User user);

    // 리프레시 토큰이 있는지 조회 쿼리77
    Optional<Auth> findByRefreshToken(String refreshToken);
}
