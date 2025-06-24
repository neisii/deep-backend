package org.example.backendproject.user.repository;

import org.example.backendproject.user.entity.User;
import org.example.backendproject.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    @Override
    Optional<UserProfile> findById(Long user_id);
}
