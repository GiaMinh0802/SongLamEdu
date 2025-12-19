package com.songlam.edu.service;

import com.songlam.edu.dto.RegisterDTO;
import com.songlam.edu.entity.Person;
import com.songlam.edu.entity.User;
import com.songlam.edu.repository.PersonRepository;
import com.songlam.edu.repository.UserRepository;
import com.songlam.edu.util.ValidationUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByPersonEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByPersonEmail(email);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User createUser(User user, String rawPassword) {
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public User registerCashier(RegisterDTO registerDTO) {
        // Validate password
        if (!ValidationUtil.isValidPassword(registerDTO.getPassword())) {
            throw new IllegalArgumentException(ValidationUtil.getPasswordValidationMessage());
        }

        // Check if password matches
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        // Check if email already exists
        if (userRepository.existsByPersonEmail(registerDTO.getEmail())) {
            throw new IllegalArgumentException("Email đã được đăng ký");
        }

        // Check if citizen ID already exists
        if (personRepository.existsById(registerDTO.getCitizenId())) {
            throw new IllegalArgumentException("CCCD đã được đăng ký");
        }

        // Create Person entity
        Person person = new Person();
        person.setCitizenId(registerDTO.getCitizenId());
        person.setFullName(registerDTO.getFullName());
        person.setDateOfBirth(registerDTO.getDateOfBirth());
        person.setSex(registerDTO.getSex());
        person.setPhone(registerDTO.getPhone());
        person.setEmail(registerDTO.getEmail());
        person.setAddress(registerDTO.getAddress());
        person.setNationality("Việt Nam");

        personRepository.save(person);

        // Create User entity
        User user = new User();
        user.setPerson(person);
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRole((short) 0); // 0 = Cashier
        user.setIsActive(false); // Need admin to activate

        return userRepository.save(user);
    }
}
