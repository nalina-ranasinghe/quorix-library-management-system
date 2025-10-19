package com.library.app.controller;

import com.library.app.entity.Book;
import com.library.app.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class
BookApiController {

    private final BookService bookService;

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword) {
        return bookService.searchBooks(keyword);
    }
}