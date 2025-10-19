package com.library.app.controller;

import com.library.app.service.BookService;
import com.library.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final UserService userService;
    private final BookService bookService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        model.addAttribute("books", bookService.getAllBooks());
        if (principal != null) {
            userService.findUserByUsername(principal.getName())
                    .ifPresent(user -> model.addAttribute("currentUser", user));
        }
        return "home";
    }
}