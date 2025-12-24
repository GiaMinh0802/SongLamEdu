package com.songlam.edu.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TransactionDTO {
    private String transactionId;
    private String studentName;
    private String cashierName;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateFrom;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateTo;
}
