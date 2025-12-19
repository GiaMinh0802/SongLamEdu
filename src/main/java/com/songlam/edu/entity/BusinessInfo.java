package com.songlam.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BusinessInfo {

    @Id
    @Column(name = "company_code", length = 8)
    private String companyCode;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "abbreviated_name", nullable = false)
    private String abbreviatedName;

    @Column(name = "business_type", nullable = false)
    private String businessType;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_citizen_id", referencedColumnName = "citizen_id")
    private Person owner;

    @Column(name = "tax_code", length = 20, nullable = false)
    private String taxCode;

    @Column(name = "established_date", nullable = false)
    private LocalDate establishedDate;

    @Column(name = "business_registration_code", length = 10, nullable = false)
    private String businessRegistrationCode;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "issued_place", nullable = false)
    private String issuedPlace;

    @Column(name = "representative_title", nullable = false)
    private String representativeTitle;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
