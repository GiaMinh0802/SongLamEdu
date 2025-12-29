document.addEventListener('DOMContentLoaded', function() {

    const singleTab = document.getElementById('singleTab');
    const hasError = singleTab.querySelector('.alert-error');

    if (hasError) {
        openPopup('addStudentPopup');
        switchTab('single');
    }

    const bulkTab = document.getElementById('bulkTab');
    const hasImportMessage = bulkTab.querySelector('.alert-info');

    if (hasImportMessage) {
        openPopup('addStudentPopup');
        switchTab('bulk');
    }

    const addStudentForm = document.getElementById('addStudentForm');
    const citizenId = document.getElementById('citizenId');
    const fullName = document.getElementById('fullName');
    const dateOfBirth = document.getElementById('dateOfBirth');
    const sex = document.getElementById('sex');
    const phone = document.getElementById('phone');

    citizenId.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'CCCD không được để trống');
        } else if (!validateCitizenId(this.value)) {
            addValidationFeedback(this, false, 'CCCD phải là 12 số hoặc định dạng hợp lệ');
        } else {
            removeValidationFeedback(this);
        }
    });

    fullName.addEventListener('blur', function() {
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Họ và tên không được để trống');
        } else if (!validateFullName(this.value)) {
            addValidationFeedback(this, false, 'Họ và tên chỉ chứa chữ cái và khoảng trắng');
        } else {
            removeValidationFeedback(this);
        }
    });

    dateOfBirth.addEventListener('change', function() {
        if (this.value === '') {
            addValidationFeedback(this, false, 'Ngày sinh không được để trống');
        } else if (!validateAge(this.value)) {
            addValidationFeedback(this, false, 'Phải từ 6 tuổi trở lên');
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
        if (this.value.trim() === '') {
            addValidationFeedback(this, false, 'Số điện thoại không được để trống');
        } else if (!validatePhone(this.value)) {
            addValidationFeedback(this, false, 'Số điện thoại phải có đúng 10 số');
        } else {
            removeValidationFeedback(this);
        }
    });

    phone.addEventListener('input', function() {
        this.value = this.value.replace(/\D/g, '');
    });

    addStudentForm.addEventListener('submit', function(e) {
        let isValid = true;

        // Validate all fields
        if (citizenId.value.trim() === '' || !validateCitizenId(citizenId.value)) {
            addValidationFeedback(citizenId, false, 'CCCD không hợp lệ');
            isValid = false;
        }

        if (fullName.value.trim() === '' || !validateFullName(fullName.value)) {
            addValidationFeedback(fullName, false, 'Họ và tên không hợp lệ');
            isValid = false;
        }

        if (dateOfBirth.value === '' || !validateAge(dateOfBirth.value)) {
            addValidationFeedback(dateOfBirth, false, 'Ngày sinh không hợp lệ');
            isValid = false;
        }

        if (sex.value === '') {
            addValidationFeedback(sex, false, 'Vui lòng chọn giới tính');
            isValid = false;
        }

        if (phone.value.trim() === '' || !validatePhone(phone.value)) {
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
            const submitBtn = addStudentForm.querySelector('.btn-register');
            submitBtn.disabled = true;
            submitBtn.textContent = 'Đang xử lý...';
        }
    });
});

function switchTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });

    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    if (tabName === 'single') {
        document.getElementById('singleTab').classList.add('active');
        document.querySelectorAll('.tab-btn')[0].classList.add('active');
    } else if (tabName === 'bulk') {
        document.getElementById('bulkTab').classList.add('active');
        document.querySelectorAll('.tab-btn')[1].classList.add('active');
    }
}

function clearSearchFilters() {
    document.getElementById('citizenIdInput').value = '';
    document.getElementById('fullNameInput').value = '';
    document.getElementById('phoneInput').value = '';
}

function showPaymentHistory(citizenId, fullName) {
    document.getElementById('popupStudentId').textContent = citizenId;
    document.getElementById('popupStudentName').textContent = fullName;

    const tbody = document.getElementById('paymentHistoryBody');
    tbody.innerHTML = '<tr><td colspan="6" class="empty-row">Đang tải...</td></tr>';

    openPopup('paymentHistoryPopup');

    fetch(`/students/api/student/${citizenId}`)
        .then(response => response.json())
        .then(data => {
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="empty-row">Chưa có khoản đóng nào</td></tr>';
                document.getElementById('paymentTotal').textContent = '0';
                return;
            }

            let total = 0;
            tbody.innerHTML = '';

            data.forEach(payment => {
                total += payment.amount || 0;
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${payment.id || '-'}</td>
                    <td>${formatDate(payment.paymentDate)}</td>
                    <td>${payment.academicYearName || '-'}</td>
                    <td>${payment.className || '-'}</td>
                    <td>${payment.subjectName || '-'}</td>
                    <td>${formatCurrency(payment.amount)}</td>
                `;
                tbody.appendChild(row);
            });

            document.getElementById('paymentTotal').textContent = formatCurrency(total);
        })
        .catch(error => {
            console.error('Error:', error);
            tbody.innerHTML = '<tr><td colspan="6" class="empty-row">Có lỗi xảy ra</td></tr>';
        });
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount || 0);
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN');
}

function changePageSize(size) {
    const url = new URL(window.location.href);
    url.searchParams.set('size', size);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}

document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closePopup('addStudentPopup');
    }
});
