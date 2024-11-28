package org.example.fitnessclubmanagement.controller;

import jakarta.validation.Valid;
import org.example.fitnessclubmanagement.exceptions.UserAlreadyExistsException;
import org.example.fitnessclubmanagement.payload.UserRegistrationRequest;
import org.example.fitnessclubmanagement.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controller for handling user registration and authentication.
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Displays the login page.
     *
     * @return the name of the login view
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    /**
     * Displays the registration page.
     *
     * @param model the model to hold the user registration request
     * @return the name of the registration view
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserRegistrationRequest());
        return "auth/register";
    }

    /**
     * Handles the user registration.
     *
     * @param request the user registration request containing username, email, and password
     * @param result the binding result to hold validation errors
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationRequest request, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.registerUser(request);
            return "redirect:/users";
        } catch (UserAlreadyExistsException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
}

