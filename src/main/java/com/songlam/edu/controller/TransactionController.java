package com.songlam.edu.controller;

import com.songlam.edu.dto.TransactionDTO;
import com.songlam.edu.entity.Transaction;
import com.songlam.edu.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/revenues")
    public String viewRevenues(@ModelAttribute TransactionDTO dto, Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {

        Page<Transaction> revenues = transactionService.search(dto, page, size);

        model.addAttribute("revenues", revenues);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", revenues.getTotalPages());
        model.addAttribute("totalItems", revenues.getTotalElements());
        model.addAttribute("dto", dto);
        return "revenues";
    }

    @PostMapping("/revenues")
    public String createRevenue(Model model, Authentication authentication,
                                @RequestParam("studentId") String studentId,
                                @RequestParam("reason") String reason,
                                @RequestParam("amount") String amountStr,
                                @RequestParam(value = "yearId", required = false) Long yearId,
                                @RequestParam(value = "classId", required = false) Long classId,
                                @RequestParam(value = "subjectId", required = false) Long subjectId) {
        try {
            BigDecimal amount = new BigDecimal(amountStr.replaceAll("[.,]", ""));
            String email = authentication.getName();
            transactionService.createRevenue(studentId.trim(), reason.trim(), amount, email, yearId, classId, subjectId);
            return "redirect:/transactions/revenues";
        } catch (IllegalArgumentException e) {
            Page<Transaction> revenues = transactionService.search(new TransactionDTO(), null, null);
            model.addAttribute("revenues", revenues);
            model.addAttribute("error", e.getMessage());
            return "revenues";
        }
    }

    @GetMapping("/revenues/download/{id}")
    public ResponseEntity<byte[]> downloadRevenuePdf(@PathVariable String id) throws IOException {

        byte[] pdfBytes = transactionService.createPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=phieu-thu-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/revenues/view/{id}")
    public ResponseEntity<byte[]> viewRevenuePdf(@PathVariable String id) throws IOException {

        byte[] pdfBytes = transactionService.createPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=phieu-thu-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/revenues/detail/{id}")
    public String viewRevenueDetail(@PathVariable String id, Model model) {
        Transaction transaction = transactionService.findById(id).orElse(null);
        TransactionDTO dto = transactionService.toDTO(transaction);
        model.addAttribute("transaction", dto);
        return "revenue-detail";
    }

    @PostMapping("/revenues/detail")
    public String updateRevenue(@ModelAttribute TransactionDTO dto) {
        transactionService.updateRevenue(dto);
        return "redirect:/transactions/revenues/detail/" + dto.getTransactionId() + "?updated=true";
    }

    @GetMapping("/expenses")
    public String viewExpenses() {
        return "expenses";
    }
}
