package com.songlam.edu.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class StudentDTO {
    private String citizenId;
    private String fullName;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;
    private Short sex; // 0: Male, 1: Female
    private String nationality;
    private String placeOfOrigin;
    private String placeOfResidence;
    private String address;
    private String phone;
    private String email;
    private Short status;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate endDate;
}
