package com.library.app.controller;

import com.library.app.entity.User;
import com.library.app.service.BookService;
import com.library.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final UserService userService;
    private final BookService bookService; // Injected BookService

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // UPDATED Home Page Endpoint
    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        // Add the list of all books to the model
        model.addAttribute("books", bookService.getAllBooks());

        // Get the logged-in user's details and add them to the model
        if (principal != null) {
            userService.findUserByUsername(principal.getName())
                    .ifPresent(user -> model.addAttribute("currentUser", user));
        }
        return "home";
    }

    //  PASSWORD RESET ENDPOINTS

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String username, @RequestParam String email, RedirectAttributes ra) {
        if (userService.verifyUserForPasswordReset(username, email)) {
            ra.addAttribute("username", username);
            return "redirect:/reset-password";
        } else {
            ra.addFlashAttribute("errorMessage", "Invalid username or email.");
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String username, @RequestParam String password, @RequestParam String confirmPassword, RedirectAttributes ra) {
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("errorMessage", "Passwords do not match.");
            ra.addAttribute("username", username);
            return "redirect:/reset-password";
        }
        userService.resetUserPassword(username, password);
        ra.addFlashAttribute("successMessage", "Your password has been reset successfully. Please sign in.");
        return "redirect:/";
    }

    //  SETTINGS PAGE ENDPOINTS

    @GetMapping("/settings")
    public String showSettingsForm(Model model, Principal principal, RedirectAttributes ra) {
        if (principal == null) {
            ra.addFlashAttribute("errorMessage", "Please log in to access settings.");
            return "redirect:/"; // Redirect to login if not authenticated
        }
        userService.findUserByUsername(principal.getName())
                .ifPresent(user -> model.addAttribute("currentUser", user));
        return "settings";
    }

    @PostMapping("/settings")
    public String processSettingsForm(@RequestParam String fullName,
                                      @RequestParam String email,
                                      @RequestParam String phone,
                                      @RequestParam(required = false) String newPassword,
                                      @RequestParam(required = false) String confirmPassword,
                                      Principal principal,
                                      RedirectAttributes ra) {
        try {
            // Check if new passwords match (if provided)
            if (newPassword != null && !newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("New passwords do not match.");
            }

            userService.updateUserDetails(principal.getName(), fullName, email, phone, newPassword);
            ra.addFlashAttribute("successMessage", "Your details have been updated successfully.");

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/settings";
    }
}