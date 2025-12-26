document.addEventListener('DOMContentLoaded', function() {
    const changeInfoForm = document.getElementById('changeInfoForm');
    const fullName = document.getElementById('fullName');
    const sex = document.getElementById('sex');
    const phone = document.getElementById('phone');
    const email = document.getElementById('email');
    const status = document.getElementById('status');
    const endDate = document.getElementById('endDate');

    fullName.addEventListener('blur', function() {
        if (!validateFullName(this.value)) {
            addValidationFeedback(this, false, 'Họ và tên chỉ chứa chữ cái và khoảng trắng');
        } else {
            removeValidationFeedback(this);
        }
    });

    sex.addEventListener('change', function() {
        if (this.value === '') {
            addValidationFeedback(this, false, 'Vui lòng chọn giới tính');
        } else {
            removeValidationFeedback(this);
        }
    });

    phone.addEventListener('blur', function() {
        if (!validatePhone(this.value)) {
            addValidationFeedback(this, false, 'Số điện thoại phải có đúng 10 số');
        } else {
            removeValidationFeedback(this);
        }
    });

    phone.addEventListener('input', function() {
        this.value = this.value.replace(/\D/g, '');
    });

    email.addEventListener('blur', function() {
        if (this.value && !validateEmail(this.value)) {
            addValidationFeedback(this, false, 'Email không hợp lệ');
        } else {
            removeValidationFeedback(this);
        }
    });

    status.addEventListener('change', function() {
        if (this.value === '0' && !endDate.value) {
            addValidationFeedback(endDate, false, 'Ngày thôi học không được để trống');
        } else {
            removeValidationFeedback(endDate);
        }
    });

    endDate.addEventListener('blur', function() {
        if (status.value === '0' && !this.value) {
            addValidationFeedback(this, false, 'Ngày thôi học không được để trống');
        } else {
            removeValidationFeedback(this);
        }
    });

    changeInfoForm.addEventListener('submit', function(e) {
        let isValid = true;

        if (!validateFullName(fullName.value)) {
            addValidationFeedback(fullName, false, 'Họ và tên không hợp lệ');
            isValid = false;
        }

        if (!validatePhone(phone.value)) {
            addValidationFeedback(phone, false, 'Số điện thoại không hợp lệ');
            isValid = false;
        }

        if (email.value && !validateEmail(email.value)) {
            addValidationFeedback(email, false, 'Email không hợp lệ');
            isValid = false;
        }

        if (status.value === '0' && !endDate.value) {
            addValidationFeedback(endDate, false, 'Ngày thôi học không được để trống');
            isValid = false;
        }

        if (!isValid) {
            e.preventDefault();
            const firstError = document.querySelector('.error');
            if (firstError) {
                firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        } else {
            const submitBtn = changeInfoForm.querySelector('.btn-edit');
            submitBtn.disabled = true;
        }
    });
})
