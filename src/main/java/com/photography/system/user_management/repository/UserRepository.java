package com.photography.system.user_management.repository;

import com.photography.system.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByRole_Name(String roleName);
    long countByRole_Name(String roleName);
}