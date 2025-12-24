package com.songlam.edu.controller;

import com.songlam.edu.dto.BusinessInfoDTO;
import com.songlam.edu.entity.BusinessInfo;
import com.songlam.edu.service.CompanyInfoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/info")
public class InfoController {

    private final CompanyInfoService companyInfoService;

    public InfoController(CompanyInfoService companyInfoService) {
        this.companyInfoService = companyInfoService;
    }

    @GetMapping
    public String viewInfo(Model model) {
        BusinessInfo info = companyInfoService.getBusinessInfo().orElse(null);
        BusinessInfoDTO dto = companyInfoService.toDTO(info);
        model.addAttribute("info", dto);
        return "info";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String updateInfo(@ModelAttribute BusinessInfoDTO dto) {
        companyInfoService.updateBusinessInfo(dto);
        return "redirect:/info?updated=true";
    }
}
