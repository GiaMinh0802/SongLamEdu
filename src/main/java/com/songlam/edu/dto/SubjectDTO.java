package com.songlam.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDTO {

    private Long id;
    private String subjectName;
    private Long classId;
    private String className;
}