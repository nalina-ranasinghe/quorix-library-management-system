package com.library.app.controller;

import com.library.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final UserService userService;

    @GetMapping("/")
    public String index() {
        return "index"; // intro page with modal
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/staff")
    public String staff() {
        return "staff";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String username, @RequestParam String email, RedirectAttributes ra) {
        if (userService.verifyUserForPasswordReset(username, email)) {
            // If valid, redirect to the reset page with username as a parameter
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
            ra.addAttribute("username", username); // Resend username on error
            return "redirect:/reset-password";
        }
        userService.resetUserPassword(username, password);
        ra.addFlashAttribute("successMessage", "Your password has been reset successfully. Please sign in.");
        return "redirect:/"; // Redirect to login page
    }
}