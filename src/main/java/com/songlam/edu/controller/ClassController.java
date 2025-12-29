package com.songlam.edu.controller;

import com.songlam.edu.dto.SchoolClassDTO;
import com.songlam.edu.dto.SubjectDTO;
import com.songlam.edu.entity.AcademicYear;
import com.songlam.edu.entity.SchoolClass;
import com.songlam.edu.entity.Student;
import com.songlam.edu.entity.Subject;
import com.songlam.edu.service.ClassService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/classes")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public String viewClasses(Model model) {
        List<AcademicYear> academicYears = classService.getAllAcademicYears();
        AcademicYear currentYear = classService.getCurrentAcademicYear();

        model.addAttribute("academicYears", academicYears);
        model.addAttribute("currentYearId", currentYear != null ? currentYear.getId() : null);

        return "classes";
    }

    @GetMapping("/api/academic-years")
    @ResponseBody
    public List<Map<String, Object>> getAcademicYears() {
        List<AcademicYear> years = classService.getAllAcademicYears();
        return years.stream().map(y -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", y.getId());
            map.put("name", y.getName());
            return map;
        }).toList();
    }

    @GetMapping("/api/classes")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getClassesByYear(@RequestParam Long academicYearId) {
        List<SchoolClass> classes = classService.getClassesByAcademicYear(academicYearId);
        List<Map<String, Object>> result = classes.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("className", c.getClassName());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/subjects")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getSubjectsByClass(@RequestParam Long classId) {
        List<Subject> subjects = classService.getSubjectsByClass(classId);
        List<Map<String, Object>> result = subjects.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("subjectName", s.getSubjectName());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/students")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getStudentsBySubject(@RequestParam Long subjectId) {
        List<Student> students = classService.getStudentsBySubject(subjectId);
        List<Map<String, Object>> result = students.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("citizenId", s.getCitizenId());
            map.put("fullName", s.getPerson() != null ? s.getPerson().getFullName() : "");
            map.put("dateOfBirth", s.getPerson() != null ? s.getPerson().getDateOfBirth() : null);
            map.put("sex", s.getPerson() != null ? s.getPerson().getSex() : null);
            map.put("phone", s.getPerson() != null ? s.getPerson().getPhone() : "");
            map.put("status", s.getStatus());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/students/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchStudentsForSubject(
            @RequestParam(required = false) String fullName,
            @RequestParam Long subjectId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Student> students = classService.searchStudentsForSubject(fullName, subjectId, page, size);

        List<Map<String, Object>> content = students.getContent().stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("citizenId", s.getCitizenId());
            map.put("fullName", s.getPerson() != null ? s.getPerson().getFullName() : "");
            map.put("phone", s.getPerson() != null ? s.getPerson().getPhone() : "");
            return map;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("totalElements", students.getTotalElements());
        result.put("totalPages", students.getTotalPages());
        result.put("currentPage", students.getNumber());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/students/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addStudentsToSubject(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long subjectId = Long.valueOf(request.get("subjectId").toString());
            @SuppressWarnings("unchecked")
            List<String> studentIds = (List<String>) request.get("studentIds");

            if (studentIds == null || studentIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn ít nhất một học sinh!");
                return ResponseEntity.ok(response);
            }

            int addedCount = classService.addStudentsToSubject(subjectId, studentIds);
            response.put("success", true);
            response.put("message", "Đã thêm " + addedCount + " học sinh vào môn học!");
            response.put("addedCount", addedCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/subjects/{subjectId}/students")
    @ResponseBody
    public ResponseEntity<?> removeStudentsFromSubject(
            @PathVariable Long subjectId,
            @RequestBody List<String> studentIds) {
        try {
            int removedCount = classService.removeStudentsFromSubject(subjectId, studentIds);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "removedCount", removedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/add-class")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addClass(@RequestBody SchoolClassDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            classService.createClass(dto);
            response.put("success", true);
            response.put("message", "Thêm lớp học thành công!");
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-subject")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addSubject(@RequestBody SubjectDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            classService.createSubject(dto);
            response.put("success", true);
            response.put("message", "Thêm môn học thành công!");
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}