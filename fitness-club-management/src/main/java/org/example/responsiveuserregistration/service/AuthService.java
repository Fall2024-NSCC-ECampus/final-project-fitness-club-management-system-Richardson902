package org.example.responsiveuserregistration.service;

import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.payload.UserRegistrationRequest;
import org.example.responsiveuserregistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    public void registerUser(UserRegistrationRequest request){

        // Trim it to avoid nonsense
        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword();

        if (userRepository.existsByUsernameOrEmail(username, email)) {
            throw new IllegalArgumentException("User already exists");
        }

        String hashedPassword = passwordEncoder.encode(password); // Hash that s*** NO PLAINTEXT PASSWORDS IN HERE

        User user = new User(username, email, hashedPassword, Set.of("USER")); // Create new user and set default role
        userRepository.save(user);
    }

    public void updatePassword(String username, String newPassword) {
        User user = userService.getUserByUsername(username);
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword); // sets new password to new hashed password. SECURITY MOMENT
        userRepository.save(user);
    }
}
