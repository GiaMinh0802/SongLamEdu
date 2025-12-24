// ===== Common utility functions for popup management =====

function openPopup(popupId) {
    const popup = document.getElementById(popupId);
    if (popup) {
        popup.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
}

function closePopup(popupId) {
    const popup = document.getElementById(popupId);
    if (popup) {
        popup.classList.remove('active');
        document.body.style.overflow = '';
    }
}

function closePopupOnOverlay(event, popupId) {
    if (event.target.id === popupId) {
        closePopup(popupId);
    }
}

function setupEscapeKeyListener(popupIds) {
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            popupIds.forEach(popupId => closePopup(popupId));
        }
    });
}

// ===== Common validation functions common =====

function validateCitizenId(citizenId) {
    const re = /^\d{12}(-\d{2})?$/;
    return re.test(citizenId);
}

function validateFullName(name) {
    const re = /^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵỷỹ\s]+$/;
    return re.test(name) && name.trim().length >= 2;
}

function validatePhone(phone) {
    return /^\d{10}$/.test(phone);
}

function validateEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function validatePassword(password) {
    return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/.test(password);
}

function validateAge(dateStr) {
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
    return age >= 6;
}

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
    if (feedback) feedback.remove();
    input.className = '';
}

function removeAllValidationFeedback(container) {
    container.querySelectorAll('.validation-feedback').forEach(el => el.remove());

    container.querySelectorAll('input, select, textarea').forEach(input => {
        input.className = input.className.replace(/\b(valid|invalid)\b/g, '').trim();
    });
}