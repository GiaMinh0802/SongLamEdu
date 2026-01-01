package com.songlam.edu.repository;

import com.songlam.edu.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    @Query(value = """
        SELECT s.*
        FROM students s
        JOIN person p ON s.citizen_id = p.citizen_id
        WHERE (:citizenId IS NULL OR p.citizen_id LIKE CONCAT('%', :citizenId, '%'))
          AND (:fullName IS NULL OR LOWER(p.full_name) LIKE LOWER(CONCAT('%', :fullName, '%')))
          AND (:phone IS NULL OR p.phone LIKE CONCAT('%', :phone, '%'))
        ORDER BY p.updated_at DESC
        """, nativeQuery = true)
    Page<Student> search(
            @Param("citizenId") String citizenId,
            @Param("fullName") String fullName,
            @Param("phone") String phone,
            Pageable pageable
    );

    @Query(value = """
        SELECT s.*
        FROM students s
        JOIN person p ON s.citizen_id = p.citizen_id
        JOIN student_subjects ss ON s.citizen_id = ss.student_id
        WHERE ss.subject_id = :subjectId
          AND (:citizenId IS NULL OR s.citizen_id LIKE CONCAT('%', :citizenId, '%'))
          AND (:fullName IS NULL OR LOWER(p.full_name) LIKE LOWER(CONCAT('%', :fullName, '%')))
          AND (:phone IS NULL OR p.phone LIKE CONCAT('%', :phone, '%'))
        ORDER BY p.updated_at DESC
        """,
                countQuery = """
        SELECT COUNT(s.citizen_id)
        FROM students s
        JOIN person p ON s.citizen_id = p.citizen_id
        JOIN student_subjects ss ON s.citizen_id = ss.student_id
        WHERE ss.subject_id = :subjectId
          AND (:citizenId IS NULL OR s.citizen_id LIKE CONCAT('%', :citizenId, '%'))
          AND (:fullName IS NULL OR LOWER(p.full_name) LIKE LOWER(CONCAT('%', :fullName, '%')))
          AND (:phone IS NULL OR p.phone LIKE CONCAT('%', :phone, '%'))
        """, nativeQuery = true)
    Page<Student> findBySubjectIdAndFilters(
            @Param("subjectId") Long subjectId,
            @Param("citizenId") String citizenId,
            @Param("fullName") String fullName,
            @Param("phone") String phone,
            Pageable pageable
    );

    @Query(value = """
        SELECT s.*
        FROM students s
        JOIN person p ON s.citizen_id = p.citizen_id
        WHERE s.status = 1
          AND (:fullName IS NULL OR LOWER(p.full_name) LIKE LOWER(CONCAT('%', :fullName, '%')))
          AND s.citizen_id NOT IN (
              SELECT ss.student_id FROM student_subjects ss WHERE ss.subject_id = :subjectId
          )
        ORDER BY p.date_of_birth ASC
        """, nativeQuery = true)
    Page<Student> searchActiveStudentsNotInSubject(
            @Param("fullName") String fullName,
            @Param("subjectId") Long subjectId,
            Pageable pageable
    );

    @Query(value = """
    SELECT DISTINCT s.*
    FROM students s
    JOIN person p ON s.citizen_id = p.citizen_id
    JOIN student_subjects ss ON s.citizen_id = ss.student_id
    JOIN subjects sub ON ss.subject_id = sub.id
    JOIN classes c ON sub.class_id = c.id
    WHERE (:keyword IS NULL OR LOWER(s.citizen_id) LIKE LOWER(:keyword) OR LOWER(p.full_name) LIKE LOWER(:keyword))
      AND (:yearId IS NULL OR c.academic_year_id = :yearId)
      AND (:classId IS NULL OR c.id = :classId)
      AND (:subjectId IS NULL OR sub.id = :subjectId)
    ORDER BY s.citizen_id ASC
    """,
            countQuery = """
    SELECT COUNT(DISTINCT s.citizen_id)
    FROM students s
    JOIN person p ON s.citizen_id = p.citizen_id
    JOIN student_subjects ss ON s.citizen_id = ss.student_id
    JOIN subjects sub ON ss.subject_id = sub.id
    JOIN classes c ON sub.class_id = c.id
    WHERE (:keyword IS NULL OR LOWER(s.citizen_id) LIKE LOWER(:keyword) OR LOWER(p.full_name) LIKE LOWER(:keyword))
      AND (:yearId IS NULL OR c.academic_year_id = :yearId)
      AND (:classId IS NULL OR c.id = :classId)
      AND (:subjectId IS NULL OR sub.id = :subjectId)
    """,
            nativeQuery = true)
    Page<Student> searchByKeywordAndFilters(
            @Param("keyword") String keyword,
            @Param("yearId") Long yearId,
            @Param("classId") Long classId,
            @Param("subjectId") Long subjectId,
            Pageable pageable);
}
