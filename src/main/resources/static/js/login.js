// Login page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Form validation
    const loginForm = document.querySelector('form');
    const emailInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');

    // Toggle password visibility
    document.querySelectorAll('.toggle-password').forEach(button => {
        button.addEventListener('click', function() {
            const targetId = this.getAttribute('data-target');
            const input = document.getElementById(targetId);
            const eyeIcon = this.querySelector('.eye-icon');

            if (input.type === 'password') {
                input.type = 'text';
                eyeIcon.textContent = '🙈';
            } else {
                input.type = 'password';
                eyeIcon.textContent = '👁';
            }
        });
    });

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
            addValidationFeedbackWrapper(this.parentElement, false, 'Mật khẩu không được để trống');
        } else if (!validatePassword(this.value)) {
            addValidationFeedbackWrapper(this.parentElement, false, 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
        } else {
            removeValidationFeedbackWrapper(this.parentElement);
        }
    });

    passwordInput.addEventListener('focus', function() {
        if (this.value && validatePassword(this.value)) {
            removeValidationFeedbackWrapper(this.parentElement);
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
            addValidationFeedbackWrapper(passwordInput.parentElement, false, 'Mật khẩu không được để trống');
            isValid = false;
        } else if (!validatePassword(passwordInput.value)) {
            addValidationFeedbackWrapper(passwordInput.parentElement, false, 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
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

    // Helper functions for password wrapper
    function addValidationFeedbackWrapper(wrapper, isValid, message) {
        const formGroup = wrapper.closest('.form-group');
        let feedback = formGroup.querySelector('.validation-feedback');

        if (!feedback) {
            feedback = document.createElement('small');
            feedback.className = 'validation-feedback';
            formGroup.appendChild(feedback);
        }

        feedback.textContent = message;
        feedback.className = isValid ? 'validation-feedback success' : 'validation-feedback error';
        wrapper.querySelector('input').className = isValid ? 'success' : 'error';
    }

    function removeValidationFeedbackWrapper(wrapper) {
        const formGroup = wrapper.closest('.form-group');
        const feedback = formGroup.querySelector('.validation-feedback');
        if (feedback) feedback.remove();
        wrapper.querySelector('input').className = '';
    }
});