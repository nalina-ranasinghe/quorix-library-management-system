package com.library.app.controller;

import com.library.app.entity.Book;
import com.library.app.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.*;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    // READ: Display the list of all books
    @GetMapping("/staff/books")
    public String viewBookCatalog(Model model) {
        model.addAttribute("listBooks", bookService.getAllBooks());
        return "books"; // Returns books.html
    }

    // CREATE: Show the form to add a new book
    @GetMapping("/staff/books/new")
    public String showNewBookForm(Model model) {
        Book book = new Book();
        model.addAttribute("book", book);
        return "add-book-form"; // Returns add-book-form.html
    }

    // CREATE: Save the new book to the database
    @PostMapping("/staff/books/save")
    public String saveBook(@Valid @ModelAttribute("book") Book book, BindingResult result, Model model) {

        // --- Custom check for duplicate ISBN ---
        if (bookService.isbnExists(book.getIsbn())) {
            result.rejectValue("isbn", "isbn.exists", "A book with this ISBN already exists.");
        }

        // --- Check for all validation errors ---
        if (result.hasErrors()) {
            // If there are errors, redirect back to the form with error message
            String errorMessage = "Please correct the errors below: ";
            if (result.hasFieldErrors("isbn")) {
                errorMessage += "This ISBN number already exist! Please check the ISBN number again! ";
            }
            if (result.hasFieldErrors("title")) {
                errorMessage += "Title is required. ";
            }
            if (result.hasFieldErrors("author")) {
                errorMessage += "Author is required. ";
            }
            return "redirect:/staff/books/new?error=" + java.net.URLEncoder.encode(errorMessage, java.nio.charset.StandardCharsets.UTF_8);
        }

        // If there are no errors, save the book and redirect
        bookService.saveBook(book);
        return "redirect:/staff/books";
    }

    // UPDATE: Show the form to edit an existing book
    @GetMapping("/staff/books/edit/{id}")
    public String showEditBookForm(@PathVariable(value = "id") Long id, Model model) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + id));
        model.addAttribute("book", book);
        return "edit-book-form"; // Returns edit-book-form.html
    }

    // UPDATE: Save the changes to the book
    @PostMapping("/staff/books/update/{id}")
    public String updateBook(@PathVariable("id") long id,
                             @Valid @ModelAttribute("book") Book book,
                             BindingResult result,
                             Model model) {

        // --- Custom check for duplicate ISBN on UPDATE ---
        if (bookService.isbnExistsForOtherId(book.getIsbn(), id)) {
            result.rejectValue("isbn", "isbn.exists", "Another book with this ISBN already exists.");
        }

        // --- Check for all validation errors ---
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Update Book Details");
            return "edit-book-form"; // Return to the edit form with error messages
        }

        // --- If no errors, proceed with saving ---

        // We must fetch the original creation date, as it's not submitted from the form
        Book originalBook = bookService.getBookById(id).orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + id));
        book.setCreatedAt(originalBook.getCreatedAt()); // Preserve the original creation date

        book.setId(id); // Ensure the correct ID is set for updating
        bookService.saveBook(book);
        return "redirect:/staff/books";
    }

    // DELETE: Delete a book
    @GetMapping("/staff/books/delete/{id}")
    public String deleteBook(@PathVariable(value = "id") Long id, Model model) {
        String result = bookService.deleteBook(id);
        
        if (result.startsWith("Book deleted successfully")) {
            return "redirect:/staff/books";
        } else {
            // Add error message and redirect back to books page
            model.addAttribute("errorMessage", result);
            return "redirect:/staff/books?error=" + java.net.URLEncoder.encode(result, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /**
     * Handles the search request and displays the results.
     */
    @GetMapping("/staff/books/search")
    public String searchBooks(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("listBooks", bookService.searchBooks(keyword));
        model.addAttribute("keyword", keyword);
        return "search-results"; // Returns a new HTML page: search-results.html
    }

    /**
     * Displays a report of all books marked as "Missing".
     */
    @GetMapping("/staff/books/missing")
    public String viewMissingBooks(Model model) {
        model.addAttribute("listBooks", bookService.getMissingBooks());
        return "missing-books"; // Returns a new HTML page: missing-books.html
    }



}

