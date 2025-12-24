package com.songlam.edu.service;

import com.songlam.edu.dto.ImportResultDTO;
import com.songlam.edu.dto.StudentDTO;
import com.songlam.edu.entity.Person;
import com.songlam.edu.entity.Student;
import com.songlam.edu.repository.PersonRepository;
import com.songlam.edu.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final PersonRepository personRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final Pattern CITIZENID_PATTERN = Pattern.compile("^\\d{12}(-\\d{2})?$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    public Page<Student> search(String citizenId, String fullName, String phone, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null || page < 0 ? 0 : page, size == null || size <= 0 ? DEFAULT_PAGE_SIZE : size);
        String cid = emptyToNull(citizenId);
        String name = emptyToNull(fullName);
        String ph = emptyToNull(phone);
        return studentRepository.search(cid, name, ph, pageable);
    }

    public Optional<Student> findById(String citizenId) {
        return studentRepository.findById(citizenId);
    }

    public void createStudent(StudentDTO dto) {

        if (studentRepository.existsById(dto.getCitizenId())) {
            throw new IllegalArgumentException("CCCD đã được đăng ký");
        }

        if (personRepository.existsById(dto.getCitizenId())) {
            throw new IllegalArgumentException("CCCD đã được đăng ký");
        }

        Person person = new Person();
        person.setCitizenId(dto.getCitizenId());
        person.setFullName(dto.getFullName());
        person.setDateOfBirth(dto.getDateOfBirth());
        person.setSex(dto.getSex());
        person.setAddress(dto.getAddress());
        person.setPhone(dto.getPhone());
        personRepository.save(person);

        Student student = new Student();
        student.setPerson(person);
        student.setStatus((short) 1);
        student.setStartDate(LocalDate.now());

        studentRepository.save(student);
    }

    public Student updateStudent(String citizenId,
                                 String fullName,
                                 LocalDate dateOfBirth,
                                 short sex,
                                 String address,
                                 String phone,
                                 String email,
                                 Short status) {
        Person person = personRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy học sinh với CCCD " + citizenId));
        person.setFullName(fullName);
        person.setDateOfBirth(dateOfBirth);
        person.setSex(sex);
        person.setAddress(address);
        person.setPhone(phone);
        person.setEmail(email);
        personRepository.save(person);

        Student student = studentRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy học sinh với CCCD " + citizenId));
        if (status != null) {
            student.setStatus(status);
        }
        return studentRepository.save(student);
    }

    public ImportResultDTO importStudents(MultipartFile file) {
        ImportResultDTO result = new ImportResultDTO();
        if (file == null || file.isEmpty()) {
            result.addError("File import rỗng");
            return result;
        }

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.addError("Không tìm thấy sheet đầu tiên trong file");
                return result;
            }

            int totalRows = sheet.getPhysicalNumberOfRows();
            if (totalRows <= 1) {
                result.addError("File không có dữ liệu");
                return result;
            }
            int dataRows = Math.max(0, totalRows - 1);
            if (dataRows > 100) {
                result.addError("Số dòng dữ liệu vượt quá 100 (" + dataRows + ")");
                return result;
            }

            // Header expectation
            for (int i = 1; i < totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String citizenId = getString(row, 0);
                String fullName = getString(row, 1);
                LocalDate dob = getDate(row, 2);
                Short sex = getSex(row, 3);
                String address = getString(row, 4);
                String phone = getString(row, 5);

                int excelRowNum = i + 1;
                List<String> rowErrors = new ArrayList<>();

                if (isBlank(citizenId)) rowErrors.add("CCCD không được để trống");
                if (!isBlank(citizenId) && !CITIZENID_PATTERN.matcher(citizenId).matches()) rowErrors.add("CCCD phải là 12 số hoặc định dạng hợp lệ");
                if (isBlank(fullName)) rowErrors.add("Họ và tên không được để trống");
                if (dob == null) rowErrors.add("Ngày sinh không được để trống");
                if (sex == null) rowErrors.add("Giới tính không được để trống");
                if (isBlank(phone)) rowErrors.add("Số điện thoại không được để trống");
                if (!isBlank(phone) && !PHONE_PATTERN.matcher(phone).matches()) rowErrors.add("Số điện thoại phải gồm 10 chữ số");

                if (!rowErrors.isEmpty()) {
                    result.addError("Dòng " + excelRowNum + ": " + String.join(", ", rowErrors));
                    continue;
                }

                if (studentRepository.existsById(citizenId)) {
                    result.addError("Dòng " + excelRowNum + ": CCCD đã tồn tại trong hệ thống");
                    continue;
                }

                if (personRepository.existsById(citizenId)) {
                    result.addError("Dòng " + excelRowNum + ": CCCD đã tồn tại trong hệ thống");
                    continue;
                }

                try {
                    StudentDTO dto = new StudentDTO();
                    dto.setCitizenId(citizenId);
                    dto.setFullName(fullName);
                    dto.setDateOfBirth(dob);
                    dto.setSex(sex);
                    dto.setAddress(address);
                    dto.setPhone(phone);

                    createStudent(dto);

                    result.incrementSuccess();
                } catch (Exception ex) {
                    result.addError("Dòng " + excelRowNum + ": Lỗi khi lưu dữ liệu");
                }
            }
        } catch (Exception e) {
            result.addError("Lỗi đọc file Excel: " + e.getMessage());
        }

        return result;
    }

    private static String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String value = cell.getStringCellValue();
        return value != null ? value.trim() : null;
    }

    public static LocalDate getDate(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        LocalDateTime value = cell.getLocalDateTimeCellValue();
        return value != null ? value.toLocalDate() : null;
    }

    public  static Short getSex(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String value = cell.getStringCellValue();
        if ("Nam".equals(value)) return Short.valueOf("0");
        if ("Nữ".equals(value)) return Short.valueOf("1");
        return null;
    }

    private String emptyToNull(String value) {
        if (value == null) return null;
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
