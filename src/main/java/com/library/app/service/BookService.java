package com.library.app.service;

import com.library.app.entity.Book;
import com.library.app.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    /**
     * Gets all books from the repository.
     * @return A list of all books.
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Searches for books by a keyword in the title.
     * @param keyword The search term.
     * @return A list of books matching the keyword.
     */
    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchByKeyword(keyword);
    }
}