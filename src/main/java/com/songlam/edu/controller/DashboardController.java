package com.songlam.edu.controller;

import com.songlam.edu.service.ExcelReportService;
import com.songlam.edu.service.TransactionService;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final TransactionService transactionService;
    private final ExcelReportService excelReportService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate now = LocalDate.now();
        model.addAttribute("currentMonth", now.getMonthValue());
        model.addAttribute("currentQuarter", (now.getMonthValue() - 1) / 3 + 1);
        model.addAttribute("currentYear", now.getYear());

        List<Integer> years = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            years.add(2026 + i);
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

        return transactionService.buildStats(startDate, endDate, type, quarter);
    }


    @GetMapping("/api/dashboard/export-fund")
    public ResponseEntity<byte[]> exportFund(
            @RequestParam String type,
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter) throws IOException {

        byte[] excelBytes = excelReportService.generateCashBookReport(type, year, month, quarter, true);

        String filename = "So_quy_tien_mat_" + year;
        if (month != null) filename += "_T" + month;
        if (quarter != null) filename += "_Q" + quarter;
        filename += ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    @GetMapping("/api/dashboard/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String type,
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter) throws IOException {

        byte[] excelBytes = excelReportService.generateCashBookReport(type, year, month, quarter, false);

        String filename = "Report_" + year;
        if (month != null) filename += "_T" + month;
        if (quarter != null) filename += "_Q" + quarter;
        filename += ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }
}