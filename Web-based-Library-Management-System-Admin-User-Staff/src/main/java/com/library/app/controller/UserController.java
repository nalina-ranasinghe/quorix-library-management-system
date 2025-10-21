package com.library.app.controller;

import com.library.app.dto.UserBorrowingDto;
import com.library.app.dto.UserReservationDto;
import com.library.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ReservationService reservationService;
    private final BorrowingService borrowingService;
    private final WaitlistService waitlistService;

    @GetMapping("/my-books")
    public String myBooks(Model model, Principal principal) {
        if (principal == null) return "redirect:/";

        String username = principal.getName();
        userService.findUserByUsername(username).ifPresent(user -> model.addAttribute("currentUser", user));
        model.addAttribute("borrowedBooks", borrowingService.getBorrowedBooksForUser(username));
        model.addAttribute("reservedBooks", reservationService.getReservedBooksForUser(username));


        model.addAttribute("waitlistedBooks", waitlistService.getWaitlistedBooksForUser(username));

        return "my-books";
    }
    @GetMapping("/account/settings")
    public String settingsPage(Model model, Principal principal) {
        if (principal == null) return "redirect:/";

        userService.findUserByUsername(principal.getName()).ifPresent(user -> model.addAttribute("currentUser", user));
        return "useraccountsettings";
    }

    @PostMapping("/account/settings")
    public String processSettingsForm(@RequestParam String fullName, @RequestParam String email, @RequestParam String phone,
                                      @RequestParam(required = false) String newPassword, @RequestParam(required = false) String confirmPassword,
                                      Principal principal, RedirectAttributes ra) {
        try {
            if (newPassword != null && !newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("New passwords do not match.");
            }
            userService.updateUserDetails(principal.getName(), fullName, email, phone, newPassword);
            ra.addFlashAttribute("successMessage", "Your details have been updated successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/account/settings";
    }

    @PostMapping("/reserve")
    public String reserveBook(@RequestParam("bookId") int bookId, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/";
        try {
            reservationService.reserveBook(bookId, principal.getName());
            ra.addFlashAttribute("successMessage", "Book reserved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/home";
    }

    @PostMapping("/waitlist")
    public String addToWaitlist(@RequestParam("bookId") int bookId, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/";
        try {
            waitlistService.addToWaitlist(bookId, principal.getName());
            ra.addFlashAttribute("successMessage", "You've been added to the waitlist!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/home";
    }

    @PostMapping("/reservations/cancel/{id}")
    public String cancelReservation(@PathVariable("id") int reservationId, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/";
        try {
            reservationService.cancelReservation(reservationId, principal.getName());
            ra.addFlashAttribute("successMessage", "Reservation cancelled successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-books";
    }
    @PostMapping("/waitlist/cancel/{id}")
    public String cancelWaitlist(@PathVariable("id") int waitlistId, Principal principal, RedirectAttributes ra) {
        if (principal == null) {
            return "redirect:/";
        }
        try {
            waitlistService.cancelWaitlist(waitlistId, principal.getName());
            ra.addFlashAttribute("successMessage", "You have been removed from the waitlist.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-books";
    }
}