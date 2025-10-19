package com.library.app.repository;

import com.library.app.dto.UserReservationDto;
import com.library.app.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public void save(Reservation reservation) {
        String sql = "INSERT INTO Reservations (user_id, book_id, reserved_at, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, reservation.getUserId(), reservation.getBookId(), reservation.getReservedAt(), reservation.getStatus());
    }

    public Optional<Reservation> findById(int reservationId) {
        String sql = "SELECT * FROM Reservations WHERE reservation_id = ?";
        RowMapper<Reservation> rowMapper = (rs, rowNum) -> {
            Reservation res = new Reservation();
            res.setReservationId(rs.getInt("reservation_id"));
            res.setUserId(rs.getInt("user_id"));
            res.setBookId(rs.getInt("book_id"));
            res.setReservedAt(rs.getTimestamp("reserved_at").toLocalDateTime());
            res.setStatus(rs.getString("status"));
            return res;
        };
        return jdbcTemplate.query(sql, new Object[]{reservationId}, rowMapper).stream().findFirst();
    }

    public List<UserReservationDto> findActiveReservationsByUserId(int userId) {
        String sql = "SELECT r.reservation_id, b.title, b.author, r.reserved_at " +
                "FROM Reservations r JOIN Books b ON r.book_id = b.book_id " +
                "WHERE r.user_id = ? AND r.status = 'ACTIVE' ORDER BY r.reserved_at DESC";
        RowMapper<UserReservationDto> rowMapper = (rs, rowNum) -> {
            UserReservationDto dto = new UserReservationDto();
            dto.setReservationId(rs.getInt("reservation_id"));
            dto.setBookTitle(rs.getString("title"));
            dto.setBookAuthor(rs.getString("author"));
            dto.setReservedAt(rs.getTimestamp("reserved_at").toLocalDateTime());
            return dto;
        };
        return jdbcTemplate.query(sql, new Object[]{userId}, rowMapper);
    }

    public void deleteById(int reservationId) {
        String sql = "DELETE FROM Reservations WHERE reservation_id = ?";
        jdbcTemplate.update(sql, reservationId);
    }

    public boolean hasActiveReservation(int userId, int bookId) {
        String sql = "SELECT COUNT(*) FROM Reservations WHERE user_id = ? AND book_id = ? AND status = 'ACTIVE'";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{userId, bookId}, Integer.class);
        return count != null && count > 0;
    }
}