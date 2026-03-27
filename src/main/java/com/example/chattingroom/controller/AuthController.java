package com.example.chattingroom.controller;

import com.example.chattingroom.dto.UserDto;
import com.example.chattingroom.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/signin")
    public String showSigninPage() {
        return "signin";
    }

    @PostMapping("/signin")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpServletRequest request, Model model) {
        UserDto user = userService.login(username, password);
        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user.getUsername());
            log.info("User {} logged in", user.getUsername());
            return "redirect:/room";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "signin";
        }
    }

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String register(@RequestParam String username, @RequestParam String password, Model model) {
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

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.info("User {} logged out", session.getAttribute("user"));
            session.invalidate();
        }
        return "redirect:/signin";
    }
}
