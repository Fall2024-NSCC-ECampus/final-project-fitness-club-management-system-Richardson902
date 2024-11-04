package org.example.responsiveuserregistration.repository;

import org.example.responsiveuserregistration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsernameOrEmail(String username, String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByRolesContaining(String role);

}
