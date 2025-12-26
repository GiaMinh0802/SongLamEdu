package com.songlam.edu.controller;

import com.songlam.edu.dto.ImportResultDTO;
import com.songlam.edu.dto.StudentDTO;
import com.songlam.edu.entity.Student;
import com.songlam.edu.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String viewStudents(Model model,
                               @RequestParam(value = "citizenId", required = false) String citizenId,
                               @RequestParam(value = "fullName", required = false) String fullName,
                               @RequestParam(value = "phone", required = false) String phone,
                               @RequestParam(value = "page", required = false) Integer page,
                               @RequestParam(value = "size", required = false) Integer size) {

        Page<Student> students = studentService.search(citizenId, fullName, phone, page, size);

        model.addAttribute("students", students);
        model.addAttribute("citizenId", citizenId);
        model.addAttribute("fullName", fullName);
        model.addAttribute("phone", phone);
        return "students";
    }

    @PostMapping
    public String createStudent(@ModelAttribute StudentDTO dto, Model model) {
        try {
            studentService.createStudent(dto);
            return "redirect:/students";
        } catch (IllegalArgumentException e) {
            Page<Student> students = studentService.search(null, null, null, null, null);
            model.addAttribute("students", students);
            model.addAttribute("error", e.getMessage());
            return "students";
        }
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/sample/students_import.xlsx");
        if (!resource.exists()) {
            throw new FileNotFoundException();
        }

        byte[] fileBytes = StreamUtils.copyToByteArray(resource.getInputStream());

        String filename = URLEncoder.encode("students_import_sample.xlsx", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileBytes);
    }

    @PostMapping("/import")
    public String importStudents(@RequestParam("file") MultipartFile file, Model model, HttpSession session) {
        ImportResultDTO res = studentService.importStudents(file);

        if (res.hasErrors()) {
            session.setAttribute("importErrors", res.getErrorsAsText());
            model.addAttribute("hasImportErrors", true);
        }

        String importMessage = String.format(
                "Kết quả import: Thành công %d học sinh, Thất bại %d học sinh.",
                res.getSuccess(),
                res.getErrors().size()
        );
        model.addAttribute("importMessage", importMessage);

        Page<Student> students = studentService.search(null, null, null, null, null);
        model.addAttribute("students", students);

        return "students";
    }

    @GetMapping("/import/errors")
    public ResponseEntity<byte[]> downloadImportErrors(HttpSession session) throws FileNotFoundException {
        String errors = (String) session.getAttribute("importErrors");

        if (errors == null || errors.isEmpty()) {
            throw new FileNotFoundException();
        }

        byte[] data = errors.getBytes(StandardCharsets.UTF_8);

        String filename = "import_errors_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".txt";

        session.removeAttribute("importErrors");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(data.length)
                .body(data);
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable String id, Model model) {
        Student student = studentService.findById(id).orElse(null);
        StudentDTO dto = studentService.toDTO(student);
        model.addAttribute("student", dto);
        return "student-detail";
    }

    @PostMapping("/detail")
    public String updateStudent(@ModelAttribute StudentDTO dto) {
        studentService.updateStudent(dto);
        return "redirect:/students/detail/" + dto.getCitizenId() + "?updated=true";
    }
}
