package com.songlam.edu.repository;

import com.songlam.edu.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findByAcademicYearId(Long academicYearId);

    boolean existsByClassNameAndAcademicYearId(String className, Long academicYearId);

    @Query("SELECT DISTINCT sc FROM SchoolClass sc WHERE sc.academicYear.startYear >= :startYear")
    List<SchoolClass> findByAcademicYearStartYearGreaterThanEqual(@Param("startYear") Integer startYear);
}