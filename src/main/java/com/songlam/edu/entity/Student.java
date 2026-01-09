package com.songlam.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @Column(name = "citizen_id", length = 15)
    private String citizenId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status")
    private Short status = 1; // 1: Studying, 0: Stopped

    @Column(name = "studentSchool")
    private String studentSchool;

    @Column(name = "studentClass")
    private String studentClass;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "citizen_id")
    private Person person;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<StudentSubject> studentSubjects;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
