package com.songlam.edu.config;

import com.songlam.edu.entity.*;
import com.songlam.edu.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   PersonRepository personRepository,
                                   BusinessInfoRepository businessInfoRepository,
                                   AcademicYearRepository academicYearRepository,
                                   PasswordEncoder passwordEncoder,
                                   JdbcTemplate jdbcTemplate, BranchRepository branchRepository) {
        return args -> {

            jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS revenues_id_seq START WITH 1 INCREMENT BY 1");
            jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS expenses_id_seq START WITH 1 INCREMENT BY 1");

            String citizenId = "033169011971";
            if (!personRepository.existsById(citizenId)) {
                Person person = new Person();
                person.setCitizenId(citizenId);
                person.setFullName("An Thị Thanh");
                person.setDateOfBirth(LocalDate.of(1990, 1, 1));
                person.setSex((short) 1);
                person.setAddress("193/5B Nguyễn Thái Bình, Phường Tân Lập, Tỉnh Đắk Lắk");
                person.setPhone("0835100699");
                person.setEmail("mrlong07.11@gmail.com");
                personRepository.save(person);

                BusinessInfo businessInfo = new BusinessInfo();
                businessInfo.setCompanyCode("kb94guy4");
                businessInfo.setBusinessName("HỘ KINH DOANH CƠ SỞ GIÁO DỤC SONG LÂM EDU");
                businessInfo.setAbbreviatedName("HỘ KINH DOANH CƠ SỞ GIÁO DỤC SONG LÂM EDU");
                businessInfo.setBusinessType("Hộ kinh doanh");
                businessInfo.setTaxCode("8083456655-001");
                businessInfo.setEstablishedDate(LocalDate.of(2025, 3, 6));
                businessInfo.setBusinessRegistrationCode("40A8062989");
                businessInfo.setIssuedDate(LocalDate.of(2025, 3, 6));
                businessInfo.setIssuedPlace("Phường Tân Lập");
                businessInfo.setRepresentativeTitle("Chủ cơ sở");
                businessInfo.setPerson(person);
                businessInfoRepository.save(businessInfo);
            }

            String adminEmail = "songlamtech.dlk@gmail.com";
            if (!userRepository.existsByPersonEmail(adminEmail)) {
                Person adminPerson = new Person();
                adminPerson.setCitizenId("999999999999");
                adminPerson.setFullName("Dương Thành Long");
                adminPerson.setDateOfBirth(LocalDate.of(1990, 1, 1));
                adminPerson.setSex((short) 0);
                adminPerson.setPhone("0835100699");
                adminPerson.setEmail(adminEmail);
                personRepository.save(adminPerson);

                User adminUser = new User();
                adminUser.setPasswordHash(passwordEncoder.encode("Abc12345"));
                adminUser.setRole((short) 1);
                adminUser.setIsActive(true);
                adminUser.setPerson(adminPerson);
                userRepository.save(adminUser);
            }

            int startYear = 2025;
            int numberOfYears = 10;
            for (int i = 0; i < numberOfYears; i++) {
                int fromYear = startYear + i;
                int toYear = fromYear + 1;
                String name = fromYear + "-" + toYear;

                if (!academicYearRepository.existsByName(name)) {
                    AcademicYear year = new AcademicYear();
                    year.setName(name);
                    year.setStartYear(fromYear);
                    year.setEndYear(toYear);

                    academicYearRepository.save(year);
                }
            }

            for (int i = 1; i <= 3; i++) {
                String branchName = "Cơ sở " + i;
                if (!branchRepository.existsByName(branchName)) {
                    Branches branch = new Branches();
                    branch.setName(branchName);
                    branchRepository.save(branch);
                }
            }
        };
    }
}
