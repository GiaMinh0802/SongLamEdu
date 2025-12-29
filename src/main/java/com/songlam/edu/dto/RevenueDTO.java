package com.songlam.edu.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RevenueDTO {
    private String id;
    private LocalDate paymentDate;
    private String academicYearName;
    private String className;
    private String subjectName;
    private Long amount;
}
