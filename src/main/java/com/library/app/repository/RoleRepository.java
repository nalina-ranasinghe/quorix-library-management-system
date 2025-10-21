package com.library.app.repository;

import com.library.app.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Role> roleRowMapper = (rs, rowNum) -> new Role(
            rs.getInt("role_id"),
            rs.getString("role_name")
    );

    public Optional<Role> findByRoleName(String roleName) {
        String sql = "SELECT * FROM Roles WHERE role_name = ?";
        List<Role> roles = jdbcTemplate.query(sql, new Object[]{roleName}, roleRowMapper);
        return roles.stream().findFirst();
    }
}