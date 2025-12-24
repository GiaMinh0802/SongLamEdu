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
        ORDER BY s.updated_at DESC
        """, nativeQuery = true)
    Page<Student> search(
            @Param("citizenId") String citizenId,
            @Param("fullName") String fullName,
            @Param("phone") String phone,
            Pageable pageable
    );
}
