package org.example.responsiveuserregistration.controller;

import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.repository.UserRepository;
import org.example.responsiveuserregistration.service.AuthService;
import org.example.responsiveuserregistration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    //Get Mappings
    @GetMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @GetMapping("/users/{userId}")
    public String getUserDetails(@PathVariable Long userId, Model model) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            return "userdetails";
        } else {
            model.addAttribute("errorMessage", "User not found");
            return "redirect:/users";
        }
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/account")
    public String account(@AuthenticationPrincipal UserDetails userdetails, Model model) {
        String username = userdetails.getUsername();
        model.addAttribute("username", username);
        return "account";
    }

    @PostMapping("/account/updatePassword")
    public String updatePassword(@AuthenticationPrincipal UserDetails userdetails, @RequestParam("newPassword") String newPassword, Model model) {
        String username = userdetails.getUsername();
        try {
            authService.updatePassword(username, newPassword);
            model.addAttribute("successMessage", "Password updated successfully");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "account";
    }

    @PostMapping("/clearTable")
    public String clearTable(Model model) {
        userRepository.deleteAll();
        return "index";
    }

    @PostMapping("/users/{userId}/updateRole")
    public String updateUserRole(@PathVariable Long userId, @RequestParam(value = "trainerRole", required = false) String trainerRole, Model model) {
        try {
            userService.updateUserRole(userId, trainerRole);
            model.addAttribute("successMessage", "Role updated successfully");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users/" + userId;
    }

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
