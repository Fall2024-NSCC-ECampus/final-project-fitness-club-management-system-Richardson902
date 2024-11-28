package org.example.responsiveuserregistration.service;

import org.example.responsiveuserregistration.exceptions.UserAlreadyExistsException;
import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.payload.UpdatePasswordRequest;
import org.example.responsiveuserregistration.payload.UserRegistrationRequest;
import org.example.responsiveuserregistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service class for managing authentication-relates operations.
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    /**
     * Registers a new user.
     * @param request the user registration request containing username, email, and password
     * @throws UserAlreadyExistsException if the user already exists
     */
    public void registerUser(UserRegistrationRequest request){

        // Trim it to avoid nonsense
        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword();

        if (userRepository.existsByUsernameOrEmail(username, email)) {
            throw new UserAlreadyExistsException("User already exists");
        }

        String hashedPassword = passwordEncoder.encode(password); //NO PLAINTEXT PASSWORDS IN HERE

        User user = new User(username, email, hashedPassword, Set.of("USER")); // Create new user and set default role
        userRepository.save(user);
    }

    /**
     * Updates the password of an existing user.
     * @param username the username of the user
     * @param request the update password request containing the new password
     */
    public void updatePassword(String username, UpdatePasswordRequest request) {
        User user = userService.getUserByUsername(username);
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword); // sets new password to new hashed password. SECURITY MOMENT
        userRepository.save(user);
    }
}
