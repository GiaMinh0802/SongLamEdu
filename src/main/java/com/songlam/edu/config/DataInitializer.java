package com.songlam.edu.config;

import com.songlam.edu.entity.BusinessInfo;
import com.songlam.edu.entity.Person;
import com.songlam.edu.entity.User;
import com.songlam.edu.repository.BusinessInfoRepository;
import com.songlam.edu.repository.PersonRepository;
import com.songlam.edu.repository.UserRepository;
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
                                   PasswordEncoder passwordEncoder,
                                   JdbcTemplate jdbcTemplate) {
        return args -> {

            jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS revenues_id_seq START WITH 1 INCREMENT BY 1");
            jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS expenses_id_seq START WITH 1 INCREMENT BY 1");

            String citizenId = "033169011971";
            if (!personRepository.existsById(citizenId)) {
                Person person = new Person();
                person.setCitizenId(citizenId);
                person.setFullName("An Thị Thanh");
                person.setDateOfBirth(LocalDate.of(1990, 1, 1));
                person.setSex((short) 1); // Female
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

            String adminEmail = "admin@gmail.com";
            if (!userRepository.existsByPersonEmail(adminEmail)) {
                Person adminPerson = new Person();
                adminPerson.setCitizenId("999999999999");
                adminPerson.setFullName("ADMIN 01");
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
        };
    }
}
