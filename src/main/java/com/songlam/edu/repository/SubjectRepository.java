package com.songlam.edu.repository;

import com.songlam.edu.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findBySchoolClassId(Long classId);

    boolean existsBySubjectNameAndSchoolClassId(String subjectName, Long classId);
}