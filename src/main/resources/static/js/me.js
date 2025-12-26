document.addEventListener('DOMContentLoaded', function() {
  const changeInfoForm = document.getElementById('changeInfoForm');
  const fullName = document.getElementById('fullName');
  const sex = document.getElementById('sex');
  const phone = document.getElementById('phone');

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

  const changePasswordForm = document.getElementById('changePasswordForm');
  const oldPassword = document.getElementById('oldPassword');
  const newPassword = document.getElementById('newPassword');
  const confirmPassword = document.getElementById('confirmPassword');
  const errorHidden = document.getElementById('errorHidden');

  if (errorHidden && errorHidden.value === 'true') {
    addValidationFeedback(
        oldPassword,
        false,
        'Mật khẩu cũ không đúng'
    );

    oldPassword.focus();
    oldPassword.scrollIntoView({
      behavior: 'smooth',
      block: 'center'
    });
  }

  changePasswordForm.addEventListener('submit', function (e) {

    removeValidationFeedback(oldPassword);
    removeValidationFeedback(newPassword);
    removeValidationFeedback(confirmPassword)

    let isValid = true;

    if (!validatePassword(oldPassword.value)) {
      addValidationFeedback(oldPassword, false, 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
      isValid = false;
    }

    if (!validatePassword(newPassword.value)) {
      addValidationFeedback(newPassword, false, 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
      isValid = false;
    }

    if (oldPassword.value === newPassword.value) {
      addValidationFeedback(newPassword, false, 'Mật khẩu mới không được trùng với mật khẩu cũ');
      isValid = false;
    }

    if (confirmPassword.value !== newPassword.value) {
      addValidationFeedback(confirmPassword, false, 'Mật khẩu xác nhận không khớp');
      isValid = false;
    }

    if (!isValid) {
      e.preventDefault();
      const firstError = changePasswordForm.querySelector('input.error');
      if (firstError) {
        firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    } else {
      const submitBtn = changePasswordForm.querySelector('.btn-edit');
      submitBtn.disabled = true;
    }
  })
})
