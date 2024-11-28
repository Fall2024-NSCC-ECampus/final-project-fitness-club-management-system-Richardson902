package org.example.responsiveuserregistration.service;

import jakarta.annotation.PostConstruct;
import org.example.responsiveuserregistration.exceptions.UserAlreadyExistsException;
import org.example.responsiveuserregistration.exceptions.UserNotFoundException;
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

/**
 * Service class for managing users.
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername; // no hardcoded credentials around here

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    /**
     * Initializes the service by creating a default admin user if one does not exist.
     */
    @PostConstruct
    public void init() {
        createDefaultAdmin();
    }

    /**
     * Retrieves a user by a specified field and value.
     *
     * @param field the field to search by (e.g., "id" or "username")
     * @param value the value of the field
     * @return the user
     * @throws UserNotFoundException if the user is not found
     */
    private User getUserByField(String field, String value) {
        Optional<User> userOptional;
        if (field.equals("id")) {
            userOptional = userRepository.findById(Long.valueOf(value));
        } else {
            userOptional = userRepository.findByUsername(value);
        }
        return userOptional.orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user
     * @return the user
     */
    public User getUserById(Long userId) {
        return getUserByField("id", userId.toString());
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user
     * @return the user
     */
    public User getUserByUsername(String username) {
        return getUserByField("username", username);
    }

    /**
     * Retrieves a user by their role.
     *
     * @param role the role of the users
     * @return the list of users
     */
    public List<User> getUsersByRole(String role) {
        return userRepository.findUserByRoleOrderedByUsername(role);
    }

    /**
     * Retrieves a list of all users.
     *
     * @return the list of users
     */
    public List<User> getAllUsers() {
        return userRepository.findAllOrderedByUsername();
    }


    /**
     * Creates a default admin user if one does not exist.
     */
    private void createDefaultAdmin() {
        if (userRepository.findByRolesContaining("ADMIN").isEmpty()) {
            User admin = new User(adminUsername, adminEmail, passwordEncoder.encode(adminPassword), Set.of("ADMIN", "USER"));
            userRepository.save(admin);
        }
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId the ID of the user
     * @param currentUsername the username of the current user
     * @throws IllegalArgumentException if the user tries to delete themselves
     */
    public void deleteUser(Long userId, String currentUsername) {
        User user = getUserById(userId);
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("Cannot delete yourself"); // just in case something goes buck wild (confidence lvl 100)
        }
        userRepository.delete(user);

    }

    /**
     * Retrieves a user update request by the user's ID.
     *
     * @param userId the ID of the user
     * @return the user update request
     */
    public UserUpdateRequest getUserUpdateRequest(Long userId) {
        User user = getUserById(userId);
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setUsername(user.getUsername());
        userUpdateRequest.setEmail(user.getEmail());
        userUpdateRequest.setTrainerRole(user.getRoles().contains("TRAINER") ? "on" : null);
        return userUpdateRequest;
    }

    /**
     * Updates a user with the given request.
     *
     * @param userId the ID of the user
     * @param request the user update request
     * @throws UserAlreadyExistsException if the username or email already exists
     */
    public void updateUser(Long userId, UserUpdateRequest request) {
        User currentUser = getUserById(userId);

        // if the username is different and the new username already exists
        if (!currentUser.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (!currentUser.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        currentUser.setUsername(request.getUsername());
        currentUser.setEmail(request.getEmail());
        Set<String> roles = currentUser.getRoles();

        // if the trainer box is checked, add the role, otherwise remove it
        if (request.getTrainerRole() != null) {
            roles.add("TRAINER");
        } else {
            roles.remove("TRAINER");
        }
        currentUser.setRoles(roles);
        userRepository.save(currentUser);
    }

    /**
     * Loads a user by their username for authentication.
     *
     * @param username the username of the user
     * @return the user details
     * @throws UsernameNotFoundException if the user is not found
     */
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

        // For authentication (spring security moment)
    }

}
