package com.songlam.edu.repository;

import com.songlam.edu.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
          AND t.type LIKE 'PT'
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

    List<Transaction> findByStudentCitizenId(String citizenId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.dateOfRecorded BETWEEN :start AND :end AND t.type = :type")
    BigDecimal sumAmountByDateRangeAndType(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("type") String type);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.dateOfRecorded BETWEEN :start AND :end AND t.type = :type")
    Long countByDateRangeAndType(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("type") String type);

    @Query("SELECT s.subjectName, SUM(t.amount) as total FROM Transaction t LEFT JOIN t.subject s WHERE t.dateOfRecorded BETWEEN :start AND :end AND t.type = :type GROUP BY s.subjectName ORDER BY total DESC")
    List<Object[]> sumAmountGroupBySubjectAndType(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("type") String type);

    @Query("SELECT c.className, SUM(t.amount) as total FROM Transaction t LEFT JOIN t.schoolClass c WHERE t.dateOfRecorded BETWEEN :start AND :end AND t.type = :type GROUP BY c.className ORDER BY total DESC")
    List<Object[]> sumAmountGroupByClassAndType(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("type") String type);

    @Query("SELECT t.dateOfRecorded, SUM(t.amount) FROM Transaction t WHERE t.dateOfRecorded BETWEEN :start AND :end AND t.type = :type GROUP BY t.dateOfRecorded ORDER BY t.dateOfRecorded")
    List<Object[]> sumAmountGroupByDayAndType(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("type") String type);

    @Query("SELECT MONTH(t.dateOfRecorded), SUM(t.amount) FROM Transaction t WHERE t.dateOfRecorded BETWEEN :start AND :end AND t.type = :type GROUP BY MONTH(t.dateOfRecorded) ORDER BY MONTH(t.dateOfRecorded)")
    List<Object[]> sumAmountGroupByMonthAndType(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("type") String type);

    List<Transaction> findByDateOfRecordedBetweenOrderByDateOfRecordedAsc(LocalDate startDate, LocalDate endDate);
}