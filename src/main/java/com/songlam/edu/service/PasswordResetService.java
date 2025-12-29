package com.songlam.edu.service;

import com.songlam.edu.dto.ForgotPasswordDTO;
import com.songlam.edu.dto.ResetPasswordDTO;
import com.songlam.edu.entity.PasswordResetToken;
import com.songlam.edu.entity.User;
import com.songlam.edu.repository.PasswordResetTokenRepository;
import com.songlam.edu.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String verifyUserAndCreateToken(ForgotPasswordDTO dto) {

        Optional<User> userOpt = userRepository.findByCitizenIdAndPerson_FullNameAndPerson_DateOfBirthAndPerson_PhoneAndPerson_Email(
                dto.getCitizenId(),
                dto.getFullName(),
                dto.getDateOfBirth(),
                dto.getPhone(),
                dto.getEmail()
        );

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Thông tin không khớp với bất kỳ tài khoản nào");
        }

        User user = userOpt.get();

        tokenRepository.deleteByUser_CitizenId(user.getCitizenId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(resetToken);

        return token;
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        return tokenOpt.isPresent() && !tokenOpt.get().isExpired() && !tokenOpt.get().isUsed();
    }

    @Transactional
    public void resetPassword(ResetPasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        if (!isValidPassword(dto.getNewPassword())) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số");
        }

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(dto.getToken());
        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }

        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Token đã hết hạn");
        }
        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Token đã được sử dụng");
        }

        // Update password
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    private boolean isValidPassword(String password) {
        return password != null &&
                password.length() >= 8 &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*\\d.*");
    }
}