package com.library.app.controller;

import com.library.app.entity.Book;
import com.library.app.service.BookService;
import jakarta.servlet.http.HttpServletRequest; // Import HttpServletRequest
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    // Helper method to add request URI to model
    private void addRequestUriToModel(Model model, HttpServletRequest request) {
        model.addAttribute("requestURI", request.getRequestURI());
    }

    // READ: Display the list of all books
    @GetMapping("/staff/books")
    public String viewBookCatalog(Model model, HttpServletRequest request) { // Inject request
        addRequestUriToModel(model, request); // Add URI to model
        model.addAttribute("listBooks", bookService.getAllBooks());
        return "books";
    }

    // CREATE: Show the form to add a new book
    @GetMapping("/staff/books/new")
    public String showNewBookForm(Model model, HttpServletRequest request) { // Inject request
        addRequestUriToModel(model, request); // Add URI to model
        Book book = new Book();
        model.addAttribute("book", book);
        return "add-book-form";
    }

    // CREATE: Save the new book
    @PostMapping("/staff/books/save")
    public String saveBook(@Valid @ModelAttribute("book") Book book,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model, // Add model to pass URI back if errors
                           HttpServletRequest request) { // Inject request

        if (bookService.isbnExists(book.getIsbn())) {
            result.rejectValue("isbn", "isbn.duplicate", "A book with this ISBN already exists.");
        }

        if (result.hasErrors()) {
            addRequestUriToModel(model, request); // Add URI back to model for form reshow
            return "add-book-form"; // Return to form
        }

        bookService.saveBook(book);
        redirectAttributes.addFlashAttribute("successMessage", "Book '" + book.getTitle() + "' saved successfully!");
        return "redirect:/staff/library-management"; // Redirect as requested
    }

    // UPDATE: Show form to edit
    @GetMapping("/staff/books/edit/{id}")
    public String showUpdateForm(@PathVariable(value = "id") Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) { // Inject request
        Optional<Book> bookOpt = bookService.getBookById(id);
        if (bookOpt.isPresent()) {
            addRequestUriToModel(model, request); // Add URI to model
            model.addAttribute("book", bookOpt.get());
            return "edit-book-form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found with ID: " + id);
            return "redirect:/staff/books";
        }
    }

    // UPDATE: Process update
    @PostMapping("/staff/books/update/{id}")
    public String updateBook(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("book") Book book,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model, // Add model
                             HttpServletRequest request) { // Inject request

        if (bookService.isbnExistsForOtherId(book.getIsbn(), id)) {
            result.rejectValue("isbn", "isbn.duplicate", "Another book with this ISBN already exists.");
        }

        if (result.hasErrors()) {
            addRequestUriToModel(model, request); // Add URI back for form reshow
            book.setId(id); // Ensure ID is set
            return "edit-book-form";
        }

        book.setId(id);
        bookService.saveBook(book);
        redirectAttributes.addFlashAttribute("successMessage", "Book '" + book.getTitle() + "' updated successfully!");
        return "redirect:/staff/library-management"; // Redirect as requested
    }

    // DELETE: Delete a book
    @GetMapping("/staff/books/delete/{id}")
    public String deleteBook(@PathVariable(value = "id") Long id, RedirectAttributes redirectAttributes) {
        String result = bookService.deleteBook(id);
        if (result.startsWith("✅") || result.toLowerCase().contains("success")) { // Check common success patterns
            redirectAttributes.addFlashAttribute("successMessage", result);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result);
        }
        return "redirect:/staff/books"; // Redirect back to book list after delete attempt
    }

    // SEARCH: Handle search
    @GetMapping("/staff/books/search")
    public String searchBooks(@RequestParam("keyword") String keyword, Model model, HttpServletRequest request) { // Inject request
        addRequestUriToModel(model, request); // Add URI to model
        model.addAttribute("listBooks", bookService.searchBooks(keyword));
        model.addAttribute("keyword", keyword);
        return "search-results";
    }

    // MISSING: View missing books
    @GetMapping("/staff/books/missing")
    public String viewMissingBooks(Model model, HttpServletRequest request) { // Inject request
        addRequestUriToModel(model, request); // Add URI to model
        model.addAttribute("listBooks", bookService.getMissingBooks());
        return "missing-books";
    }
}