package com.library.app.controller;

import com.library.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/announcements") // Base URL for this controller
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;


    /**
     * Handles the GET request to show the announcements form page.
     * This is linked from the "Send Alerts" button on the admin dashboard.
     * @return The path to the announcements.html template.
     */
    @GetMapping
    public String showAnnouncementsPage() {
        return "admin/announcements";
    }


    /**
     * Handles the POST request from the form to create and send a new announcement.
     */
    @PostMapping("/create")
    public String createAnnouncement(@RequestParam String title,
                                     @RequestParam String message,
                                     Principal principal,
                                     RedirectAttributes ra) {
        if (principal == null) {
            return "redirect:/";
        }
        try {
            notificationService.createAnnouncement(title, message, principal.getName());
            ra.addFlashAttribute("successMessage", "Announcement sent successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Failed to send announcement: " + e.getMessage());
        }

        return "redirect:/admin/announcements";
    }


}