package com.library.app.repository;

import com.library.app.entity.Book;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Book> findAll() {
        return jdbcTemplate.query("SELECT book_id as id, * FROM books ORDER BY book_id DESC",
                new BeanPropertyRowMapper<>(Book.class));
    }

    public Optional<Book> findById(Long id) {
        List<Book> list = jdbcTemplate.query("SELECT book_id as id, * FROM books WHERE book_id = ?",
                new BeanPropertyRowMapper<>(Book.class), id);
        return list.stream().findFirst();
    }

    public Optional<Book> findByIsbn(String isbn) {
        List<Book> list = jdbcTemplate.query("SELECT book_id as id, * FROM books WHERE isbn = ?",
                new BeanPropertyRowMapper<>(Book.class), isbn);
        return list.stream().findFirst();
    }

    public List<Book> findByStatus(String status) {
        return jdbcTemplate.query("SELECT book_id as id, * FROM books WHERE status = ?",
                new BeanPropertyRowMapper<>(Book.class), status);
    }

    public List<Book> findByTitleOrAuthorLike(String keyword) {
        String like = "%" + keyword + "%";
        return jdbcTemplate.query(
                "SELECT book_id as id, * FROM books WHERE LOWER(title) LIKE LOWER(?) OR LOWER(author) LIKE LOWER(?)",
                new BeanPropertyRowMapper<>(Book.class), like, like);
    }

    public Book save(Book book) {
        if (book.getId() == null) {
            // insert
            String sql = "INSERT INTO books(title, author, isbn, category, location, status, quantity, created_at, updated_at) VALUES(?,?,?,?,?,?,?,?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, book.getTitle());
                ps.setString(2, book.getAuthor());
                ps.setString(3, book.getIsbn());
                ps.setString(4, book.getCategory());
                ps.setString(5, book.getLocation());
                ps.setString(6, book.getStatus());
                ps.setInt(7, book.getQuantity());
                ps.setObject(8, java.time.LocalDateTime.now());
                ps.setObject(9, java.time.LocalDateTime.now());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                book.setId(key.longValue());
            }
            return book;
        } else {
            // update
            String sql = "UPDATE books SET title=?, author=?, isbn=?, category=?, location=?, status=?, quantity=?, updated_at=? WHERE book_id=?";
            jdbcTemplate.update(sql,
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.getCategory(),
                    book.getLocation(),
                    book.getStatus(),
                    book.getQuantity(),
                    java.time.LocalDateTime.now(),
                    book.getId());
            return book;
        }
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM books WHERE book_id = ?", id);
    }
}

