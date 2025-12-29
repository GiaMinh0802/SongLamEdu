document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    const citizenIdInput = document.getElementById('citizenId');
    const fullNameInput = document.getElementById('fullName');
    const dateOfBirthInput = document.getElementById('dateOfBirth');
    const sexInput = document.getElementById('sex');
    const phoneInput = document.getElementById('phone');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');

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

    // CCCD validation
    citizenIdInput.addEventListener('blur', function() {
        const re = /^\d{12}$/;
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'CCCD không được để trống');
        } else if (!re.test(this.value)) {
            addValidationFeedback(this, false, 'CCCD phải là 12 số');
        } else {
            removeValidationFeedback(this);
        }
    });

    // Full name validation
    fullNameInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Họ và tên không được để trống');
        } else if (!validateFullName(this.value)) {
            addValidationFeedback(this, false, 'Họ và tên chỉ chứa chữ cái và khoảng trắng');
        } else {
            removeValidationFeedback(this);
        }
    });

    // Date of birth validation - only validate on change, not blur
    dateOfBirthInput.addEventListener('change', function() {
        if (this.value === '') {
            addValidationFeedback(this, false, 'Ngày sinh không được để trống');
        } else if (!validateAge(this.value)) {
            addValidationFeedback(this, false, 'Phải từ 6 tuổi trở lên');
        } else {
            removeValidationFeedback(this);
        }
    });

    // Sex validation
    sexInput.addEventListener('change', function() {
        if (this.value === '') {
            addValidationFeedback(this, false, 'Vui lòng chọn giới tính');
        } else {
            removeValidationFeedback(this);
        }
    });

    // Phone validation
    phoneInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Số điện thoại không được để trống');
        } else if (!validatePhone(this.value)) {
            addValidationFeedback(this, false, 'Số điện thoại phải có đúng 10 số');
        } else {
            removeValidationFeedback(this);
        }
    });

    // Only allow digits for phone
    phoneInput.addEventListener('input', function() {
        this.value = this.value.replace(/\D/g, '');
    });

    // Email validation
    emailInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Email không được để trống');
        } else if (!validateEmail(this.value)) {
            addValidationFeedback(this, false, 'Email không hợp lệ');
        } else {
            removeValidationFeedback(this);
        }
    });

    // Password validation
    passwordInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedbackWrapper(this.parentElement, false, 'Mật khẩu không được để trống');
        } else if (!validatePassword(this.value)) {
            addValidationFeedbackWrapper(this.parentElement, false, 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
        } else {
            removeValidationFeedbackWrapper(this.parentElement);
        }
    });

    // Confirm password validation
    confirmPasswordInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedbackWrapper(this.parentElement, false, 'Vui lòng xác nhận mật khẩu');
        } else if (this.value !== passwordInput.value) {
            addValidationFeedbackWrapper(this.parentElement, false, 'Mật khẩu xác nhận không khớp');
        } else {
            removeValidationFeedbackWrapper(this.parentElement);
        }
    });

    // Re-validate confirm password when password changes
    passwordInput.addEventListener('input', function() {
        if (confirmPasswordInput.value !== '') {
            if (confirmPasswordInput.value !== this.value) {
                addValidationFeedbackWrapper(confirmPasswordInput.parentElement, false, 'Mật khẩu xác nhận không khớp');
            } else {
                removeValidationFeedbackWrapper(confirmPasswordInput.parentElement);
            }
        }
    });

    // Form submission validation
    registerForm.addEventListener('submit', function(e) {
        let isValid = true;

        // Validate all fields
        if (citizenIdInput.value.trim() === '' || !validateCitizenId(citizenIdInput.value)) {
            addValidationFeedback(citizenIdInput, false, 'CCCD không hợp lệ');
            isValid = false;
        }

        if (fullNameInput.value.trim() === '' || !validateFullName(fullNameInput.value)) {
            addValidationFeedback(fullNameInput, false, 'Họ và tên không hợp lệ');
            isValid = false;
        }

        if (dateOfBirthInput.value === '' || !validateAge(dateOfBirthInput.value)) {
            addValidationFeedback(dateOfBirthInput, false, 'Ngày sinh không hợp lệ');
            isValid = false;
        }

        if (sexInput.value === '') {
            addValidationFeedback(sexInput, false, 'Vui lòng chọn giới tính');
            isValid = false;
        }

        if (phoneInput.value.trim() === '' || !validatePhone(phoneInput.value)) {
            addValidationFeedback(phoneInput, false, 'Số điện thoại không hợp lệ');
            isValid = false;
        }

        if (emailInput.value.trim() === '' || !validateEmail(emailInput.value)) {
            addValidationFeedback(emailInput, false, 'Email không hợp lệ');
            isValid = false;
        }

        if (passwordInput.value.trim() === '' || !validatePassword(passwordInput.value)) {
            addValidationFeedbackWrapper(passwordInput.parentElement, false, 'Mật khẩu không hợp lệ');
            isValid = false;
        }

        if (confirmPasswordInput.value !== passwordInput.value) {
            addValidationFeedbackWrapper(confirmPasswordInput.parentElement, false, 'Mật khẩu xác nhận không khớp');
            isValid = false;
        }

        if (!isValid) {
            e.preventDefault();
            // Scroll to first error
            const firstError = document.querySelector('.error');
            if (firstError) {
                firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        } else {
            // Show loading state
            const submitBtn = registerForm.querySelector('.btn-register');
            submitBtn.disabled = true;
            submitBtn.textContent = 'Đang xử lý...';
        }
    });

    // Auto-hide success alerts after 5 seconds
    const successAlerts = document.querySelectorAll('.alert-success');
    successAlerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
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