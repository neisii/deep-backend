package org.example.backendproject.auth.repository;

import org.example.backendproject.auth.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRespository extends JpaRepository<Auth, Long> {
//    public AuthRespository findByUserId(String userId);
}
