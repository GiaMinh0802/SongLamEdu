package com.songlam.edu.dto;

import lombok.Data;

@Data
public class SchoolClassDTO {
    private Long id;
    private String className;
    private Long academicYearId;
    private String academicYearName;
}