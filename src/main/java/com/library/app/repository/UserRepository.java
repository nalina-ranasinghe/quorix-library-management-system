package com.library.app.repository;

import com.library.app.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("password_hash"),
            rs.getString("role"),
            rs.getString("status"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null,
            new HashSet<>() // Roles will be loaded separately
    );

    public List<User> findAll() {
        String sql = "SELECT * FROM Users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, new Object[]{id}, userRowMapper);
        return users.stream().findFirst();
    }

    public Optional<User> findByUsernameIgnoreCase(String username) {
        String sql = "SELECT * FROM Users WHERE LOWER(username) = LOWER(?)";
        List<User> users = jdbcTemplate.query(sql, new Object[]{username}, userRowMapper);
        return users.stream().findFirst();
    }

    public Optional<User> findByEmailIgnoreCase(String email) {
        String sql = "SELECT * FROM Users WHERE LOWER(email) = LOWER(?)";
        List<User> users = jdbcTemplate.query(sql, new Object[]{email}, userRowMapper);
        return users.stream().findFirst();
    }

    public boolean existsByUsernameIgnoreCase(String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE LOWER(username) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{username}, Integer.class);
        return count != null && count > 0;
    }

    public boolean existsByEmailIgnoreCase(String email) {
        String sql = "SELECT COUNT(*) FROM Users WHERE LOWER(email) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{email}, Integer.class);
        return count != null && count > 0;
    }

    public User save(User user) {
        String sql = "INSERT INTO Users (username, full_name, email, phone, password_hash, role, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getPasswordHash());
            ps.setString(6, user.getRole());
            ps.setString(7, user.getStatus());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            user.setUserId(keyHolder.getKey().intValue());
        }
        return user;
    }

    public int update(User user) {
        String sql = "UPDATE Users SET username = ?, full_name = ?, email = ?, phone = ?, password_hash = ?, role = ?, status = ?, updated_at = GETDATE() WHERE user_id = ?";
        return jdbcTemplate.update(sql,
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getPasswordHash(),
                user.getRole(),
                user.getStatus(),
                user.getUserId());
    }

    public Set<String> findRolesByUserId(int userId) {
        String sql = "SELECT r.role_name FROM Roles r JOIN UserRoles ur ON r.role_id = ur.role_id WHERE ur.user_id = ?";
        List<String> roles = jdbcTemplate.queryForList(sql, new Object[]{userId}, String.class);
        return new HashSet<>(roles);
    }

    public void linkUserToRole(int userId, int roleId) {
        String sql = "INSERT INTO UserRoles (user_id, role_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, roleId);
    }

    public void clearUserRoles(int userId) {
        String sql = "DELETE FROM UserRoles WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public int deleteById(int id) {
        // Step 1: Delete from the join table first to satisfy foreign key constraints
        String deleteRolesSql = "DELETE FROM UserRoles WHERE user_id = ?";
        jdbcTemplate.update(deleteRolesSql, id);

        // Step 2: Delete the user from the main table
        String deleteUserSql = "DELETE FROM Users WHERE user_id = ?";
        return jdbcTemplate.update(deleteUserSql, id);
    }

    // Method to count new users in the last 30 days
    public int countNewUsersLast30Days() {
        String sql = "SELECT COUNT(*) FROM Users WHERE created_at >= DATEADD(day, -30, GETDATE())";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return (count != null) ? count : 0;
    }
}