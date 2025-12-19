package com.songlam.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    private String citizenId;
    private String fullName;
    private LocalDate dateOfBirth;
    private Short sex; // 0: Male, 1: Female
    private String phone;
    private String email;
    private String address;
    private String password;
    private String confirmPassword;
}
