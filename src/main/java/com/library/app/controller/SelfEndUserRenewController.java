package com.library.app.controller;

import com.library.app.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

    @Controller
    @RequiredArgsConstructor
    public class SelfEndUserRenewController {

        private final BorrowingService borrowingService;

        @PostMapping("/borrowings/renew/{id}")
        public String renewBorrowing(@PathVariable("id") int borrowingId, Principal principal, RedirectAttributes ra) {
            if (principal == null) {

                ra.addFlashAttribute("errorMessage", "You must be logged in to renew a book.");
                return "redirect:/";
            }
            try {
                borrowingService.renewBorrowing(borrowingId, principal.getName());
                ra.addFlashAttribute("successMessage", "Book renewed for 7 more days!");
            } catch (SecurityException | IllegalArgumentException | IllegalStateException e) {
                // Catch specific, expected errors and show them to the user.
                ra.addFlashAttribute("errorMessage", e.getMessage());
            } catch (Exception e) {
                // Catch any other unexpected errors.
                ra.addFlashAttribute("errorMessage", "An unexpected error occurred while renewing the book.");
            }
            return "redirect:/my-books";
        }
    }
