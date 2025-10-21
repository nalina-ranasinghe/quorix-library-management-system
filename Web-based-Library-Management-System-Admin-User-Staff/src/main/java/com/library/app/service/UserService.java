package com.library.app.service;

import com.library.app.dto.RegisterRequest;
import com.library.app.entity.Role;
import com.library.app.entity.User;
import com.library.app.repository.RoleRepository;
import com.library.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerEndUser(RegisterRequest req) {
        if (userRepo.existsByUsernameIgnoreCase(req.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        Role endUserRole = roleRepo.findByRoleName("END_USER")
                .orElseThrow(() -> new IllegalStateException("END_USER role not present"));

        User u = new User();
        u.setUsername(req.getUsername().trim());
        u.setFullName(req.getFullName().trim());
        u.setEmail(req.getEmail().trim());
        u.setPhone(req.getPhone().trim());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setRole("END_USER");
        u.setStatus("PENDING");

        User savedUser = userRepo.save(u);
        userRepo.linkUserToRole(savedUser.getUserId(), endUserRole.getRoleId());

        return savedUser;
    }

    // ===============================================
    // == NEW METHODS FOR MEMBERSHIP APPROVAL ==
    // ===============================================

    /**
     * Finds all users with a 'PENDING' status.
     * @return A list of users awaiting approval.
     */
    public List<User> findPendingUsers() {
        return userRepo.findByStatus("PENDING");
    }

    /**
     * Approves a user by changing their status to 'ACTIVE'.
     * @param userId The ID of the user to approve.
     */
    @Transactional
    public void approveUser(int userId) {
        userRepo.updateStatus(userId, "ACTIVE");
    }

    /**
     * Rejects a user by calling the existing deleteUser method.
     * @param userId The ID of the user to reject.
     */
    @Transactional
    public void rejectUser(int userId) {
        // This reuses your existing delete logic, which is good practice
        this.deleteUser(userId);
    }

    // ===============================================
    // == YOUR EXISTING METHODS ARE PRESERVED BELOW ==
    // ===============================================

    public List<User> findAllUsers() {
        return userRepo.findAll();
    }

    public Optional<User> findUserById(int id) {
        return userRepo.findById(id);
    }

    @Transactional
    public void saveUserByAdmin(User user) {
        boolean isNewUser = user.getUserId() == null || user.getUserId() == 0;

        if (isNewUser) {
            // ADDED: Check for duplicates before saving a NEW user
            if (userRepo.existsByUsernameIgnoreCase(user.getUsername())) {
                throw new IllegalArgumentException("Username '" + user.getUsername() + "' already exists.");
            }
            if (userRepo.existsByEmailIgnoreCase(user.getEmail())) {
                throw new IllegalArgumentException("Email '" + user.getEmail() + "' already exists.");
            }

            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            User savedUser = userRepo.save(user);
            Role role = roleRepo.findByRoleName(user.getRole()).orElseThrow();
            userRepo.linkUserToRole(savedUser.getUserId(), role.getRoleId());
        } else {
            // Logic for updating an existing user
            User existingUser = userRepo.findById(user.getUserId()).orElseThrow();
            if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
                existingUser.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            }
            existingUser.setUsername(user.getUsername());
            existingUser.setFullName(user.getFullName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            existingUser.setRole(user.getRole());
            existingUser.setStatus(user.getStatus());

            userRepo.update(existingUser);

            userRepo.clearUserRoles(existingUser.getUserId());
            Role role = roleRepo.findByRoleName(user.getRole()).orElseThrow();
            userRepo.linkUserToRole(existingUser.getUserId(), role.getRoleId());
        }
    }

    @Transactional
    public void deleteUser(int id) {
        userRepo.deleteById(id);
    }

    public boolean verifyUserForPasswordReset(String username, String email) {
        return userRepo.findByUsernameIgnoreCase(username)
                .map(user -> user.getEmail().equalsIgnoreCase(email))
                .orElse(false);
    }

    @Transactional
    public void resetUserPassword(String username, String newPassword) {
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.update(user);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepo.findByUsernameIgnoreCase(username);
    }

    @Transactional
    public void updateUserDetails(String currentUsername, String newFullName, String newEmail, String newPhone, String newPassword) {
        // Find the user by their current username
        User user = userRepo.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if the new email is already taken by another user
        userRepo.findByEmailIgnoreCase(newEmail).ifPresent(existingUser -> {
            if (!existingUser.getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("Email '" + newEmail + "' is already in use by another account.");
            }
        });

        // Update the personal details
        user.setFullName(newFullName);
        user.setEmail(newEmail);
        user.setPhone(newPhone);

        // Only update the password if a new one was actually entered
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
        }

        // Save the changes to the database
        userRepo.update(user);
    }
}