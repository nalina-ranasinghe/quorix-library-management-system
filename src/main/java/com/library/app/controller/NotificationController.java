package com.library.app.controller;

import com.library.app.entity.Notification;
import com.library.app.entity.User;
import com.library.app.repository.UserRepository;
import com.library.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public String showNotifications(Model model, Principal principal) {
        if (principal == null) return "redirect:/";

        User currentUser = userRepository.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        Integer userId = currentUser.getUserId();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("notifications", notificationService.getUserNotifications(userId));
        model.addAttribute("announcements", notificationService.getAnnouncements());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(userId));
        return "notifications";
    }

    @PostMapping("/delete/{id}")
    public String deleteNotification(@PathVariable("id") int notificationId, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/";
        try {
            notificationService.deleteNotification(notificationId, principal.getName());
            ra.addFlashAttribute("successMessage", "Notification deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/notifications";
    }

    @PostMapping("/mark-read/{id}")
    public String markNotificationAsRead(@PathVariable("id") int notificationId, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/";
        try {
            notificationService.markAsRead(notificationId, principal.getName());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/notifications";
    }

    @PostMapping("/mark-all-read")
    public String markAllAsRead(Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/";
        User currentUser = userRepository.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        notificationService.markAllAsRead(currentUser.getUserId());
        ra.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/notifications";
    }

    @GetMapping("/preferences")
    public String showPreferences(Model model, Principal principal) {
        if (principal == null) return "redirect:/";
        User currentUser = userRepository.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));


        model.addAttribute("currentUser", currentUser);

        // passes the user's *current* preference object to the form
        model.addAttribute("preferences", notificationService.getUserPreferences(currentUser.getUserId()));
        return "notification-preferences";
    }

    @PostMapping("/preferences")
    public String updatePreferences(@ModelAttribute NotificationService.UserNotificationPreferences preferences,
                                    Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/";
        User currentUser = userRepository.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));


        notificationService.updateUserPreferences(currentUser.getUserId(), preferences);

        ra.addFlashAttribute("successMessage", "Your notification preferences have been saved.");
        return "redirect:/notifications/preferences";
    }
}