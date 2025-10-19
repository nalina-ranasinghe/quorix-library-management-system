package com.library.app.controller;

import com.library.app.dto.RegisterRequest;
import com.library.app.entity.User;
import com.library.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req, BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, br.getAllErrors().get(0).getDefaultMessage()));
        }
        try {
            User u = userService.registerEndUser(req);
            return ResponseEntity.ok(new ApiResponse(true, "Registration successful. Please login to continue.", "/"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "Something went wrong"));
        }
    }

    record ApiResponse(boolean success, String message, String redirectUrl) {
        public ApiResponse(boolean success, String message) {
            this(success, message, null);
        }
    }
}