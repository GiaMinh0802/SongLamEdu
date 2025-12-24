package com.songlam.edu.config;

import com.songlam.edu.security.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        TokenBasedRememberMeServices rememberMeServices = new TokenBasedRememberMeServices(
                "songLamEduRememberMeKey",
                userDetailsService
        );
        rememberMeServices.setTokenValiditySeconds(30 * 24 * 60 * 60); // 30 days
        rememberMeServices.setAlwaysRemember(true); // Always remember
        return rememberMeServices;
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                              Authentication authentication) throws IOException {
                response.sendRedirect("/me");
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                                AuthenticationException exception) throws IOException {
                String username = request.getParameter("username");
                String errorMessage = determineErrorMessage(exception);

                request.getSession().setAttribute("LOGIN_ERROR", errorMessage);
                request.getSession().setAttribute("LOGIN_USERNAME", username);

                response.sendRedirect("/login?error");
            }

            private String determineErrorMessage(AuthenticationException exception) {
                return switch (exception) {
                    case UsernameNotFoundException usernameNotFoundException -> "Tài khoản chưa được đăng ký";
                    case BadCredentialsException badCredentialsException -> "Mật khẩu không chính xác";
                    case InternalAuthenticationServiceException serviceException -> serviceException.getMessage();
                    case null, default -> "Đăng nhập thất bại. Vui lòng thử lại";
                };
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/forgot-password", "/reset-password", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/users/**", "/dashboard/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/transactions/**").hasAnyRole("ADMIN", "CASHIER")
                .requestMatchers(HttpMethod.POST, "/transactions/revenues").hasRole("CASHIER")
                .requestMatchers(HttpMethod.POST, "/transactions/revenues/*/cancel").hasAnyRole("ADMIN", "CASHIER")
                .requestMatchers(HttpMethod.GET, "/students/**").hasAnyRole("ADMIN", "CASHIER")
                .requestMatchers(HttpMethod.POST, "/students").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/students/import").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/students/import/template").hasRole("ADMIN")
                .requestMatchers("/me").hasAnyRole("ADMIN", "CASHIER")
                .requestMatchers(HttpMethod.GET, "/info").hasAnyRole("ADMIN", "CASHIER")
                .requestMatchers(HttpMethod.POST, "/info").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customSuccessHandler())
                .failureHandler(customFailureHandler())
                .permitAll()
            )
            .rememberMe(remember -> remember
                .rememberMeServices(rememberMeServices())
                .key("songLamEduRememberMeKey")
                .tokenValiditySeconds(30 * 24 * 60 * 60) // 30 days
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }
}
