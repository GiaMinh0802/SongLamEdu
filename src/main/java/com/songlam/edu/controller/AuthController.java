package com.songlam.edu.controller;

import com.songlam.edu.dto.RegisterDTO;
import com.songlam.edu.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            HttpSession session,
            Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/info";
        }

        if (error != null) {
            // Get error message and username from session
            String errorMessage = (String) session.getAttribute("LOGIN_ERROR_MESSAGE");
            String username = (String) session.getAttribute("LOGIN_USERNAME");

            if (errorMessage != null) {
                model.addAttribute("error", errorMessage);
                // Remove from session after use
                session.removeAttribute("LOGIN_ERROR_MESSAGE");
            }

            if (username != null && !username.isEmpty()) {
                model.addAttribute("username", username);
                // Remove from session after use
                session.removeAttribute("LOGIN_USERNAME");
            }
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String citizenId,
            @RequestParam String fullName,
            @RequestParam String dateOfBirth,
            @RequestParam Short sex,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam(required = false) String address,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // Parse date from dd/MM/yyyy format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate parsedDate = LocalDate.parse(dateOfBirth, formatter);

            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setCitizenId(citizenId);
            registerDTO.setFullName(fullName);
            registerDTO.setDateOfBirth(parsedDate);
            registerDTO.setSex(sex);
            registerDTO.setPhone(phone);
            registerDTO.setEmail(email);
            registerDTO.setAddress(address);
            registerDTO.setPassword(password);
            registerDTO.setConfirmPassword(confirmPassword);

            userService.registerCashier(registerDTO);

            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công!");
            return "redirect:/login";

        } catch (DateTimeParseException e) {
            model.addAttribute("error", "Ngày sinh không hợp lệ. Vui lòng sử dụng định dạng dd/MM/yyyy");
            model.addAttribute("citizenId", citizenId);
            model.addAttribute("fullName", fullName);
            model.addAttribute("dateOfBirth", dateOfBirth);
            model.addAttribute("sex", sex);
            model.addAttribute("phone", phone);
            model.addAttribute("email", email);
            model.addAttribute("address", address);
            return "register";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("citizenId", citizenId);
            model.addAttribute("fullName", fullName);
            model.addAttribute("dateOfBirth", dateOfBirth);
            model.addAttribute("sex", sex);
            model.addAttribute("phone", phone);
            model.addAttribute("email", email);
            model.addAttribute("address", address);
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại sau.");
            model.addAttribute("citizenId", citizenId);
            model.addAttribute("fullName", fullName);
            model.addAttribute("dateOfBirth", dateOfBirth);
            model.addAttribute("sex", sex);
            model.addAttribute("phone", phone);
            model.addAttribute("email", email);
            model.addAttribute("address", address);
            return "register";
        }
    }

    @GetMapping("/access-denied")
    public String showAccessDeniedPage() {
        return "access-denied";
    }
}
