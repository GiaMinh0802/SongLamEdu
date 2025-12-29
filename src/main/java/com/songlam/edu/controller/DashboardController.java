package com.songlam.edu.controller;

import com.songlam.edu.repository.TransactionRepository;
import com.songlam.edu.service.ExcelReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final TransactionRepository transactionRepository;
    private final ExcelReportService excelReportService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate now = LocalDate.now();
        model.addAttribute("currentMonth", now.getMonthValue());
        model.addAttribute("currentQuarter", (now.getMonthValue() - 1) / 3 + 1);
        model.addAttribute("currentYear", now.getYear());

        List<Integer> years = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            years.add(2025 + i);
        }
        Collections.sort(years);
        model.addAttribute("years", years);

        return "dashboard";
    }

    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    public Map<String, Object> getStats(
            @RequestParam String type,
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter) {

        LocalDate startDate;
        LocalDate endDate;

        switch (type) {
            case "month":
                startDate = LocalDate.of(year, month, 1);
                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                break;
            case "quarter":
                int startMonth = (quarter - 1) * 3 + 1;
                startDate = LocalDate.of(year, startMonth, 1);
                endDate = startDate.plusMonths(2).withDayOfMonth(startDate.plusMonths(2).lengthOfMonth());
                break;
            case "year":
                startDate = LocalDate.of(year, 1, 1);
                endDate = LocalDate.of(year, 12, 31);
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }

        return buildStats(startDate, endDate, type, year, month, quarter);
    }

    private Map<String, Object> buildStats(LocalDate startDate, LocalDate endDate,
                                           String type, int year, Integer month, Integer quarter) {
        Map<String, Object> result = new HashMap<>();

        // Tổng thu (PT)
        BigDecimal totalRevenue = transactionRepository.sumAmountByDateRangeAndType(startDate, endDate, "PT");
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // Tổng chi (PC)
        BigDecimal totalExpense = transactionRepository.sumAmountByDateRangeAndType(startDate, endDate, "PC");
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        // Số giao dịch thu
        Long revenueCount = transactionRepository.countByDateRangeAndType(startDate, endDate, "PT");
        if (revenueCount == null) revenueCount = 0L;

        // Số giao dịch chi
        Long expenseCount = transactionRepository.countByDateRangeAndType(startDate, endDate, "PC");
        if (expenseCount == null) expenseCount = 0L;

        // Số dư = Thu - Chi
        BigDecimal balance = totalRevenue.subtract(totalExpense);

        result.put("totalRevenue", totalRevenue);
        result.put("totalExpense", totalExpense);
        result.put("balance", balance);
        result.put("revenueCount", revenueCount);
        result.put("expenseCount", expenseCount);

        // Doanh thu theo môn học (chỉ PT)
        List<Object[]> bySubject = transactionRepository.sumAmountGroupBySubjectAndType(startDate, endDate, "PT");
        List<Map<String, Object>> subjectStats = new ArrayList<>();
        for (Object[] row : bySubject) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", row[0] != null ? row[0] : "Không xác định");
            item.put("amount", row[1] != null ? row[1] : BigDecimal.ZERO);
            subjectStats.add(item);
        }
        result.put("bySubject", subjectStats);

        // Doanh thu theo lớp học (chỉ PT)
        List<Object[]> byClass = transactionRepository.sumAmountGroupByClassAndType(startDate, endDate, "PT");
        List<Map<String, Object>> classStats = new ArrayList<>();
        for (Object[] row : byClass) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", row[0] != null ? row[0] : "Không xác định");
            item.put("amount", row[1] != null ? row[1] : BigDecimal.ZERO);
            classStats.add(item);
        }
        result.put("byClass", classStats);

        // Timeline chart data
        List<Map<String, Object>> timelineData = new ArrayList<>();

        if ("month".equals(type)) {
            // Theo tuần trong tháng
            List<Object[]> revenueByDay = transactionRepository.sumAmountGroupByDayAndType(startDate, endDate, "PT");
            List<Object[]> expenseByDay = transactionRepository.sumAmountGroupByDayAndType(startDate, endDate, "PC");

            Map<Integer, BigDecimal> revenueByWeek = new HashMap<>();
            Map<Integer, BigDecimal> expenseByWeek = new HashMap<>();

            WeekFields weekFields = WeekFields.of(Locale.getDefault());

            for (Object[] row : revenueByDay) {
                LocalDate date = (LocalDate) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                int weekOfMonth = date.get(weekFields.weekOfMonth());
                revenueByWeek.merge(weekOfMonth, amount, BigDecimal::add);
            }

            for (Object[] row : expenseByDay) {
                LocalDate date = (LocalDate) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                int weekOfMonth = date.get(weekFields.weekOfMonth());
                expenseByWeek.merge(weekOfMonth, amount, BigDecimal::add);
            }

            int maxWeek = endDate.get(weekFields.weekOfMonth());
            for (int week = 1; week <= maxWeek; week++) {
                Map<String, Object> item = new HashMap<>();
                item.put("label", "Tuần " + week);
                item.put("revenue", revenueByWeek.getOrDefault(week, BigDecimal.ZERO));
                item.put("expense", expenseByWeek.getOrDefault(week, BigDecimal.ZERO));
                timelineData.add(item);
            }
            result.put("timelineTitle", "Thu - Chi theo tuần");

        } else if ("quarter".equals(type)) {
            List<Object[]> revenueByMonth = transactionRepository.sumAmountGroupByMonthAndType(startDate, endDate, "PT");
            List<Object[]> expenseByMonth = transactionRepository.sumAmountGroupByMonthAndType(startDate, endDate, "PC");

            Map<Integer, BigDecimal> revenueMonthMap = new HashMap<>();
            Map<Integer, BigDecimal> expenseMonthMap = new HashMap<>();

            for (Object[] row : revenueByMonth) {
                Integer m = (Integer) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                revenueMonthMap.put(m, amount);
            }

            for (Object[] row : expenseByMonth) {
                Integer m = (Integer) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                expenseMonthMap.put(m, amount);
            }

            int startMonthQ = (quarter - 1) * 3 + 1;
            String[] monthNames = {"", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
            for (int m = startMonthQ; m < startMonthQ + 3; m++) {
                Map<String, Object> item = new HashMap<>();
                item.put("label", monthNames[m]);
                item.put("revenue", revenueMonthMap.getOrDefault(m, BigDecimal.ZERO));
                item.put("expense", expenseMonthMap.getOrDefault(m, BigDecimal.ZERO));
                timelineData.add(item);
            }
            result.put("timelineTitle", "Thu - Chi theo tháng");

        } else {
            List<Object[]> revenueByMonth = transactionRepository.sumAmountGroupByMonthAndType(startDate, endDate, "PT");
            List<Object[]> expenseByMonth = transactionRepository.sumAmountGroupByMonthAndType(startDate, endDate, "PC");

            Map<Integer, BigDecimal> revenueMonthMap = new HashMap<>();
            Map<Integer, BigDecimal> expenseMonthMap = new HashMap<>();

            for (Object[] row : revenueByMonth) {
                Integer m = (Integer) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                revenueMonthMap.put(m, amount);
            }

            for (Object[] row : expenseByMonth) {
                Integer m = (Integer) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                expenseMonthMap.put(m, amount);
            }

            String[] monthNames = {"", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
            for (int m = 1; m <= 12; m++) {
                Map<String, Object> item = new HashMap<>();
                item.put("label", monthNames[m]);
                item.put("revenue", revenueMonthMap.getOrDefault(m, BigDecimal.ZERO));
                item.put("expense", expenseMonthMap.getOrDefault(m, BigDecimal.ZERO));
                timelineData.add(item);
            }
            result.put("timelineTitle", "Thu - Chi theo tháng");
        }

        result.put("timelineData", timelineData);
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());

        return result;
    }

    @GetMapping("/api/dashboard/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String type,
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter) throws IOException {

        byte[] excelBytes = excelReportService.generateCashBookReport(type, year, month, quarter);

        String filename = "So_quy_tien_mat_" + year;
        if (month != null) filename += "_T" + month;
        if (quarter != null) filename += "_Q" + quarter;
        filename += ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }
}