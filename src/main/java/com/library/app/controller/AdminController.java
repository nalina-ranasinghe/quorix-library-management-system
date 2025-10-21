package com.library.app.controller;

import com.library.app.entity.User;
import com.library.app.service.ReportLogService;
import com.library.app.service.ReportService;
import com.library.app.service.UserService;
import com.library.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ReportService reportService;
    private final ReportLogService reportLogService;

    @GetMapping
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/reports")
    public String showReports(Model model, Principal principal) {
        // Data for the summary cards at the top
        model.addAttribute("reportData", reportService.generateAdminDashboardReport(principal));

        // Data for the main report tables (Overdue, Availability, Attendance)
        model.addAttribute("operationalReportData", reportService.generateOperationalReport(principal));

        // Data for the report generation log table
        model.addAttribute("reportLogs", reportLogService.getRecentLogs());

        return "admin/reports";
    }

    /**
     * Handles the new, separate dashboard for Usage Patterns.
     */
    @GetMapping("/reports/usage")
    public String showUsageReport(Model model, Principal principal) {
        // Fetches only the data for popular books and top users
        model.addAttribute("usageReportData", reportService.generateUsagePatternReport(principal));
        return "admin/usage-report"; // Renders the new, dedicated usage report page
    }

    // ===============================================
    // == ALL OTHER USER MANAGEMENT METHODS ARE PRESERVED ==
    // ===============================================

    // Display list of users
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users-list";
    }

    // Show form to add a new user
    @GetMapping("/users/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("pageTitle", "Add New User");
        return "admin/user-form";
    }

    // Show form to edit an existing user
    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") int id, Model model, RedirectAttributes ra) {
        return userService.findUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    model.addAttribute("pageTitle", "Edit User (ID: " + id + ")");
                    return "admin/user-form";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "User not found with ID: " + id);
                    return "redirect:/admin/users";
                });
    }

    // Process the add/edit user form
    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("user") User user, RedirectAttributes ra) {
        try {
            userService.saveUserByAdmin(user);
            ra.addFlashAttribute("successMessage", "User saved successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            ra.addFlashAttribute("user", user);
            if (user.getUserId() == null || user.getUserId() == 0) {
                return "redirect:/admin/users/add";
            } else {
                return "redirect:/admin/users/edit/" + user.getUserId();
            }
        }
    }

    // Delete a user permanently
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") int id, RedirectAttributes ra) {
        userService.deleteUser(id);
        ra.addFlashAttribute("successMessage", "User deleted permanently!");
        return "redirect:/admin/users";
    }

    // Method for viewing user details
    @GetMapping("/users/view/{id}")
    public String viewUser(@PathVariable("id") int id, Model model, RedirectAttributes ra) {
        return userService.findUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "admin/user-details";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "User not found with ID: " + id);
                    return "redirect:/admin/users";
                });
    }
    @GetMapping("/users/pending")
    public String showPendingUsers(Model model) {
        model.addAttribute("pendingUsers", userService.findPendingUsers());
        return "admin/pending-users"; // This is a new HTML file we will create
    }
    @PostMapping("/users/approve/{id}")
    public String approveUser(@PathVariable("id") int id, RedirectAttributes ra) {
        userService.approveUser(id);
        ra.addFlashAttribute("successMessage", "User has been approved and is now active.");
        return "redirect:/admin/users/pending";
    }
    @PostMapping("/users/reject/{id}")
    public String rejectUser(@PathVariable("id") int id, RedirectAttributes ra) {
        userService.rejectUser(id);
        ra.addFlashAttribute("successMessage", "User has been rejected and removed.");
        return "redirect:/admin/users/pending";
    }
    @PostMapping("/reports/send")
    public String sendReportEmail(@RequestParam("recipientEmail") String email, RedirectAttributes ra) {
        try {
            reportService.sendOperationalReportByEmail(email);
            ra.addFlashAttribute("successMessage", "Report has been sent successfully to " + email);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Failed to send report. Please check the system logs.");
        }
        return "redirect:/admin/reports";
    }
}