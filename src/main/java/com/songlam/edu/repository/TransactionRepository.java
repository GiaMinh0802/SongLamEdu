package com.songlam.edu.repository;

import com.songlam.edu.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query(value = """
        SELECT t.*
        FROM transactions t
        LEFT JOIN students s ON t.student_id = s.citizen_id
        LEFT JOIN person ps ON s.citizen_id = ps.citizen_id
        LEFT JOIN users u ON t.cashier_id = u.citizen_id
        LEFT JOIN person pu ON u.citizen_id = pu.citizen_id
        WHERE (:transactionId IS NULL OR t.transaction_number LIKE CONCAT('%', :transactionId, '%'))
          AND (:studentName IS NULL OR LOWER(ps.full_name) LIKE LOWER(CONCAT('%', :studentName, '%')))
          AND (:cashierName IS NULL OR LOWER(pu.full_name) LIKE LOWER(CONCAT('%', :cashierName, '%')))
          AND t.date_of_recorded >= COALESCE(:fromDate, t.date_of_recorded)
          AND t.date_of_recorded <= COALESCE(:toDate, t.date_of_recorded)
        ORDER BY t.date_of_recorded DESC, t.transaction_number DESC
        """, nativeQuery = true)
    Page<Transaction> search(
            @Param("transactionId") String transactionId,
            @Param("studentName") String studentName,
            @Param("cashierName") String cashierName,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );
}
