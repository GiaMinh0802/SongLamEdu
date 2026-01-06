
package com.songlam.edu.repository;

import com.songlam.edu.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    boolean existsByName(String name);

    Optional<AcademicYear> findByStartYear(Integer startYear);

    List<AcademicYear> findByStartYearGreaterThanEqual(Integer startYear);

    List<AcademicYear> findAllByOrderByStartYearAsc();

}