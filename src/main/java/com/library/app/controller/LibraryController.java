package com.library.app.controller;

import com.library.app.entity.Book;
import com.library.app.entity.Borrowing;
import com.library.app.service.BorrowService; // Uses the updated service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/staff")
public class LibraryController {

    @Autowired
    private BorrowService borrowService; // Uses the updated service

    /** Redirects base /staff URL to management page */
    @GetMapping("")
    public String staffHomeRedirect() {
        return "redirect:/staff/library-management";
    }

    /** Serves the main HTML page */
    @GetMapping("/library-management")
    public String libraryManagementPage(Model model) {
        model.addAttribute("pageTitle", "Library Management System");
        return "library-management";
    }

    //API Endpoints

    @PostMapping("/api/borrow-book")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> borrowBookApi(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Use helper for safe parsing
            Integer userId = parseInteger(request.get("userId"), "userId");
            Integer bookId = parseInteger(request.get("bookId"), "bookId");
            Integer days = parseInteger(request.get("days"), "days");

            String resultMessage = borrowService.borrowBook(userId, bookId, days);
            boolean success = resultMessage.startsWith("✅");
            response.put("success", success);
            response.put("message", resultMessage);
            return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "❌ Error: Invalid input - " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("API Error /api/borrow-book: " + e.getMessage()); e.printStackTrace();
            response.put("success", false);
            response.put("message", "❌ Error: Server error during borrow.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/api/return-book/{borrowingId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> returnBookApi(@PathVariable Integer borrowingId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (borrowingId == null || borrowingId <= 0) throw new IllegalArgumentException("Borrowing ID required.");
            String resultMessage = borrowService.returnBook(borrowingId);
            boolean success = resultMessage.startsWith("✅");
            response.put("success", success);
            response.put("message", resultMessage);
            return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false); response.put("message", "❌ Error: Invalid input - " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("API Error /api/return-book: " + e.getMessage()); e.printStackTrace();
            response.put("success", false); response.put("message", "❌ Error: Server error during return.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/api/renew-book/{borrowingId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> renewBookApi(@PathVariable Integer borrowingId,
                                                            @RequestParam Integer additionalDays) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Basic validation in controller
            if (borrowingId == null || borrowingId <= 0) throw new IllegalArgumentException("Borrowing ID required.");
            if (additionalDays == null || additionalDays <= 0) throw new IllegalArgumentException("Additional days must be positive.");

            String resultMessage = borrowService.renewBook(borrowingId, additionalDays);
            boolean success = resultMessage.startsWith("✅");
            response.put("success", success);
            response.put("message", resultMessage);
            return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false); response.put("message", "❌ Error: Invalid input - " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("API Error /api/renew-book: " + e.getMessage()); e.printStackTrace();
            response.put("success", false); response.put("message", "❌ Error: Server error during renewal.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Endpoints for Existing UI

    @GetMapping("/api/user-borrowings/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserBorrowingsApi(@PathVariable Integer userId) {
        try {
            if (userId == null || userId <= 0) throw new IllegalArgumentException("User ID must be positive.");
            List<Borrowing> borrowings = borrowService.getUserBorrowings(userId);
            return ResponseEntity.ok(borrowings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "❌ Error: Invalid User ID."));
        } catch (Exception e) {
            System.err.println("API Error /api/user-borrowings: " + e.getMessage()); e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "❌ Error: Server error fetching borrowings."));
        }
    }

    @GetMapping("/api/available-books")
    @ResponseBody
    public ResponseEntity<List<Book>> getAvailableBooksApi() {
        try {
            List<Book> books = borrowService.getAvailableBooks();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            System.err.println("API Error /api/available-books: " + e.getMessage()); e.printStackTrace();
            // Return an empty list or an error response
            return ResponseEntity.internalServerError().body(List.of()); // Or construct an error map
        }
    }

    // Helper Method for Parsing
    private Integer parseInteger(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        try {
            // Handle potential NumberFormatException if input is not a valid integer string
            int value = Integer.parseInt(obj.toString());
            if (value <= 0 && !fieldName.equals("quantity")) { // Allow 0 quantity, but not IDs
                throw new IllegalArgumentException(fieldName + " must be a positive number.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for field: " + fieldName);
        }
    }
}