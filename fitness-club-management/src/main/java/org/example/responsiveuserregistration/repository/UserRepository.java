package org.example.responsiveuserregistration.repository;

import org.example.responsiveuserregistration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsernameOrEmail(String username, String email);
    Optional<User> findByUsername(String username);
    List<User> findByRolesContaining(String role);

}
