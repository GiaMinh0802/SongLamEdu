package com.songlam.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SchoolClassDTO {
    private Long id;
    private String className;
    private Long academicYearId;
    private String academicYearName;
}