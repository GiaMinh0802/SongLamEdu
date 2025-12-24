package com.songlam.edu.controller;

import com.songlam.edu.dto.RegisterDTO;
import com.songlam.edu.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "success", required = false) boolean success,
                                HttpServletRequest request, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/me";
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            String error = (String) session.getAttribute("LOGIN_ERROR");
            String username = (String) session.getAttribute("LOGIN_USERNAME");

            if (error != null) {
                model.addAttribute("error", error);
                session.removeAttribute("LOGIN_ERROR");
            }
            if (username != null) {
                model.addAttribute("username", username);
                session.removeAttribute("LOGIN_USERNAME");
            }
        }
        model.addAttribute("success", success);
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterDTO dto, Model model) {
        try {
            userService.registerCashier(dto);
            return "redirect:/login?success=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerDTO", dto);
            return "register";
        }
    }

    @GetMapping("/access-denied")
    public String showAccessDeniedPage() {
        return "error/access-denied";
    }
}
