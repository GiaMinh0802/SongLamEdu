package com.songlam.edu.service;

import com.songlam.edu.dto.MeDTO;
import com.songlam.edu.dto.RegisterDTO;
import com.songlam.edu.entity.Person;
import com.songlam.edu.entity.User;
import com.songlam.edu.repository.PersonRepository;
import com.songlam.edu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int DEFAULT_PAGE_SIZE = 20;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByPersonEmail(email);
    }

    public Page<User> search(String citizenId, String fullName, String phone, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null || page < 0 ? 0 : page, size == null || size <= 0 ? DEFAULT_PAGE_SIZE : size);
        return  userRepository.search(emptyToNull(citizenId),
                                    emptyToNull(fullName),
                                    emptyToNull(phone),
                                    pageable);
    }

    public boolean checkMatchPassword(String oldPassword, String encodedPassword) {
        return passwordEncoder.matches(oldPassword, encodedPassword);
    }

    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void activeUser(String citizenId) {
        User user = userRepository.findById(citizenId).orElse(null);
        if (user != null) {
            user.setIsActive(true);
            userRepository.save(user);
        }
    }

    public void registerCashier(RegisterDTO registerDTO) {

        if (userRepository.existsByPersonEmail(registerDTO.getEmail())) {
            throw new IllegalArgumentException("Email đã được đăng ký");
        }

        if (personRepository.existsById(registerDTO.getCitizenId())) {
            throw new IllegalArgumentException("CCCD đã được đăng ký");
        }

        Person person = new Person();
        person.setCitizenId(registerDTO.getCitizenId());
        person.setFullName(registerDTO.getFullName());
        person.setDateOfBirth(registerDTO.getDateOfBirth());
        person.setSex(registerDTO.getSex());
        person.setPhone(registerDTO.getPhone());
        person.setEmail(registerDTO.getEmail());
        personRepository.save(person);

        User user = new User();
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
        user.setPerson(person);
        userRepository.save(user);
    }

    public MeDTO toDTOForMe(User user) {
        MeDTO dto = new MeDTO();
        if (user == null) return dto;
        Person person = user.getPerson();
        if (person != null) {
            dto.setCitizenId(person.getCitizenId());
            dto.setFullName(person.getFullName());
            dto.setDateOfBirth(person.getDateOfBirth());
            dto.setSex(person.getSex());
            dto.setNationality(person.getNationality());
            dto.setPlaceOfOrigin(person.getPlaceOfOrigin());
            dto.setPlaceOfResidence(person.getPlaceOfResidence());
            dto.setAddress(person.getAddress());
            dto.setPhone(person.getPhone());
            dto.setEmail(person.getEmail());
        }
        return dto;
    }

    private String emptyToNull(String value) {
        if (value == null) return null;
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

}
