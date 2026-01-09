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
import java.time.temporal.WeekFields;
import java.util.*;

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
    private final BranchRepository branchRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public TransactionService(TransactionRepository transactionRepository,
                              StudentRepository studentRepository,
                              UserRepository userRepository,
                              CompanyInfoService companyInfoService,
                              TemplateEngine templateEngine,
                              AcademicYearRepository academicYearRepository,
                              SchoolClassRepository schoolClassRepository,
                              SubjectRepository subjectRepository,
                              BranchRepository branchRepository) {
        this.transactionRepository = transactionRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.companyInfoService = companyInfoService;
        this.templateEngine = templateEngine;
        this.academicYearRepository = academicYearRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
        this.branchRepository = branchRepository;
    }

    public Page<Transaction> searchForRevenues(TransactionDTO dto, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null || page < 0 ? 0 : page, size == null || size <= 0 ? 10 : size);
        return transactionRepository.searchForRevenues(
                emptyToNull(dto.getTransactionId()),
                emptyToNull(dto.getStudentName()),
                emptyToNull(dto.getCashierName()),
                dto.getDateFrom(),
                dto.getDateTo(),
                pageable);
    }

    public Page<Transaction> searchForExpenses(TransactionDTO dto, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null || page < 0 ? 0 : page, size == null || size <= 0 ? 10 : size);
        return transactionRepository.searchForExpenses(
                dto.getBranchId(),
                emptyToNull(dto.getTransactionId()),
                emptyToNull(dto.getReceiverName()),
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

        String code = generateNextCodeForRevenues();
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

    @Transactional
    public void createExpense(Long branchId, String receiverName, String receiverAddress, String reason, BigDecimal amount, String cashierEmail) {

        User cashier = userRepository.findByPersonEmail(cashierEmail)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thủ quỹ"));

        Branches branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cở sở"));

        String code = generateNextCodeForExpenses();
        Transaction tx = new Transaction();
        tx.setTransactionNumber(code);
        tx.setBranch(branch);
        tx.setDateOfRecorded(LocalDate.now());
        tx.setDateOfDocument(LocalDate.now());
        tx.setReason(reason);
        tx.setAmount(amount);
        tx.setReceiverName(receiverName);
        tx.setReceiverAdress(receiverAddress);
        tx.setCashier(cashier);
        tx.setType("PC");
        transactionRepository.save(tx);
    }

    public void updateTransaction(TransactionDTO dto) {
        Transaction tx = transactionRepository.findById(dto.getTransactionId()).orElseGet(Transaction::new);
        if (!dto.getReceiverAddress().isBlank()) {
            tx.setReceiverAdress(dto.getReceiverAddress());
        }
        if (!dto.getReason().isBlank()) {
            tx.setReason(dto.getReason());
        }
        if (!dto.getAmountHidden().isBlank()) {
            tx.setAmount(new BigDecimal(dto.getAmountHidden()));
        }
        if (!dto.getAttachments().isBlank()) {
            tx.setAttachments(dto.getAttachments());
        }
        if (!dto.getSourceDocuments().isBlank()) {
            tx.setSourceDocuments(dto.getSourceDocuments());
        }
        if (!dto.getNote().isBlank()) {
            tx.setNote(dto.getNote());
        }
        transactionRepository.save(tx);
    }

    public byte[] createPdf(String transactionNumber, boolean isRevenue) throws IOException {

        Transaction transaction = findById(transactionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn" + transactionNumber));
        BusinessInfo businessInfo = companyInfoService.getBusinessInfo().orElseThrow();

        Context context = new Context();
        context.setVariable("businessName", businessInfo.getBusinessName());
        context.setVariable("businessAddress", businessInfo.getPerson().getAddress());
        context.setVariable("receiptDay", transaction.getDateOfRecorded().getDayOfMonth());
        context.setVariable("receiptMonth", transaction.getDateOfRecorded().getMonthValue());
        context.setVariable("receiptYear", transaction.getDateOfRecorded().getYear());
        context.setVariable("transactionNumber", transactionNumber);

        if (isRevenue) {
            context.setVariable("payerName", transaction.getStudent().getPerson().getFullName());
            context.setVariable("payerAddress", transaction.getStudent().getPerson().getAddress());
        } else {
            context.setVariable("receiverName", transaction.getReceiverName());
            context.setVariable("receiverAddress", transaction.getReceiverAdress());
        }

        context.setVariable("reason", transaction.getReason());
        context.setVariable("amount", CurrencyUtil.formatBigDecimal(transaction.getAmount()));
        context.setVariable("amountInWords", CurrencyUtil.convertText(transaction.getAmount()));
        context.setVariable("signatureDay", transaction.getDateOfRecorded().getDayOfMonth());
        context.setVariable("signatureMonth", transaction.getDateOfRecorded().getMonthValue());
        context.setVariable("signatureYear", transaction.getDateOfRecorded().getYear());
        context.setVariable("representativeName", businessInfo.getPerson().getFullName());
        context.setVariable("cashierName", transaction.getCashier().getPerson().getFullName());

        String html;
        if (isRevenue) {
            html = templateEngine.process("sample/receipt_template_revenue", context);
        } else {
            html = templateEngine.process("sample/receipt_template_expense", context);
        }

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

    public Map<String, Object> buildStats(LocalDate startDate, LocalDate endDate,
                                           String type, Integer quarter) {
        Map<String, Object> result = new HashMap<>();

        BigDecimal totalRevenue = transactionRepository.sumAmountByDateRangeAndType(startDate, endDate, "PT");
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        BigDecimal totalExpense = transactionRepository.sumAmountByDateRangeAndType(startDate, endDate, "PC");
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        Long revenueCount = transactionRepository.countByDateRangeAndType(startDate, endDate, "PT");
        if (revenueCount == null) revenueCount = 0L;

        Long expenseCount = transactionRepository.countByDateRangeAndType(startDate, endDate, "PC");
        if (expenseCount == null) expenseCount = 0L;

        BigDecimal balance = totalRevenue.subtract(totalExpense);

        result.put("totalRevenue", totalRevenue);
        result.put("totalExpense", totalExpense);
        result.put("balance", balance);
        result.put("revenueCount", revenueCount);
        result.put("expenseCount", expenseCount);

        List<Object[]> bySubject = transactionRepository.sumAmountGroupBySubjectAndType(startDate, endDate, "PT");
        List<Map<String, Object>> subjectStats = new ArrayList<>();
        for (Object[] row : bySubject) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", row[0] != null ? row[0] : "Không xác định");
            item.put("amount", row[1] != null ? row[1] : BigDecimal.ZERO);
            subjectStats.add(item);
        }
        result.put("bySubject", subjectStats);

        List<Object[]> byClass = transactionRepository.sumAmountGroupByClassAndType(startDate, endDate, "PT");
        List<Map<String, Object>> classStats = new ArrayList<>();
        for (Object[] row : byClass) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", row[0] != null ? row[0] : "Không xác định");
            item.put("amount", row[1] != null ? row[1] : BigDecimal.ZERO);
            classStats.add(item);
        }
        result.put("byClass", classStats);

        List<Map<String, Object>> timelineData = new ArrayList<>();

        if ("month".equals(type)) {
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

    private String generateNextCodeForRevenues() {
        // Use DB sequence revenues_id_seq to get next number
        Object val = entityManager.createNativeQuery("SELECT nextval('revenues_id_seq')")
                .getSingleResult();
        long seq;
        if (val instanceof Number n) seq = n.longValue();
        else seq = Long.parseLong(val.toString());
        return "PT" + String.format("%06d", seq);
    }

    private String generateNextCodeForExpenses() {
        // Use DB sequence expenses_id_seq to get next number
        Object val = entityManager.createNativeQuery("SELECT nextval('expenses_id_seq')")
                .getSingleResult();
        long seq;
        if (val instanceof Number n) seq = n.longValue();
        else seq = Long.parseLong(val.toString());
        return "PC" + String.format("%06d", seq);
    }

    public TransactionDTO toDTO(Transaction tx) {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(tx.getTransactionNumber());
        dto.setBranchName(tx.getBranch().getName());
        dto.setType(tx.getType());
        if (tx.getStudent() != null) {
            dto.setStudentName(tx.getStudent().getPerson().getFullName());
        }
        dto.setReceiverName(tx.getReceiverName());
        dto.setReceiverAddress(tx.getReceiverAdress());
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

    public List<Branches> getAllBranches() {
        return branchRepository.findAll();
    }
}
