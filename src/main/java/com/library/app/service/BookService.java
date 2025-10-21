package com.library.app.service;

import com.library.app.entity.Book;
import com.library.app.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import com.library.app.service.DatabaseService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private DatabaseService databaseService;

    // READ: Get all books
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // READ: Get a single book by its ID
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    // CREATE / UPDATE: Save a book
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    // DELETE: Delete a book by its ID
    public String deleteBook(Long id) {
        try {
            // Check if book exists
            Optional<Book> bookOpt = bookRepository.findById(id);
            if (bookOpt.isEmpty()) {
                return "Book not found";
            }

            // Check if book has any borrowing records
            if (databaseService.hasBorrowingRecords(id.intValue())) {
                return "Cannot delete book: It has borrowing records. Please return all borrowed copies first or contact administrator.";
            }

            // Safe to delete the book
            bookRepository.deleteById(id);
            return "Book deleted successfully";

        } catch (Exception e) {
            return "Error deleting book: " + e.getMessage();
        }
    }

    // --- New Service Methods ---

    /**
     * Searches for books where the title or author contains the given keyword.
     * @param keyword The search term.
     * @return A list of matching books.
     */
    public List<Book> searchBooks(String keyword) {
        return bookRepository.findByTitleOrAuthorLike(keyword);
    }

    /**
     * Retrieves all books marked with the status "Missing".
     * @return A list of missing books.
     */
    public List<Book> getMissingBooks() {
        return bookRepository.findByStatus("Missing");
    }


    public boolean isbnExists(@NotBlank(message = "ISBN is required.")
                              @Size(min = 10, max = 13, message = "ISBN must be between 10 and 13 digits.")
                              @Pattern(regexp = "^[0-9]*$", message = "ISBN must contain only numbers.") String isbn) {
        return bookRepository.findByIsbn(isbn).isPresent();
    }

	/**
	 * Checks whether an ISBN exists for a different book than the provided id.
	 * Used to validate uniqueness on updates.
	 */
	public boolean isbnExistsForOtherId(@NotBlank(message = "ISBN is required.")
	                                  @Size(min = 10, max = 13, message = "ISBN must be between 10 and 13 digits.")
	                                  @Pattern(regexp = "^[0-9]*$", message = "ISBN must contain only numbers.") String isbn,
	                                  Long currentBookId) {
		return bookRepository.findByIsbn(isbn)
		        .map(found -> !found.getId().equals(currentBookId))
		        .orElse(false);
	}
}