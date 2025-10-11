// Updated UserService.java with missing methods
package com.library.app.service;

import com.library.app.dto.RegisterRequest;
import com.library.app.entity.User;
import com.library.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; // Add this import
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Existing methods (from previous suggestions)
    public User registerEndUser(RegisterRequest req) {
        // Use getter methods instead of direct field access
        if (userRepository.existsByUsernameIgnoreCase(req.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword())); // Fixed method call
        user.setRole("END_USER");
        user.setStatus("PENDING");
        user = userRepository.save(user);
        userRepository.linkUserToRole(user.getUserId(), 3);
        return user;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(int id) {
        return userRepository.findById(id);
    }

    public void saveUserByAdmin(User user) {
        if (user.getUserId() == null || user.getUserId() == 0) {
            // New user
            if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash())); // Hash plain password
            user = userRepository.save(user);
            int roleId = getRoleId(user.getRole());
            userRepository.linkUserToRole(user.getUserId(), roleId);
        } else {
            // Update user
            Optional<User> existing = findUserById(user.getUserId());
            if (existing.isPresent()) {
                User ex = existing.get();
                if (!ex.getUsername().equalsIgnoreCase(user.getUsername()) && userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
                    throw new IllegalArgumentException("Username already exists");
                }
                if (!ex.getEmail().equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmailIgnoreCase(user.getEmail())) {
                    throw new IllegalArgumentException("Email already exists");
                }
                if (!user.getPasswordHash().equals(ex.getPasswordHash()) && !user.getPasswordHash().startsWith("$2a$")) {
                    user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
                }
                userRepository.update(user);
                userRepository.clearUserRoles(user.getUserId());
                int roleId = getRoleId(user.getRole());
                userRepository.linkUserToRole(user.getUserId(), roleId);
            }
        }
    }

    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    public void approveUser(int id) {
        findUserById(id).ifPresent(user -> {
            if ("PENDING".equals(user.getStatus())) {
                user.setStatus("ACTIVE");
                userRepository.update(user);
            }
        });
    }

    private int getRoleId(String role) {
        return switch (role) {
            case "ADMIN" -> 1;
            case "STAFF" -> 2;
            default -> 3; // END_USER
        };
    }

    // NEW: Missing methods from ViewController errors

    public Optional<User> findUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        user.ifPresent(u -> {
            Set<String> roles = userRepository.findRolesByUserId(u.getUserId());
            u.setRoles(roles);
        });
        return user;
    }

    public boolean verifyUserForPasswordReset(String username, String email) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        return user.isPresent() && user.get().getEmail().equalsIgnoreCase(email);
    }

    public void resetUserPassword(String username, String newPassword) {
        findUserByUsername(username).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.update(user);
        });
    }

    public void updateUserDetails(String username, String fullName, String email, String phone, String newPassword) {
        findUserByUsername(username).ifPresent(user -> {
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            if (newPassword != null && !newPassword.isEmpty()) {
                user.setPasswordHash(passwordEncoder.encode(newPassword));
            }
            // Check for duplicates if changed
            if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
            userRepository.update(user);
        });
    }
}