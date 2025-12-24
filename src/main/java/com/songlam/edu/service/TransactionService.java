package com.songlam.edu.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.songlam.edu.dto.TransactionDTO;
import com.songlam.edu.entity.BusinessInfo;
import com.songlam.edu.entity.Student;
import com.songlam.edu.entity.Transaction;
import com.songlam.edu.entity.User;
import com.songlam.edu.repository.StudentRepository;
import com.songlam.edu.repository.TransactionRepository;
import com.songlam.edu.repository.UserRepository;
import com.songlam.edu.util.CurrencyUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CompanyInfoService companyInfoService;
    private final TemplateEngine templateEngine;

    @PersistenceContext
    private EntityManager entityManager;

    public TransactionService(TransactionRepository transactionRepository,
                              StudentRepository studentRepository,
                              UserRepository userRepository,
                              CompanyInfoService companyInfoService,
                              TemplateEngine templateEngine) {
        this.transactionRepository = transactionRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.companyInfoService = companyInfoService;
        this.templateEngine = templateEngine;
    }

    public Page<Transaction> search(TransactionDTO dto, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null || page < 0 ? 0 : page, size == null || size <= 0 ? 20 : size);
        return transactionRepository.search(
                emptyToNull(dto.getTransactionId()),
                emptyToNull(dto.getStudentName()),
                emptyToNull(dto.getCashierName()),
                dto.getDateFrom(),
                dto.getDateTo(),
                pageable);
    }

    public Optional<Transaction> findById(String transactionNumber) {
        return transactionRepository.findById(transactionNumber);
    }

    @Transactional
    public void createRevenue(String studentCitizenId, String reason, BigDecimal amount, String cashierEmail) {

        Student student = studentRepository.findById(studentCitizenId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy học sinh"));

        User cashier = userRepository.findByPersonEmail(cashierEmail)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thủ quỹ"));

        String code = generateNextCode();
        Transaction tx = new Transaction();
        tx.setTransactionNumber(code);
        tx.setDateOfRecorded(LocalDate.now());
        tx.setDateOfDocument(LocalDate.now());
        tx.setStudent(student);
        tx.setReason(reason);
        tx.setAmount(amount);
        tx.setCashier(cashier);
        transactionRepository.save(tx);
    }

    @Transactional
    public Transaction updateRevenue(String transactionNumber, String reason, BigDecimal amount, LocalDate date) {
        Transaction tx = transactionRepository.findById(transactionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu thu " + transactionNumber));
        tx.setReason(reason);
        tx.setAmount(amount);
        tx.setDateOfRecorded(date);
        tx.setDateOfDocument(date);
        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction cancelRevenue(String transactionNumber, String note) {
        if (note == null || note.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do hủy (note) không được để trống");
        }
        Transaction tx = transactionRepository.findById(transactionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu thu " + transactionNumber));
        tx.setNote(note);
        return transactionRepository.save(tx);
    }

    public byte[] createPdf(String transactionNumber) throws IOException {

        Transaction transaction = findById(transactionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu thu " + transactionNumber));
        BusinessInfo businessInfo = companyInfoService.getBusinessInfo().orElseThrow();

        Context context = new Context();
        context.setVariable("businessName", businessInfo.getBusinessName());
        context.setVariable("businessAddress", businessInfo.getPerson().getAddress());
        context.setVariable("receiptDay", transaction.getDateOfRecorded().getDayOfMonth());
        context.setVariable("receiptMonth", transaction.getDateOfRecorded().getMonthValue());
        context.setVariable("receiptYear", transaction.getDateOfRecorded().getYear());
        context.setVariable("transactionNumber", transactionNumber);
        context.setVariable("payerName", transaction.getStudent().getPerson().getFullName());
        context.setVariable("payerAddress", transaction.getStudent().getPerson().getAddress());
        context.setVariable("reason", transaction.getReason());
        context.setVariable("amount", CurrencyUtil.formatBigDecimal(transaction.getAmount()));
        context.setVariable("amountInWords", CurrencyUtil.convertText(transaction.getAmount()));
        context.setVariable("signatureDay", transaction.getDateOfRecorded().getDayOfMonth());
        context.setVariable("signatureMonth", transaction.getDateOfRecorded().getMonthValue());
        context.setVariable("signatureYear", transaction.getDateOfRecorded().getYear());

        String html = templateEngine.process("sample/receipt_template", context);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFont(
                () -> getClass().getResourceAsStream("/fonts/times.ttf"),
                "Times New Roman"
        );
        builder.withHtmlContent(html, null);
        builder.toStream(outputStream);
        builder.run();

        return outputStream.toByteArray();
    }

    private String generateNextCode() {
        // Use DB sequence revenues_id_seq to get next number
        Object val = entityManager.createNativeQuery("SELECT nextval('revenues_id_seq')")
                .getSingleResult();
        long seq;
        if (val instanceof Number n) seq = n.longValue();
        else seq = Long.parseLong(val.toString());
        return "PT" + String.format("%06d", seq);
    }

    private static String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
