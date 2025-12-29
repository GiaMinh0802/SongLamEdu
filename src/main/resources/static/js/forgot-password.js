document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('forgot-password-form');
    const citizenIdInput = document.getElementById('citizenId');
    const fullNameInput = document.getElementById('fullName');
    const dateOfBirthInput = document.getElementById('dateOfBirth');
    const phoneInput = document.getElementById('phone');
    const emailInput = document.getElementById('email');

    // Citizen ID validation
    citizenIdInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Số CCCD không được để trống');
        } else if (!validateCitizenId(this.value)) {
            addValidationFeedback(this, false, 'Số CCCD phải có 12 chữ số');
        } else {
            removeValidationFeedback(this);
        }
    });

    citizenIdInput.addEventListener('input', function() {
        this.value = this.value.replace(/\D/g, '').slice(0, 12);
    });

    // Full name validation
    fullNameInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Họ và tên không được để trống');
        } else if (!validateFullName(this.value)) {
            addValidationFeedback(this, false, 'Họ và tên không hợp lệ');
        } else {
            removeValidationFeedback(this);
        }
    });

    // Date of birth validation with auto-formatting
    dateOfBirthInput.addEventListener('input', function() {
        let value = this.value.replace(/\D/g, '');
        if (value.length >= 2) {
            value = value.slice(0, 2) + '/' + value.slice(2);
        }
        if (value.length >= 5) {
            value = value.slice(0, 5) + '/' + value.slice(5, 9);
        }
        this.value = value;
    });

    dateOfBirthInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Ngày sinh không được để trống');
        } else if (!isValidDateFormat(this.value)) {
            addValidationFeedback(this, false, 'Ngày sinh phải có định dạng dd/MM/yyyy');
        } else {
            removeValidationFeedback(this);
        }
    });

    // Phone validation
    phoneInput.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Số điện thoại không được để trống');
        } else if (!validatePhone(this.value)) {
            addValidationFeedback(this, false, 'Số điện thoại phải có 10 chữ số');
        } else {
            removeValidationFeedback(this);
        }
    });

    phoneInput.addEventListener('input', function() {
        this.value = this.value.replace(/\D/g, '').slice(0, 10);
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

    // Form submission
    form.addEventListener('submit', function(e) {
        let isValid = true;

        if (citizenIdInput.value.trim() === '' || !validateCitizenId(citizenIdInput.value)) {
            addValidationFeedback(citizenIdInput, false, 'Số CCCD phải có 12 chữ số');
            isValid = false;
        }

        if (fullNameInput.value.trim() === '' || !validateFullName(fullNameInput.value)) {
            addValidationFeedback(fullNameInput, false, 'Họ và tên không hợp lệ');
            isValid = false;
        }

        if (dateOfBirthInput.value.trim() === '' || !isValidDateFormat(dateOfBirthInput.value)) {
            addValidationFeedback(dateOfBirthInput, false, 'Ngày sinh phải có định dạng dd/MM/yyyy');
            isValid = false;
        }

        if (phoneInput.value.trim() === '' || !validatePhone(phoneInput.value)) {
            addValidationFeedback(phoneInput, false, 'Số điện thoại phải có 10 chữ số');
            isValid = false;
        }

        if (emailInput.value.trim() === '' || !validateEmail(emailInput.value)) {
            addValidationFeedback(emailInput, false, 'Email không hợp lệ');
            isValid = false;
        }

        if (!isValid) {
            e.preventDefault();
        } else {
            const submitBtn = form.querySelector('.btn-login');
            submitBtn.disabled = true;
            submitBtn.textContent = 'Đang xác minh...';
        }
    });

    function isValidDateFormat(dateStr) {
        const re = /^\d{2}\/\d{2}\/\d{4}$/;
        if (!re.test(dateStr)) return false;

        const parts = dateStr.split('/');
        const day = parseInt(parts[0], 10);
        const month = parseInt(parts[1], 10);
        const year = parseInt(parts[2], 10);

        if (month < 1 || month > 12) return false;
        if (day < 1 || day > 31) return false;
        if (year < 1900 || year > new Date().getFullYear()) return false;

        return true;
    }
});