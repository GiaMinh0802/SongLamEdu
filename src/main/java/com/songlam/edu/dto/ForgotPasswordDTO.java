package com.songlam.edu.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ForgotPasswordDTO {
    private String citizenId;
    private String fullName;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;
    private String phone;
    private String email;
}