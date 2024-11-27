package org.example.responsiveuserregistration.service;

import jakarta.annotation.PostConstruct;
import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.payload.UpdateUsernameRequest;
import org.example.responsiveuserregistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @PostConstruct // This method will be called after the bean has been created
    public void init() {
        createDefaultAdmin();
    }

    // We love getting rid of redundancy
    public User getUserById(Long userId) {
        Optional<User> userOptional= userRepository.findById(userId);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    public User getUserByUsername(String username) {
        Optional<User> userOptional= userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findUserByRoleOrderedByUsername(role);
    }

    public List<User> getAllUsers() {
        return userRepository.findAllOrderedByUsername();
    }


    // Create a default admin user if one does not exist - called at startup
    private void createDefaultAdmin() {
        String adminUsername = "admin";
        String adminEmail = "admin@admin.com";
        String adminPassword = "admin123"; // Great password, very secure

        List<User> adminUser = userRepository.findByRolesContaining("ADMIN"); // DOES AN ADMIN EXIST?????
        if (adminUser.isEmpty()) {
            String hashedPassword = passwordEncoder.encode(adminPassword);
            User admin = new User(adminUsername, adminEmail, hashedPassword, Set.of("Admin", "User")); // Create new user, add big bad admin status
            userRepository.save(admin);
            logger.info("Admin user created");
        } else {
            logger.info("Admin user already exists");
        }
    }

    public void updateUserRole(Long userId, String trainerRole) {
        User user = getUserById(userId);
        Set<String> roles = user.getRoles(); // Get the roles, not to be confused with rolls (mmmmm)
        if (trainerRole != null) { // (if the checkbox is checked on front end)
            roles.add("TRAINER");
        } else {
            roles.remove("TRAINER");
        }
        user.setRoles(roles); // updates the roles
        userRepository.save(user);
    }

    public void deleteUser(Long userId, String currentUsername) {
        User user = getUserById(userId);
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("Cannot delete yourself"); // just in case something goes buck wild (confidence lvl 100)
        }
        userRepository.delete(user);

    }

    public void updateUsername(Long userId, UpdateUsernameRequest request) {
        String username = request.getUsername().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = getUserById(userId);
        user.setUsername(username);
        userRepository.save(user);
    }

    public void updateEmail(Long userId, String newEmail) {
        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = getUserById(userId);
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    // For authentication (spring security moment)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsername(username); // creates a user details object
        Set<String> roles = user.getRoles(); // get role

        // Build and return the object with the users information
        // This is a spring security thing, don't worry about it too much
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles.toArray(new String[0]))
                .build();

    }

}
