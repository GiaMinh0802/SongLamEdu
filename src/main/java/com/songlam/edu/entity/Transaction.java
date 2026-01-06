package com.songlam.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @Column(name = "transaction_number", length = 8)
    private String transactionNumber;

    @Column(name = "type", nullable = false, length = 2)
    private String type;

    @Column(name = "date_of_recorded", nullable = false)
    private LocalDate dateOfRecorded;

    @Column(name = "date_of_document", nullable = false)
    private LocalDate dateOfDocument;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "attachments")
    private String attachments;

    @Column(name = "source_documents")
    private String sourceDocuments;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "citizen_id")
    private Student student;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "receiver_adress")
    private String receiverAdress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", referencedColumnName = "citizen_id")
    private User cashier;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
