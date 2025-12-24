package com.songlam.edu.controller;

import com.songlam.edu.dto.ChangePasswordDTO;
import com.songlam.edu.dto.MeDTO;
import com.songlam.edu.entity.User;
import com.songlam.edu.service.PersonService;
import com.songlam.edu.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/me")
public class MeController {

    private final UserService userService;

    private final PersonService personService;

    public MeController(UserService userService, PersonService personService) {
        this.userService = userService;
        this.personService = personService;
    }

    @GetMapping
    public String viewMe(Model model, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        MeDTO dto = userService.toDTOForMe(user);
        model.addAttribute("me", dto);
        return "me";
    }

    @PostMapping
    public String updateMe(@ModelAttribute MeDTO dto) {
        personService.updateMeInfo(dto);
        return "redirect:/me?updated=true";
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute ChangePasswordDTO dto, Authentication authentication) {

        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return "redirect:/me?error=true#password";
        }

        if (!userService.checkMatchPassword(dto.getOldPassword(), user.getPasswordHash())) {
            return "redirect:/me?error=true#password";
        }

        userService.updatePassword(user, dto.getNewPassword());
        return "redirect:/me?success=true#password";
    }
}
