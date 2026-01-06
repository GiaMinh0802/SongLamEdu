package com.songlam.edu.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TransactionDTO {
    private String transactionId;
    private String type;
    private String studentName;
    private String receiverName;
    private String receiverAddress;
    private String cashierName;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateFrom;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateTo;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfRecorded;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfDocument;
    private String reason;
    private String amount;
    private String amountHidden;
    private String attachments;
    private String sourceDocuments;
    private String note;
    private String academicYearName;
    private String className;
    private String subjectName;
}
