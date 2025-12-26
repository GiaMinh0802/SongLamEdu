package com.songlam.edu.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final String fullName;

    protected CustomUserDetails(CustomUserBuilder builder) {
        super(
                builder.username,
                builder.password,
                builder.enabled,
                !builder.accountExpired,
                !builder.credentialsExpired,
                !builder.accountLocked,
                builder.authorities
        );
        this.fullName = builder.fullName;
    }

    public static CustomUserBuilder builder(String username) {
        return new CustomUserBuilder(username);
    }

    public static class CustomUserBuilder {

        private final String username;
        private String password;
        private String fullName;
        private boolean enabled = true;
        private boolean accountExpired;
        private boolean accountLocked;
        private boolean credentialsExpired;
        private Collection<? extends GrantedAuthority> authorities;

        private CustomUserBuilder(String username) {
            this.username = username;
        }

        public CustomUserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public CustomUserBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public CustomUserBuilder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }

        public CustomUserBuilder disabled(boolean disabled) {
            this.enabled = !disabled;
            return this;
        }

        public CustomUserBuilder accountExpired(boolean accountExpired) {
            this.accountExpired = accountExpired;
            return this;
        }

        public CustomUserBuilder accountLocked(boolean accountLocked) {
            this.accountLocked = accountLocked;
            return this;
        }

        public CustomUserBuilder credentialsExpired(boolean credentialsExpired) {
            this.credentialsExpired = credentialsExpired;
            return this;
        }

        public CustomUserDetails build() {
            return new CustomUserDetails(this);
        }
    }
}