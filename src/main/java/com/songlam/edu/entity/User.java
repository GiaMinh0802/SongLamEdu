package com.songlam.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "citizen_id", length = 15)
    private String citizenId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false)
    private Short role = 0; // 0: Cashier, 1: Admin

    @Column(name = "is_active")
    private Boolean isActive = false;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "citizen_id")
    private Person person;

    @OneToMany(mappedBy = "cashier", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
