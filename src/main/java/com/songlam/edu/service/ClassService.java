package com.songlam.edu.service;

import com.songlam.edu.dto.SchoolClassDTO;
import com.songlam.edu.dto.SubjectDTO;
import com.songlam.edu.entity.*;
import com.songlam.edu.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClassService {

    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final StudentSubjectRepository studentSubjectRepository;

    public ClassService(AcademicYearRepository academicYearRepository,
                        SchoolClassRepository schoolClassRepository,
                        SubjectRepository subjectRepository,
                        StudentRepository studentRepository,
                        StudentSubjectRepository studentSubjectRepository) {
        this.academicYearRepository = academicYearRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.studentSubjectRepository = studentSubjectRepository;
    }

    public List<AcademicYear> getAllAcademicYears() {
        return academicYearRepository.findAllByOrderByStartYearAsc();
    }

    public List<SchoolClass> getClassesByAcademicYear(Long academicYearId) {
        return schoolClassRepository.findByAcademicYearId(academicYearId);
    }

    public List<Subject> getSubjectsByClass(Long classId) {
        return subjectRepository.findBySchoolClassId(classId);
    }

    public List<Student> getStudentsBySubject(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject == null) {
            return List.of();
        }
        return subject.getStudentSubjects().stream()
                .map(StudentSubject::getStudent)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createClass(SchoolClassDTO dto) {
        String className = dto.getClassName().trim();

        List<AcademicYear> targetYears = getCurrentAndFutureAcademicYears();

        if (targetYears.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy năm học phù hợp!");
        }

        for (AcademicYear year : targetYears) {
            if (!schoolClassRepository.existsByClassNameAndAcademicYearId(className, year.getId())) {
                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setClassName(className);
                schoolClass.setAcademicYear(year);
                schoolClassRepository.save(schoolClass);
            }
        }
    }

    @Transactional
    public void createSubject(SubjectDTO dto) {
        String subjectName = dto.getSubjectName().trim();

        List<AcademicYear> targetYears = getCurrentAndFutureAcademicYears();

        if (targetYears.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy năm học phù hợp!");
        }

        for (AcademicYear year : targetYears) {
            List<SchoolClass> classes = schoolClassRepository.findByAcademicYearId(year.getId());
            for (SchoolClass schoolClass : classes) {
                if (!subjectRepository.existsBySubjectNameAndSchoolClassId(subjectName, schoolClass.getId())) {
                    Subject subject = new Subject();
                    subject.setSubjectName(subjectName);
                    subject.setSchoolClass(schoolClass);
                    subjectRepository.save(subject);
                }
            }
        }
    }

    public Page<Student> searchStudentsForSubject(String fullName, Long subjectId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(
                page == null || page < 0 ? 0 : page,
                size == null || size <= 0 ? 10 : size
        );

        String searchName = (fullName != null && !fullName.trim().isEmpty()) ? fullName.trim() : null;

        return studentRepository.searchActiveStudentsNotInSubject(searchName, subjectId, pageable);
    }

    public Page<Map<String, Object>> searchStudentsInSubject(
            Long subjectId, String citizenId, String fullName, String phone, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Student> studentPage = studentRepository.findBySubjectIdAndFilters(
                subjectId,
                citizenId != null ? citizenId.trim() : null,
                fullName != null ? fullName.trim() : null,
                phone != null ? phone.trim() : null,
                pageable);

        List<Map<String, Object>> students = studentPage.getContent().stream()
                .map(student -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("citizenId", student.getCitizenId());
                    map.put("fullName", student.getPerson().getFullName());
                    map.put("dateOfBirth", student.getPerson().getDateOfBirth());
                    map.put("sex", student.getPerson().getSex());
                    map.put("phone", student.getPerson().getPhone());
                    map.put("status", student.getStatus());
                    return map;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(students, pageable, studentPage.getTotalElements());
    }

    @Transactional
    public int addStudentsToSubject(Long subjectId, List<String> studentIds) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học!"));

        int addedCount = 0;
        for (String citizenId : studentIds) {
            if (!studentSubjectRepository.existsByStudentCitizenIdAndSubjectId(citizenId, subjectId)) {
                Student student = studentRepository.findById(citizenId).orElse(null);
                if (student != null && student.getStatus() == 1) { // Chỉ thêm học sinh đang học
                    StudentSubject studentSubject = new StudentSubject();
                    studentSubject.setStudent(student);
                    studentSubject.setSubject(subject);
                    studentSubjectRepository.save(studentSubject);
                    addedCount++;
                }
            }
        }
        return addedCount;
    }

    @Transactional
    public int removeStudentsFromSubject(Long subjectId, List<String> studentIds) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new IllegalArgumentException("Không tìm thấy môn học!");
        }

        int removedCount = 0;
        for (String citizenId : studentIds) {
            StudentSubject studentSubject = studentSubjectRepository
                    .findByStudentCitizenIdAndSubjectId(citizenId, subjectId)
                    .orElse(null);

            if (studentSubject != null) {
                studentSubjectRepository.delete(studentSubject);
                removedCount++;
            }
        }
        return removedCount;
    }

    public AcademicYear getCurrentAcademicYear() {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        int startYear;
        if (today.getMonth().getValue() >= Month.SEPTEMBER.getValue()) {
            startYear = currentYear;
        } else {
            startYear = currentYear - 1;
        }

        return academicYearRepository.findByStartYear(startYear).orElse(null);
    }

    public List<AcademicYear> getCurrentAndFutureAcademicYears() {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        int startYear;
        if (today.getMonth().getValue() >= Month.SEPTEMBER.getValue()) {
            startYear = currentYear;
        } else {
            startYear = currentYear - 1;
        }

        return academicYearRepository.findByStartYearGreaterThanEqual(startYear);
    }
}
