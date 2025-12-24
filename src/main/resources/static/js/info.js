document.addEventListener('DOMContentLoaded', function() {
    const representativeName = document.getElementById('representativeName');
    const email = document.getElementById('email');
    const phone = document.getElementById('phone');

    representativeName.addEventListener('blur', function() {
        if (!validateFullName(this.value)) {
            addValidationFeedback(this, false, 'Họ và tên chỉ chứa chữ cái và khoảng trắng');
        } else {
            removeValidationFeedback(this);
        }
    });

    email.addEventListener('blur', function() {
        if (!validateEmail(this.value)) {
            addValidationFeedback(this, false, 'Email không hợp lệ');
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
});