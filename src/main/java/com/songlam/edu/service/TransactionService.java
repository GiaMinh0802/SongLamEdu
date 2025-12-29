package com.songlam.edu.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.songlam.edu.dto.TransactionDTO;
import com.songlam.edu.entity.*;
import com.songlam.edu.repository.*;
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
    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public TransactionService(TransactionRepository transactionRepository,
                              StudentRepository studentRepository,
                              UserRepository userRepository,
                              CompanyInfoService companyInfoService,
                              TemplateEngine templateEngine, AcademicYearRepository academicYearRepository, SchoolClassRepository schoolClassRepository, SubjectRepository subjectRepository) {
        this.transactionRepository = transactionRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.companyInfoService = companyInfoService;
        this.templateEngine = templateEngine;
        this.academicYearRepository = academicYearRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
    }

    public Page<Transaction> search(TransactionDTO dto, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null || page < 0 ? 0 : page, size == null || size <= 0 ? 10 : size);
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
    public void createRevenue(String studentCitizenId, String reason, BigDecimal amount, String cashierEmail,
                              Long yearId, Long classId, Long subjectId) {

        Student student = studentRepository.findById(studentCitizenId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy học sinh"));

        User cashier = userRepository.findByPersonEmail(cashierEmail)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thủ quỹ"));

        AcademicYear academicYear = academicYearRepository.findById(yearId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy năm học"));

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học"));

        String code = generateNextCode();
        Transaction tx = new Transaction();
        tx.setTransactionNumber(code);
        tx.setDateOfRecorded(LocalDate.now());
        tx.setDateOfDocument(LocalDate.now());
        tx.setStudent(student);
        tx.setReason(reason);
        tx.setAmount(amount);
        tx.setAcademicYear(academicYear);
        tx.setSchoolClass(schoolClass);
        tx.setSubject(subject);
        tx.setCashier(cashier);
        tx.setType("PT");
        transactionRepository.save(tx);
    }

    public void updateRevenue(TransactionDTO dto) {
        Transaction tx = transactionRepository.findById(dto.getTransactionId()).orElseGet(Transaction::new);
        tx.setReason(dto.getReason());
        tx.setAmount(new BigDecimal(dto.getAmountHidden()));
        tx.setAttachments(dto.getAttachments());
        tx.setSourceDocuments(dto.getSourceDocuments());
        tx.setNote(dto.getNote());
        transactionRepository.save(tx);
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
        context.setVariable("representativeName", businessInfo.getPerson().getFullName());
        context.setVariable("cashierName", transaction.getCashier().getPerson().getFullName());

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

    public TransactionDTO toDTO(Transaction tx) {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(tx.getTransactionNumber());
        dto.setType(tx.getType());
        dto.setStudentName(tx.getStudent().getPerson().getFullName());
        dto.setCashierName(tx.getCashier().getPerson().getFullName());
        dto.setDateOfRecorded(tx.getDateOfRecorded());
        dto.setDateOfDocument(tx.getDateOfDocument());
        dto.setReason(tx.getReason());
        dto.setAmount(tx.getAmount().stripTrailingZeros().toPlainString());
        dto.setAttachments(tx.getAttachments());
        dto.setSourceDocuments(tx.getSourceDocuments());
        dto.setNote(tx.getNote());
        if (tx.getAcademicYear() != null) {
            dto.setAcademicYearName(tx.getAcademicYear().getName());
        }
        if (tx.getSchoolClass() != null) {
            dto.setClassName(tx.getSchoolClass().getClassName());
        }
        if (tx.getSubject() != null) {
            dto.setSubjectName(tx.getSubject().getSubjectName());
        }
        return dto;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
