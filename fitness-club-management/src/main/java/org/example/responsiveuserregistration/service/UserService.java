package org.example.responsiveuserregistration.service;

import jakarta.annotation.PostConstruct;
import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        createDefaultAdmin();
    }

    public void registerUser(String username, String email, String password){

        username = username.trim().toLowerCase();
        email = email.trim().toLowerCase();

        if (userRepository.existsByUsernameOrEmail(username, email)) {
            throw new IllegalArgumentException("User already exists");
        }

        String hashedPassword = passwordEncoder.encode(password);
        logger.info("Hashed password: {}", hashedPassword);

        User user = new User(null, username, email, hashedPassword, Set.of("USER"));
        userRepository.save(user);
    }

    // Create a default admin user if one does not exist
    private void createDefaultAdmin() {
        String adminUsername = "admin";
        String adminEmail = "admin@admin.com";
        String adminPassword = "admin123";

        Optional<User> adminUser = userRepository.findByRolesContaining("ADMIN");
        if (adminUser.isEmpty()) {
            String hashedPassword = passwordEncoder.encode(adminPassword);
            User admin = new User(null, adminUsername, adminEmail, hashedPassword, Set.of("ADMIN", "USER"));
            userRepository.save(admin);
            logger.info("Admin user created");
        } else {
            logger.info("Admin user already exists");
        }
    }

    // Update password for a user, admin123 is not a very secure password lol
    public void updatePassword(String username, String newPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String hashedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            logger.info("Password updated for user: {}", username);
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }


    // For authentication
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Try to find user by given username
        Optional<User> userOptional = userRepository.findByUsername(username);

        // If user is found, return user details from optional to Spring Security
        if(userOptional.isPresent()) {
            User user = userOptional.get(); // creates a user details object
            Set<String> roles = user.getRoles(); // get role

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername()) // Set username
                    .password(user.getPassword()) // Set password
                    .roles(roles.toArray(new String[0])) // Set role
                    .build();
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

}
