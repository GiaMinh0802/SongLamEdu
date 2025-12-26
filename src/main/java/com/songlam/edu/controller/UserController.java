package com.songlam.edu.controller;

import com.songlam.edu.entity.User;
import com.songlam.edu.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String viewUsers(Model model,
                            @RequestParam(value = "citizenId", required = false) String citizenId,
                            @RequestParam(value = "fullName", required = false) String fullName,
                            @RequestParam(value = "phone", required = false) String phone,
                            @RequestParam(value = "page", required = false) Integer page,
                            @RequestParam(value = "size", required = false) Integer size) {

        Page<User> users = userService.search(citizenId, fullName, phone, page, size);

        model.addAttribute("users", users);
        model.addAttribute("citizenId", citizenId);
        model.addAttribute("fullName", fullName);
        model.addAttribute("phone", phone);
        return "users";
    }

    @GetMapping("/active/{userId}")
    public String activeUser(@PathVariable String userId) {
        userService.activeUser(userId);
        return "redirect:/users?active=true";
    }
}
