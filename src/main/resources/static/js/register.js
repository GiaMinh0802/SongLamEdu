// Register page JavaScript

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

    // Validation functions
    function validateCitizenId(citizenId) {
        // Must be 12 digits or max 15 characters (for format like 033169011971-01)
        const re = /^\d{12}$|^[\d-]{1,15}$/;
        return re.test(citizenId);
    }

    function validateFullName(name) {
        // Only Vietnamese characters and spaces
        const re = /^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵỷỹ\s]+$/;
        return re.test(name) && name.trim().length >= 2;
    }

    function validatePhone(phone) {
        // Must be exactly 10 digits
        const re = /^\d{10}$/;
        return re.test(phone);
    }

    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    function validatePassword(password) {
        // Min 8 chars, at least one uppercase, one lowercase, one number
        const re = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
        return re.test(password);
    }

    function validateAge(dateStr) {
        // Parse dd/MM/yyyy format
        const parts = dateStr.split('/');
        if (parts.length !== 3) return false;

        const day = parseInt(parts[0], 10);
        const month = parseInt(parts[1], 10) - 1;
        const year = parseInt(parts[2], 10);

        const birthDate = new Date(year, month, day);
        if (isNaN(birthDate.getTime())) return false;

        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDiff = today.getMonth() - birthDate.getMonth();

        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }

        return age >= 16 && age <= 100;
    }

    // Add validation feedback
    function addValidationFeedback(input, isValid, message) {
        const formGroup = input.parentElement;
        let feedback = formGroup.querySelector('.validation-feedback');

        if (!feedback) {
            feedback = document.createElement('small');
            feedback.className = 'validation-feedback';
            formGroup.appendChild(feedback);
        }

        feedback.textContent = message;
        feedback.className = isValid ? 'validation-feedback success' : 'validation-feedback error';
        input.className = isValid ? 'success' : 'error';
    }

    function removeValidationFeedback(input) {
        const formGroup = input.parentElement;
        const feedback = formGroup.querySelector('.validation-feedback');
        const hint = formGroup.querySelector('.hint');

        if (feedback && !hint) {
            feedback.remove();
        } else if (feedback && hint) {
            feedback.remove();
        }
        input.className = '';
    }

    // CCCD validation
    citizenIdInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'CCCD không được để trống');
        } else if (!validateCitizenId(this.value)) {
            addValidationFeedback(this, false, 'CCCD phải là 12 số hoặc định dạng hợp lệ');
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
            addValidationFeedback(this, false, 'Tuổi phải từ 16 đến 100');
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
            addValidationFeedback(this, false, 'Mật khẩu không được để trống');
        } else if (!validatePassword(this.value)) {
            addValidationFeedback(this, false, 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
        } else {
            removeValidationFeedback(this);
        }
    });

    // Confirm password validation
    confirmPasswordInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Vui lòng xác nhận mật khẩu');
        } else if (this.value !== passwordInput.value) {
            addValidationFeedback(this, false, 'Mật khẩu xác nhận không khớp');
        } else {
            removeValidationFeedback(this);
        }
    });

    // Re-validate confirm password when password changes
    passwordInput.addEventListener('input', function() {
        if (confirmPasswordInput.value !== '') {
            if (confirmPasswordInput.value !== this.value) {
                addValidationFeedback(confirmPasswordInput, false, 'Mật khẩu xác nhận không khớp');
            } else {
                removeValidationFeedback(confirmPasswordInput);
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
            addValidationFeedback(passwordInput, false, 'Mật khẩu không hợp lệ');
            isValid = false;
        }

        if (confirmPasswordInput.value !== passwordInput.value) {
            addValidationFeedback(confirmPasswordInput, false, 'Mật khẩu xác nhận không khớp');
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
});
