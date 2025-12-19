package com.songlam.edu.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountNotActivatedException extends AuthenticationException {
    public AccountNotActivatedException(String msg) {
        super(msg);
    }

    public AccountNotActivatedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
