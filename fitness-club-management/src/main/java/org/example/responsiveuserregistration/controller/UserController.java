package org.example.responsiveuserregistration.controller;

import jakarta.validation.Valid;
import org.example.responsiveuserregistration.model.User;
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
        try {
            User user = userService.getUserById(userId);
            UserUpdateRequest userUpdateRequest = userService.getUserUpdateRequest(userId);
            model.addAttribute("user", user);
            model.addAttribute("userUpdateRequest", userUpdateRequest);
            return "userdetails";
        } catch (IllegalArgumentException e) {
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

    @PostMapping("/users/{userId}/update")
    public String updateUser(@PathVariable Long userId, @Valid @ModelAttribute("userUpdateRequest") UserUpdateRequest request, BindingResult result, Model model) {
       User user = userService.getUserById(userId);
       model.addAttribute("user", user);
       if (result.hasErrors()) {
           return "userdetails";
       }
       try {
           userService.updateUser(userId, request);
           return "redirect:/users/" + userId;
       } catch (IllegalArgumentException e) {
              model.addAttribute("errorMessage", e.getMessage());
              return "userdetails";
       }
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
