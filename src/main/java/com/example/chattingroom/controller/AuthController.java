package com.example.chattingroom.controller;

import com.example.chattingroom.dto.SignupRequest;
import com.example.chattingroom.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/")
    public String Index() {
        return "redirect:/signin";
    }

    @GetMapping("/signin")
    public String showSigninPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        return "signin";
    }

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String register(@ModelAttribute SignupRequest request, Model model) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Username and password are required");
            return "signup";
        }

        boolean success = userService.register(username, password);
        if (success) {
            log.info("New user {} registered", username);
            model.addAttribute("message", "Registration successful. Please sign in.");
            return "signin";
        } else {
            model.addAttribute("error", "Username already exists");
            return "signup";
        }
    }

    // Logout is handled by Spring Security automatically via /logout
}
