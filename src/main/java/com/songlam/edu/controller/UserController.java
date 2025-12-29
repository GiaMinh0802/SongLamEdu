package com.songlam.edu.controller;

import com.songlam.edu.dto.PersonDTO;
import com.songlam.edu.entity.User;
import com.songlam.edu.service.PersonService;
import com.songlam.edu.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private final PersonService personService;

    public UserController(UserService userService, PersonService personService) {
        this.userService = userService;
        this.personService = personService;
    }

    @GetMapping
    public String viewUsers(Model model,
                            @RequestParam(value = "citizenId", required = false) String citizenId,
                            @RequestParam(value = "fullName", required = false) String fullName,
                            @RequestParam(value = "phone", required = false) String phone,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {

        Page<User> users = userService.search(citizenId, fullName, phone, page, size);

        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalItems", users.getTotalElements());
        model.addAttribute("citizenId", citizenId);
        model.addAttribute("fullName", fullName);
        model.addAttribute("phone", phone);
        return "users";
    }

    @GetMapping("/active/{id}")
    public String activeUser(@PathVariable String id) {
        userService.activeUser(id);
        return "redirect:/users?active=true";
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable String id, Model model) {
        User user = userService.findById(id).orElse(null);
        PersonDTO dto = userService.toDTO(user);
        model.addAttribute("user", dto);
        return "user-detail";
    }

    @PostMapping("/detail")
    public String updateUser(@ModelAttribute PersonDTO dto) {
        personService.updateInfo(dto);
        return "redirect:/users/detail/" + dto.getCitizenId() + "?updated=true";
    }
}
