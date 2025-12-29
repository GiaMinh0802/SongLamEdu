package com.songlam.edu.controller;

import com.songlam.edu.dto.ForgotPasswordDTO;
import com.songlam.edu.dto.RegisterDTO;
import com.songlam.edu.dto.ResetPasswordDTO;
import com.songlam.edu.service.PasswordResetService;
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
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
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

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@ModelAttribute ForgotPasswordDTO dto, Model model) {
        try {
            String token = passwordResetService.verifyUserAndCreateToken(dto);
            return "redirect:/reset-password?token=" + token;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("forgotPasswordDTO", dto);
            return "forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam String token, Model model) {
        if (!passwordResetService.validateToken(token)) {
            model.addAttribute("error", "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn");
            return "redirect:/forgot-password";
        }
        model.addAttribute("token", token);
        model.addAttribute("resetPasswordDTO", new ResetPasswordDTO());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@ModelAttribute ResetPasswordDTO dto, Model model) {
        try {
            passwordResetService.resetPassword(dto);
            return "redirect:/login?resetSuccess=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", dto.getToken());
            model.addAttribute("resetPasswordDTO", dto);
            return "reset-password";
        }
    }

    @GetMapping("/access-denied")
    public String showAccessDeniedPage() {
        return "error/access-denied";
    }
}
