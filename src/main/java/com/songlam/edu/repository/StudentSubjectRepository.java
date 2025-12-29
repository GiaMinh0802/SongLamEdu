package com.songlam.edu.repository;

import com.songlam.edu.entity.StudentSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentSubjectRepository extends JpaRepository<StudentSubject, Long> {

    boolean existsByStudentCitizenIdAndSubjectId(String citizenId, Long subjectId);

    Optional<StudentSubject> findByStudentCitizenIdAndSubjectId(String citizenId, Long subjectId);

}
