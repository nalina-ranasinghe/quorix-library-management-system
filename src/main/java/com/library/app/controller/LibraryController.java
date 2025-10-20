package com.library.app.controller;

import com.library.app.entity.Book;
import com.library.app.entity.Borrowing;
import com.library.app.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LibraryController {

    @Autowired
    private BorrowService borrowService;

    // Test page for static resources
    @GetMapping("/staff/test")
    public String test() {
        return "test";
    }

    // Library Management System page
    @GetMapping("/staff/library-management")
    public String libraryManagement(Model model) {
        // Load available books for the UI
        List<Book> availableBooks = borrowService.getAvailableBooks();
        model.addAttribute("availableBooks", availableBooks);
        model.addAttribute("pageTitle", "Library Management System");
        return "library-management";
    }

    // API Endpoints for AJAX calls

    @PostMapping("/api/borrow-book")
    @ResponseBody
    public Map<String, Object> borrowBook(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer userId = Integer.parseInt(request.get("userId").toString());
            Integer bookId = Integer.parseInt(request.get("bookId").toString());
            Integer days = Integer.parseInt(request.get("days").toString());

            String result = borrowService.borrowBook(userId, bookId, days);
            response.put("success", result.startsWith("Book borrowed successfully"));
            response.put("message", result);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/api/return-book/{borrowingId}")
    @ResponseBody
    public Map<String, Object> returnBook(@PathVariable Integer borrowingId) {
        Map<String, Object> response = new HashMap<>();

        try {
            String result = borrowService.returnBook(borrowingId);
            response.put("success", result.startsWith("Book returned successfully"));
            response.put("message", result);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/api/renew-book/{borrowingId}")
    @ResponseBody
    public Map<String, Object> renewBook(@PathVariable Integer borrowingId,
                                         @RequestParam Integer additionalDays) {
        Map<String, Object> response = new HashMap<>();

        try {
            String result = borrowService.renewBook(borrowingId, additionalDays);
            response.put("success", result.startsWith("Book renewed successfully"));
            response.put("message", result);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/api/user-borrowings/{userId}")
    @ResponseBody
    public List<Borrowing> getUserBorrowings(@PathVariable Integer userId) {
        return borrowService.getUserBorrowings(userId);
    }

    @GetMapping("/api/available-books")
    @ResponseBody
    public List<Book> getAvailableBooks() {
        return borrowService.getAvailableBooks();
    }
}
