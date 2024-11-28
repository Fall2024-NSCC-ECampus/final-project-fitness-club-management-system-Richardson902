package org.example.responsiveuserregistration.controller;

import jakarta.validation.Valid;
import org.example.responsiveuserregistration.exceptions.UserAlreadyExistsException;
import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.payload.UpdatePasswordRequest;
import org.example.responsiveuserregistration.payload.UserUpdateRequest;
import org.example.responsiveuserregistration.repository.UserRepository;
import org.example.responsiveuserregistration.service.AuthService;
import org.example.responsiveuserregistration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing user-related operations.
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Displays a list of all users.
     *
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    /**
     * Displays the details of a specific user.
     * @param userId the ID of the user to display
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/users/{userId}")
    public String getUserDetails(@PathVariable Long userId, Model model) {
        try {
            User user = userService.getUserById(userId);
            UserUpdateRequest userUpdateRequest = userService.getUserUpdateRequest(userId);
            model.addAttribute("user", user);
            model.addAttribute("userUpdateRequest", userUpdateRequest);
            return "userdetails";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/users";
        }
    }

    /**
     * Displays the home page.
     *
     * @return the name of the view to render
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Displays the account page for the authenticated user.
     *
     * @param userdetails the authenticated user's details
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/account")
    public String account(@AuthenticationPrincipal UserDetails userdetails, Model model) {
        String username = userdetails.getUsername();
        model.addAttribute("username", username);
        model.addAttribute("passwordUpdateRequest", new UpdatePasswordRequest());
        return "account";
    }

    /**
     * Updates the password for the authenticated user.
     *
     * @param userdetails the authenticated user's details
     * @param request the request to update the password
     * @param result the binding result for validation
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @PostMapping("/account/updatePassword")
    public String updatePassword(@AuthenticationPrincipal UserDetails userdetails, @Valid @ModelAttribute("passwordUpdateRequest")UpdatePasswordRequest request, BindingResult result, Model model) {
        String username = userdetails.getUsername();
        if (result.hasErrors()) {
            return "account";
        }
        try {
            authService.updatePassword(username, request);
            model.addAttribute("successMessage", "Password updated successfully");
            return "account"; // doesn't redirect to prevent losing the success message
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "account";
        }
    }

    /**
     * Updated the details of a specific user.
     *
     * @param userId the ID of the user to update
     * @param request the request containing the updated user details
     * @param result the binding result for validation
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @PostMapping("/users/{userId}/update")
    public String updateUser(@PathVariable Long userId, @Valid @ModelAttribute("userUpdateRequest") UserUpdateRequest request, BindingResult result, Model model) {
       User user = userService.getUserById(userId);
       model.addAttribute("user", user);
       if (result.hasErrors()) {
           return "userdetails";
       }
       try {
           userService.updateUser(userId, request);
           model.addAttribute("successMessage", "User updated successfully");
           return "userdetails";
       } catch (UserAlreadyExistsException e) {
           model.addAttribute("errorMessage", e.getMessage());
           return "userdetails";
       }
    }

    /**
     * Deletes a specific user.
     *
     * @param userId the ID of the user to delete
     * @param currentUser the details of the authenticated user
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetails currentUser, Model model) {
        try {
            userService.deleteUser(userId, currentUser.getUsername());
            return "redirect:/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/users";
        }
    }


}
