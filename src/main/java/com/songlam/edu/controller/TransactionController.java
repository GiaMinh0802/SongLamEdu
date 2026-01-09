package com.songlam.edu.controller;

import com.songlam.edu.dto.TransactionDTO;
import com.songlam.edu.entity.Branches;
import com.songlam.edu.entity.Transaction;
import com.songlam.edu.service.TransactionService;
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
import java.util.List;
import java.util.Map;

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

        Page<Transaction> revenues = transactionService.searchForRevenues(dto, page, size);

        model.addAttribute("revenues", revenues);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", revenues.getTotalPages());
        model.addAttribute("totalItems", revenues.getTotalElements());
        model.addAttribute("dto", dto);
        return "revenues";
    }

    @GetMapping("/expenses")
    public String viewExpenses(@ModelAttribute TransactionDTO dto, Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {

        Page<Transaction> expenses = transactionService.searchForExpenses(dto, page, size);

        model.addAttribute("expenses", expenses);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", expenses.getTotalPages());
        model.addAttribute("totalItems", expenses.getTotalElements());
        model.addAttribute("dto", dto);
        return "expenses";
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
            Page<Transaction> revenues = transactionService.searchForRevenues(new TransactionDTO(), null, null);
            model.addAttribute("revenues", revenues);
            model.addAttribute("error", e.getMessage());
            return "revenues";
        }
    }

    @PostMapping("/expenses")
    public String createExpenses(Model model, Authentication authentication,
                                 @RequestParam("branchId") Long branchId,
                                 @RequestParam("receiverName") String receiverName,
                                 @RequestParam("receiverAddress") String receiverAddress,
                                 @RequestParam("reason") String reason,
                                 @RequestParam("amount") String amountStr) {
        try {
            BigDecimal amount = new BigDecimal(amountStr.replaceAll("[.,]", ""));
            String email = authentication.getName();
            transactionService.createExpense(branchId, receiverName.trim(), receiverAddress.trim(), reason.trim(), amount, email);
            return "redirect:/transactions/expenses";
        } catch (IllegalArgumentException e) {
            Page<Transaction> expenses = transactionService.searchForExpenses(new TransactionDTO(), null, null);
            model.addAttribute("expenses", expenses);
            model.addAttribute("error", e.getMessage());
            return "expenses";
        }
    }

    @GetMapping("/revenues/download/{id}")
    public ResponseEntity<byte[]> downloadRevenuePdf(@PathVariable String id) throws IOException {

        byte[] pdfBytes = transactionService.createPdf(id, true);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=phieu-thu-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/expenses/download/{id}")
    public ResponseEntity<byte[]> downloadExpensePdf(@PathVariable String id) throws IOException {

        byte[] pdfBytes = transactionService.createPdf(id, false);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=phieu-chi-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/revenues/view/{id}")
    public ResponseEntity<byte[]> viewRevenuePdf(@PathVariable String id) throws IOException {

        byte[] pdfBytes = transactionService.createPdf(id, true);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=phieu-thu-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/expenses/view/{id}")
    public ResponseEntity<byte[]> viewExpensePdf(@PathVariable String id) throws IOException {

        byte[] pdfBytes = transactionService.createPdf(id, false);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=phieu-chi-" + id + ".pdf")
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

    @GetMapping("/expenses/detail/{id}")
    public String viewExpenseDetail(@PathVariable String id, Model model) {
        Transaction transaction = transactionService.findById(id).orElse(null);
        TransactionDTO dto = transactionService.toDTO(transaction);
        model.addAttribute("transaction", dto);
        return "expense-detail";
    }

    @PostMapping("/revenues/detail")
    public String updateRevenue(@ModelAttribute TransactionDTO dto) {
        transactionService.updateTransaction(dto);
        return "redirect:/transactions/revenues/detail/" + dto.getTransactionId() + "?updated=true";
    }

    @PostMapping("/expenses/detail")
    public String updateExpenses(@ModelAttribute TransactionDTO dto) {
        transactionService.updateTransaction(dto);
        return "redirect:/transactions/expenses/detail/" + dto.getTransactionId() + "?updated=true";
    }

    @GetMapping("/api/branches")
    @ResponseBody
    public List<Map<String, Object>> getBranches() {
        List<Branches> branches = transactionService.getAllBranches();
        return branches.stream().map(b -> {
            Map<String, Object> map = Map.of(
                    "id", b.getId(),
                    "name", b.getName()
            );
            return map;
        }).toList();
    }
}
