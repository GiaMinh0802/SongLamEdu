package com.songlam.edu.repository;

import com.songlam.edu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByPersonEmail(String email);

    boolean existsByPersonEmail(String email);

    @Query(value = """
        SELECT u.*
        FROM users u
        JOIN person p ON u.citizen_id = p.citizen_id
        WHERE (:citizenId IS NULL OR p.citizen_id LIKE CONCAT('%', :citizenId, '%'))
          AND (:fullName IS NULL OR LOWER(p.full_name) LIKE LOWER(CONCAT('%', :fullName, '%')))
          AND (:phone IS NULL OR p.phone LIKE CONCAT('%', :phone, '%'))
          AND (u.role = 0)
        ORDER BY u.updated_at DESC
        """, nativeQuery = true)
    Page<User> search(
            @Param("citizenId") String citizenId,
            @Param("fullName") String fullName,
            @Param("phone") String phone,
            Pageable pageable
    );
}
