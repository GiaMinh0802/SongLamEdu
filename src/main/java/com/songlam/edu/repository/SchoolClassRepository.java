package com.songlam.edu.repository;

import com.songlam.edu.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findByAcademicYearId(Long academicYearId);

    boolean existsByClassNameAndAcademicYearId(String className, Long academicYearId);

}
