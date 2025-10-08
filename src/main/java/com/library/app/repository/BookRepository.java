package com.library.app.repository;

import com.library.app.entity.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Book> bookRowMapper = (rs, rowNum) -> {
        Book book = new Book();
        book.setBookId(rs.getInt("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setCategory(rs.getString("category"));
        book.setLocation(rs.getString("location"));
        book.setQuantity(rs.getInt("quantity"));
        book.setStatus(rs.getString("status"));
        book.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null) {
            book.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return book;
    };

    public Optional<Book> findById(int id) {
        String sql = "SELECT * FROM Books WHERE book_id = ?";
        return jdbcTemplate.query(sql, new Object[]{id}, bookRowMapper).stream().findFirst();
    }

    // --- NEW METHODS ---

    /**
     * Fetches all books from the database.
     * @return A list of all books.
     */
    public List<Book> findAll() {
        String sql = "SELECT * FROM Books ORDER BY title";
        return jdbcTemplate.query(sql, bookRowMapper);
    }

    /**
     * Searches for books where the title contains the given keyword (case-insensitive).
     * @param keyword The search term.
     * @return A list of matching books.
     */
    public List<Book> searchByTitleContaining(String keyword) {
        String sql = "SELECT * FROM Books WHERE LOWER(title) LIKE LOWER(?) ORDER BY title";
        // The '%' symbols are wildcards for the LIKE search
        return jdbcTemplate.query(sql, new Object[]{"%" + keyword + "%"}, bookRowMapper);
    }
}