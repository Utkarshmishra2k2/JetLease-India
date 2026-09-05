package com.jetlease.repository;

import com.jetlease.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmailAndPasswordAndRole(String email, String password, String role);
    Optional<User> findByPhoneAndPasswordAndRole(String phone, String password, String role);
    Optional<User> findByEmailAndRole(String email, String role);
    Optional<User> findByPhoneAndRole(String phone, String role);
    List<User> findByRole(String role);
}
