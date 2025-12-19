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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                   PersonRepository personRepository,
                                   BusinessInfoRepository businessInfoRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "mrlong07.11@gmail.com";

            if (userRepository.existsByPersonEmail(adminEmail)) {
                return;
            }

            Person adminPerson = new Person();
            adminPerson.setCitizenId("033169011971");
            adminPerson.setFullName("An Thị Thanh");
            adminPerson.setDateOfBirth(LocalDate.of(1990, 1, 1));
            adminPerson.setSex((short) 1); // Female
            adminPerson.setNationality("Việt Nam");
            adminPerson.setAddress("193/5B Nguyễn Thái Bình, Tân Lập, Đắk Lắk");
            adminPerson.setPhone("0835100699");
            adminPerson.setEmail(adminEmail);

            personRepository.save(adminPerson);

            User adminUser = new User();
            adminUser.setPerson(adminPerson);
            adminUser.setPasswordHash(passwordEncoder.encode("Abc12345"));
            adminUser.setRole((short) 1); // 1 = Admin
            adminUser.setIsActive(true);
            
            userRepository.save(adminUser);

            BusinessInfo businessInfo = new BusinessInfo();
            businessInfo.setCompanyCode("kb94guy4");
            businessInfo.setBusinessName("HỘ KINH DOANH CƠ SỞ GIÁO DỤC SONG LÂM EDU");
            businessInfo.setAbbreviatedName("HỘ KINH DOANH CƠ SỞ GIÁO DỤC SONG LÂM EDU");
            businessInfo.setBusinessType("Hộ kinh doanh");
            businessInfo.setOwner(adminPerson);
            businessInfo.setTaxCode("8083456655-001");
            businessInfo.setEstablishedDate(LocalDate.of(2025, 3, 6));
            businessInfo.setBusinessRegistrationCode("40A8062989");
            businessInfo.setIssuedDate(LocalDate.of(2025, 3, 6));
            businessInfo.setIssuedPlace("Phường Tân Lập");
            businessInfo.setRepresentativeTitle("Chủ cơ sở");

            businessInfoRepository.save(businessInfo);
        };
    }
}
