// New StaffController.java (for STAFF role, including approve)
package com.library.app.controller;

import com.library.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final UserService userService;

    @GetMapping
    public String staffDashboard() {
        return "staff/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "staff/users-list";
    }

    @GetMapping("/users/approve/{id}")
    public String approveUser(@PathVariable("id") int id, RedirectAttributes ra) {
        userService.approveUser(id);
        ra.addFlashAttribute("successMessage", "User approved successfully!");
        return "redirect:/staff/users";
    }
}