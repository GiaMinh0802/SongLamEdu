package com.songlam.edu.dto;

import lombok.Data;

@Data
public class SubjectDTO {
    private Long id;
    private String subjectName;
    private Long classId;
    private String className;
}