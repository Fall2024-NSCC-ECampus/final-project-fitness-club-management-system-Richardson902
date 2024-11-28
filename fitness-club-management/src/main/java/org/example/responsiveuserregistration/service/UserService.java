package org.example.responsiveuserregistration.service;

import jakarta.annotation.PostConstruct;
import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.payload.UserUpdateRequest;
import org.example.responsiveuserregistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct // This method will be called after the bean has been created
    public void init() {
        createDefaultAdmin();
    }

    private User getUserByField(String field, String value) {
        Optional<User> userOptional;
        if (field.equals("id")) {
            userOptional = userRepository.findById(Long.valueOf(value));
        } else {
            userOptional = userRepository.findByUsername(value);
        }
        return userOptional.orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User getUserById(Long userId) {
        return getUserByField("id", userId.toString());
    }

    public User getUserByUsername(String username) {
        return getUserByField("username", username);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findUserByRoleOrderedByUsername(role);
    }

    public List<User> getAllUsers() {
        return userRepository.findAllOrderedByUsername();
    }


    // Create a default admin user if one does not exist - called at startup
    private void createDefaultAdmin() {
        if (userRepository.findByRolesContaining("ADMIN").isEmpty()) {
            User admin = new User(adminUsername, adminEmail, passwordEncoder.encode(adminPassword), Set.of("ADMIN", "USER"));
            userRepository.save(admin);
        }
//        List<User> adminUser = userRepository.findByRolesContaining("ADMIN"); // DOES AN ADMIN EXIST?????
//        if (adminUser.isEmpty()) {
//            String hashedPassword = passwordEncoder.encode(adminPassword);
//            User admin = new User(adminUsername, adminEmail, hashedPassword, Set.of("Admin", "User")); // Create new user, add big bad admin status
//            userRepository.save(admin);
//        }
    }

    public void deleteUser(Long userId, String currentUsername) {
        User user = getUserById(userId);
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("Cannot delete yourself"); // just in case something goes buck wild (confidence lvl 100)
        }
        userRepository.delete(user);

    }

    public UserUpdateRequest getUserUpdateRequest(Long userId) {
        User user = getUserById(userId);
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setUsername(user.getUsername());
        userUpdateRequest.setEmail(user.getEmail());
        userUpdateRequest.setTrainerRole(user.getRoles().contains("TRAINER") ? "on" : null);
        return userUpdateRequest;
    }

    public void updateUser(Long userId, UserUpdateRequest request) {
        User currentUser = getUserById(userId);

        // if the username is different and the new username already exists
        if (!currentUser.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (!currentUser.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        currentUser.setUsername(request.getUsername());
        currentUser.setEmail(request.getEmail());
        Set<String> roles = currentUser.getRoles();
        if (request.getTrainerRole() != null) {
            roles.add("TRAINER");
        } else {
            roles.remove("TRAINER");
        }
        currentUser.setRoles(roles);
        userRepository.save(currentUser);
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
