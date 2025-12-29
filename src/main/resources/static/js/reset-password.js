document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('reset-password-form');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const strengthContainer = document.getElementById('password-strength');
    const strengthFill = strengthContainer.querySelector('.strength-fill');
    const strengthText = strengthContainer.querySelector('.strength-text');

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

    // Password strength checker
    newPasswordInput.addEventListener('input', function() {
        const password = this.value;
        strengthContainer.classList.toggle('visible', password.length > 0);

        if (password.length === 0) return;

        const strength = checkPasswordStrength(password);
        strengthFill.className = 'strength-fill ' + strength.level;
        strengthText.className = 'strength-text ' + strength.level;
        strengthText.textContent = strength.text;
    });

    // New password validation
    newPasswordInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this.parentElement, false, 'Mật khẩu không được để trống');
        } else if (!validatePassword(this.value)) {
            addValidationFeedback(this.parentElement, false, 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
        } else {
            removeValidationFeedbackWrapper(this.parentElement);
        }
    });

    // Confirm password validation
    confirmPasswordInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this.parentElement, false, 'Vui lòng xác nhận mật khẩu');
        } else if (this.value !== newPasswordInput.value) {
            addValidationFeedback(this.parentElement, false, 'Mật khẩu xác nhận không khớp');
        } else {
            removeValidationFeedbackWrapper(this.parentElement);
        }
    });

    confirmPasswordInput.addEventListener('input', function() {
        if (this.value === newPasswordInput.value && this.value.length > 0) {
            removeValidationFeedbackWrapper(this.parentElement);
        }
    });

    // Form submission
    form.addEventListener('submit', function(e) {
        let isValid = true;

        if (newPasswordInput.value.trim() === '' || !validatePassword(newPasswordInput.value)) {
            addValidationFeedback(newPasswordInput.parentElement, false,
                'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
            isValid = false;
        }

        if (confirmPasswordInput.value.trim() === '') {
            addValidationFeedback(confirmPasswordInput.parentElement, false, 'Vui lòng xác nhận mật khẩu');
            isValid = false;
        } else if (confirmPasswordInput.value !== newPasswordInput.value) {
            addValidationFeedback(confirmPasswordInput.parentElement, false, 'Mật khẩu xác nhận không khớp');
            isValid = false;
        }

        if (!isValid) {
            e.preventDefault();
        } else {
            const submitBtn = form.querySelector('.btn-login');
            submitBtn.disabled = true;
            submitBtn.textContent = 'Đang xử lý...';
        }
    });

    function checkPasswordStrength(password) {
        let score = 0;
        if (password.length >= 8) score++;
        if (password.length >= 12) score++;
        if (/[a-z]/.test(password)) score++;
        if (/[A-Z]/.test(password)) score++;
        if (/\d/.test(password)) score++;
        if (/[^a-zA-Z0-9]/.test(password)) score++;

        if (score <= 2) return { level: 'weak', text: 'Yếu' };
        if (score <= 4) return { level: 'medium', text: 'Trung bình' };
        return { level: 'strong', text: 'Mạnh' };
    }

    function addValidationFeedback(wrapper, isValid, message) {
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