package com.songlam.edu.security;

import com.songlam.edu.entity.User;
import com.songlam.edu.exception.AccountNotActivatedException;
import com.songlam.edu.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public @NullMarked UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByPersonEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        if (!user.getIsActive()) {
            throw new AccountNotActivatedException("Tài khoản chưa được kích hoạt");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() == 1) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_CASHIER"));
        }

        return CustomUserDetails
                .builder(user.getPerson().getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .fullName(user.getPerson().getFullName())
                .build();
    }
}
