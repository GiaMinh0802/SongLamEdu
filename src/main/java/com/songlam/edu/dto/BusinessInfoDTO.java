package com.songlam.edu.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class BusinessInfoDTO {
    private String companyCode;
    private String businessName;
    private String abbreviatedName;
    private String businessType;
    private String taxCode;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate establishedDate;
    private String businessRegistrationCode;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate issuedDate;
    private String issuedPlace;
    private String representativeTitle;
    private String ownerCitizenId;
    private String representativeName;
    private String address;
    private String phone;
    private String fax;
    private String email;
    private String website;
}
