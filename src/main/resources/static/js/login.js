// Login page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Form validation
    const loginForm = document.querySelector('form');
    const emailInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');

    // Email input validation
    emailInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Email không được để trống');
        } else if (!validateEmail(this.value)) {
            addValidationFeedback(this, false, 'Email không hợp lệ');
        } else {
            removeValidationFeedback(this);
        }
    });

    emailInput.addEventListener('focus', function() {
        if (this.value && validateEmail(this.value)) {
            removeValidationFeedback(this);
        }
    });

    // Password input validation
    passwordInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Mật khẩu không được để trống');
        } else if (!validatePassword(this.value)) {
            addValidationFeedback(this, false, 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
        } else {
            removeValidationFeedback(this);
        }
    });

    passwordInput.addEventListener('focus', function() {
        if (this.value && validatePassword(this.value)) {
            removeValidationFeedback(this);
        }
    });

    // Form submit validation
    loginForm.addEventListener('submit', function(e) {
        let isValid = true;

        // Validate email
        if (emailInput.value.trim() === '') {
            addValidationFeedback(emailInput, false, 'Email không được để trống');
            isValid = false;
        } else if (!validateEmail(emailInput.value)) {
            addValidationFeedback(emailInput, false, 'Email không hợp lệ');
            isValid = false;
        }

        // Validate password
        if (passwordInput.value.trim() === '') {
            addValidationFeedback(passwordInput, false, 'Mật khẩu không được để trống');
            isValid = false;
        } else if (!validatePassword(passwordInput.value)) {
            addValidationFeedback(passwordInput, false, 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
            isValid = false;
        }

        if (!isValid) {
            e.preventDefault();
        } else {
            // Show loading state
            const submitBtn = loginForm.querySelector('.btn-login');
            submitBtn.disabled = true;
            submitBtn.textContent = 'Đang đăng nhập...';
        }
    });

    const successAlerts = document.querySelectorAll('.alert-success');
    successAlerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    emailInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            passwordInput.focus();
        }
    });
});
