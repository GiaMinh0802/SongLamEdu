let infoDatepickers = [];
let infoDatepickerInitialized = false;

function toggleEditMode() {
  const container = document.getElementById('info-container');
  if (!container) return;

  const wasEditing = container.classList.contains('editing');
  container.classList.toggle('editing');
  const isEditing = container.classList.contains('editing');

  // Toggle view / edit
  document.querySelectorAll('[data-view]')
      .forEach(el => el.style.display = isEditing ? 'none' : 'block');

  document.querySelectorAll('[data-edit]')
      .forEach(el => el.classList.toggle('hidden', !isEditing));

  // Toggle buttons
  const updateBtn = document.getElementById('btn-update');
  const saveGroup = document.getElementById('save-group');
  if (updateBtn) updateBtn.classList.toggle('hidden', isEditing);
  if (saveGroup) saveGroup.classList.toggle('hidden', !isEditing);

  /* ================================
     ENTER EDIT MODE
  ================================= */
  if (!wasEditing && isEditing) {
    infoDatepickers = [];
    document.querySelectorAll('.datepicker-edit').forEach(input => {
      infoDatepickers.push(new DatePicker(input));
    });
  }

  /* ================================
     EXIT EDIT MODE (HỦY)
  ================================= */
  if (wasEditing && !isEditing) {
    infoDatepickers.forEach(dp => dp.destroy());
    infoDatepickers = [];
  }
}

document.addEventListener('DOMContentLoaded', function() {
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

  function validatePassword(password) {
    // Min 8 chars, at least one uppercase, one lowercase, one number
    const re = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
    return re.test(password);
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
      const firstError = document.querySelector('.error');
      if (firstError) {
        firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    } else {
      const submitBtn = changePasswordForm.querySelector('.btn-edit');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Đang xử lý...';
    }
  })
})
