// Classes Management JavaScript

// Get current user role
let currentUserRole = null;

function initUserRole() {
    const roleElement = document.getElementById('userRole');
    if (roleElement) {
        currentUserRole = roleElement.value;
    }
}

// Call when page loads
document.addEventListener('DOMContentLoaded', function() {
    initUserRole();
});

// Get CSRF token from meta tags
function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]')?.content;
}

function getCsrfHeader() {
    return document.querySelector('meta[name="_csrf_header"]')?.content;
}

// Selected students for adding to subject
let selectedStudents = new Set();
let searchTimeout = null;

function loadClasses() {
    const yearId = document.getElementById('academicYearSelect').value;
    const classSelect = document.getElementById('classSelect');
    const subjectSelect = document.getElementById('subjectSelect');

    // Reset dependent dropdowns
    classSelect.innerHTML = '<option value="">-- Chọn lớp học --</option>';
    subjectSelect.innerHTML = '<option value="">-- Chọn môn học --</option>';
    clearStudentTable();
    updateAddStudentButton();

    if (!yearId) return;

    fetch(`/classes/api/classes?academicYearId=${yearId}`)
        .then(response => response.json())
        .then(data => {
            data.forEach(cls => {
                const option = document.createElement('option');
                option.value = cls.id;
                option.textContent = cls.className;
                classSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error loading classes:', error);
        });
}

function loadSubjects() {
    const classId = document.getElementById('classSelect').value;
    const subjectSelect = document.getElementById('subjectSelect');

    // Reset dependent dropdown
    subjectSelect.innerHTML = '<option value="">-- Chọn môn học --</option>';
    clearStudentTable();
    updateAddStudentButton();

    if (!classId) return;

    fetch(`/classes/api/subjects?classId=${classId}`)
        .then(response => response.json())
        .then(data => {
            data.forEach(subject => {
                const option = document.createElement('option');
                option.value = subject.id;
                option.textContent = subject.subjectName;
                subjectSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error loading subjects:', error);
        });
}

function loadStudents() {
    const subjectId = document.getElementById('subjectSelect').value;
    const tbody = document.getElementById('studentTableBody');

    updateAddStudentButton();

    if (!subjectId) {
        clearStudentTable();
        return;
    }

    // Show loading
    tbody.innerHTML = `
        <tr class="loading-row">
            <td colspan="7">
                <span class="loading-spinner"></span>
                Đang tải dữ liệu...
            </td>
        </tr>
    `;

    fetch(`/classes/api/students?subjectId=${subjectId}`)
        .then(response => response.json())
        .then(data => {
            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="empty-row">Không có học sinh trong môn học này</td></tr>';
                return;
            }

            tbody.innerHTML = '';
            data.forEach(student => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${student.citizenId || '-'}</td>
                    <td>${student.fullName || '-'}</td>
                    <td>${formatDate(student.dateOfBirth)}</td>
                    <td>
                        <span class="badge ${student.sex === 1 ? 'badge-pink' : 'badge-blue'}">
                            ${student.sex === 1 ? 'Nữ' : 'Nam'}
                        </span>
                    </td>
                    <td>${student.phone || '-'}</td>
                    <td>
                        <span class="badge ${student.status === 1 ? 'badge-success' : 'badge-danger'}">
                            ${student.status === 1 ? 'Đang học' : 'Thôi học'}
                        </span>
                    </td>
                    <td class="action">
                        ${currentUserRole === 'CASHIER' ? `<button type="button" class="btn btn-sm primary" onclick="openPaymentPopup('${student.citizenId}', '${student.fullName}')">Thu học phí</button>` : ''}
                        ${currentUserRole === 'ADMIN' ? `<button type="button" class="btn btn-sm primary" onclick="confirmRemoveStudent('${student.citizenId}', '${student.fullName}')">Xóa</button>` : ''}
                    </td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error loading students:', error);
            tbody.innerHTML = '<tr><td colspan="7" class="empty-row">Có lỗi xảy ra khi tải dữ liệu</td></tr>';
        });
}

function clearStudentTable() {
    document.getElementById('studentTableBody').innerHTML =
        '<tr><td colspan="7" class="empty-row">Vui lòng chọn năm học, lớp và môn học để xem danh sách học sinh</td></tr>';
}

function updateAddStudentButton() {
    const subjectId = document.getElementById('subjectSelect').value;
    const addStudentBtn = document.getElementById('addStudentBtn');
    if (addStudentBtn) {
        addStudentBtn.disabled = !subjectId;
    }
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return '-';
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
}

function submitAddClass(event) {
    event.preventDefault();

    const className = document.getElementById('newClassName').value.trim();
    const errorDiv = document.getElementById('addClassError');
    const successDiv = document.getElementById('addClassSuccess');

    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';

    if (!className) {
        errorDiv.textContent = 'Vui lòng nhập tên lớp!';
        errorDiv.style.display = 'block';
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
    };

    // Add CSRF token
    const csrfToken = getCsrfToken();
    const csrfHeader = getCsrfHeader();
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    fetch('/classes/add-class', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({ className: className })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                successDiv.textContent = data.message;
                successDiv.style.display = 'block';
                document.getElementById('newClassName').value = '';

                // Reload classes nếu đang có năm học được chọn
                setTimeout(() => {
                    closePopup('addClassPopup');
                    successDiv.style.display = 'none';
                    loadClasses();
                }, 1500);
            } else {
                errorDiv.textContent = data.message;
                errorDiv.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            errorDiv.textContent = 'Có lỗi xảy ra, vui lòng thử lại!';
            errorDiv.style.display = 'block';
        });
}

function submitAddSubject(event) {
    event.preventDefault();

    const subjectName = document.getElementById('newSubjectName').value.trim();
    const errorDiv = document.getElementById('addSubjectError');
    const successDiv = document.getElementById('addSubjectSuccess');

    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';

    if (!subjectName) {
        errorDiv.textContent = 'Vui lòng nhập tên môn học!';
        errorDiv.style.display = 'block';
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
    };

    // Add CSRF token
    const csrfToken = getCsrfToken();
    const csrfHeader = getCsrfHeader();
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    fetch('/classes/add-subject', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({ subjectName: subjectName })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                successDiv.textContent = data.message;
                successDiv.style.display = 'block';
                document.getElementById('newSubjectName').value = '';

                // Reload subjects nếu đang có lớp được chọn
                setTimeout(() => {
                    closePopup('addSubjectPopup');
                    successDiv.style.display = 'none';
                    loadSubjects();
                }, 1500);
            } else {
                errorDiv.textContent = data.message;
                errorDiv.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            errorDiv.textContent = 'Có lỗi xảy ra, vui lòng thử lại!';
            errorDiv.style.display = 'block';
        });
}

// ========== Add Students to Subject Functions ==========

function openAddStudentPopup() {
    const subjectId = document.getElementById('subjectSelect').value;
    if (!subjectId) {
        alert('Vui lòng chọn môn học trước!');
        return;
    }

    // Reset state
    selectedStudents.clear();
    updateSelectedCount();
    document.getElementById('studentSearchInput').value = '';
    document.getElementById('addStudentError').style.display = 'none';
    document.getElementById('addStudentSuccess').style.display = 'none';

    openPopup('addStudentPopup');
    loadAvailableStudents();
}

function loadAvailableStudents(searchName = '') {
    const subjectId = document.getElementById('subjectSelect').value;
    const tbody = document.getElementById('availableStudentTableBody');

    tbody.innerHTML = `
        <tr class="loading-row">
            <td colspan="4">
                <span class="loading-spinner"></span>
                Đang tải danh sách học sinh...
            </td>
        </tr>
    `;

    let url = `/classes/api/students/search?subjectId=${subjectId}&size=100`;
    if (searchName) {
        url += `&fullName=${encodeURIComponent(searchName)}`;
    }

    fetch(url)
        .then(response => response.json())
        .then(data => {
            const students = data.content || [];

            if (students.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="empty-row">Không tìm thấy học sinh phù hợp</td></tr>';
                return;
            }

            tbody.innerHTML = '';
            students.forEach(student => {
                const row = document.createElement('tr');
                const isSelected = selectedStudents.has(student.citizenId);
                if (isSelected) {
                    row.classList.add('selected');
                }

                row.innerHTML = `
                    <td>
                        <input type="checkbox" 
                               value="${student.citizenId}" 
                               ${isSelected ? 'checked' : ''} 
                               onchange="toggleStudentSelection('${student.citizenId}', this)">
                    </td>
                    <td>${student.citizenId || '-'}</td>
                    <td>${student.fullName || '-'}</td>
                    <td>${student.phone || '-'}</td>
                `;

                // Click on row to toggle selection
                row.addEventListener('click', function(e) {
                    if (e.target.type !== 'checkbox') {
                        const checkbox = row.querySelector('input[type="checkbox"]');
                        checkbox.checked = !checkbox.checked;
                        toggleStudentSelection(student.citizenId, checkbox);
                    }
                });

                tbody.appendChild(row);
            });

            updateSelectAllCheckbox();
        })
        .catch(error => {
            console.error('Error loading available students:', error);
            tbody.innerHTML = '<tr><td colspan="4" class="empty-row">Có lỗi xảy ra khi tải dữ liệu</td></tr>';
        });
}

function toggleStudentSelection(citizenId, checkbox) {
    const row = checkbox.closest('tr');

    if (checkbox.checked) {
        selectedStudents.add(citizenId);
        row.classList.add('selected');
    } else {
        selectedStudents.delete(citizenId);
        row.classList.remove('selected');
    }

    updateSelectedCount();
    updateSelectAllCheckbox();
}

function toggleSelectAll() {
    const selectAllCheckbox = document.getElementById('selectAllStudents');
    const checkboxes = document.querySelectorAll('#availableStudentTableBody input[type="checkbox"]');

    checkboxes.forEach(checkbox => {
        checkbox.checked = selectAllCheckbox.checked;
        const citizenId = checkbox.value;
        const row = checkbox.closest('tr');

        if (selectAllCheckbox.checked) {
            selectedStudents.add(citizenId);
            row.classList.add('selected');
        } else {
            selectedStudents.delete(citizenId);
            row.classList.remove('selected');
        }
    });

    updateSelectedCount();
}

function updateSelectAllCheckbox() {
    const selectAllCheckbox = document.getElementById('selectAllStudents');
    const checkboxes = document.querySelectorAll('#availableStudentTableBody input[type="checkbox"]');

    if (checkboxes.length === 0) {
        selectAllCheckbox.checked = false;
        selectAllCheckbox.indeterminate = false;
        return;
    }

    const checkedCount = Array.from(checkboxes).filter(cb => cb.checked).length;

    if (checkedCount === 0) {
        selectAllCheckbox.checked = false;
        selectAllCheckbox.indeterminate = false;
    } else if (checkedCount === checkboxes.length) {
        selectAllCheckbox.checked = true;
        selectAllCheckbox.indeterminate = false;
    } else {
        selectAllCheckbox.checked = false;
        selectAllCheckbox.indeterminate = true;
    }
}

function updateSelectedCount() {
    document.getElementById('selectedCount').textContent = selectedStudents.size;
}

function debounceSearch() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        const searchValue = document.getElementById('studentSearchInput').value.trim();
        loadAvailableStudents(searchValue);
    }, 300); // 300ms debounce
}

function submitAddStudents() {
    const subjectId = document.getElementById('subjectSelect').value;
    const errorDiv = document.getElementById('addStudentError');
    const successDiv = document.getElementById('addStudentSuccess');

    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';

    if (selectedStudents.size === 0) {
        errorDiv.textContent = 'Vui lòng chọn ít nhất một học sinh!';
        errorDiv.style.display = 'block';
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
    };

    // Add CSRF token
    const csrfToken = getCsrfToken();
    const csrfHeader = getCsrfHeader();
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    const confirmBtn = document.getElementById('confirmAddStudents');
    confirmBtn.disabled = true;
    confirmBtn.textContent = 'Đang xử lý...';

    fetch('/classes/api/students/add', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({
            subjectId: subjectId,
            studentIds: Array.from(selectedStudents)
        })
    })
        .then(response => response.json())
        .then(data => {
            confirmBtn.disabled = false;
            confirmBtn.textContent = '➕ Thêm học sinh đã chọn';

            if (data.success) {
                successDiv.textContent = data.message;
                successDiv.style.display = 'block';

                // Reload and close popup
                setTimeout(() => {
                    closePopup('addStudentPopup');
                    loadStudents();
                }, 1500);
            } else {
                errorDiv.textContent = data.message;
                errorDiv.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            confirmBtn.disabled = false;
            confirmBtn.textContent = '➕ Thêm học sinh đã chọn';
            errorDiv.textContent = 'Có lỗi xảy ra, vui lòng thử lại!';
            errorDiv.style.display = 'block';
        });
}

function confirmRemoveStudent(citizenId, studentName) {
    document.getElementById('removeStudentCitizenId').value = citizenId;
    document.getElementById('removeStudentName').textContent = studentName;
    document.getElementById('removeStudentError').style.display = 'none';
    document.getElementById('removeStudentSuccess').style.display = 'none';
    openPopup('removeStudentPopup');
}

function openPaymentPopup(citizenId, studentName) {
    const yearId = document.getElementById('academicYearSelect').value;
    const classId = document.getElementById('classSelect').value;
    const subjectId = document.getElementById('subjectSelect').value;

    const yearText = document.getElementById('academicYearSelect').selectedOptions[0]?.text || '';
    const classText = document.getElementById('classSelect').selectedOptions[0]?.text || '';
    const subjectText = document.getElementById('subjectSelect').selectedOptions[0]?.text || '';

    // Redirect to revenues page with query parameters
    const params = new URLSearchParams({
        openPopup: 'true',
        citizenId: citizenId,
        studentName: studentName,
        yearId: yearId,
        yearText: yearText,
        classId: classId,
        classText: classText,
        subjectId: subjectId,
        subjectText: subjectText
    });

    window.location.href = `/transactions/revenues?${params.toString()}`;
}


function submitRemoveStudent() {
    const subjectId = document.getElementById('subjectSelect').value;
    const citizenId = document.getElementById('removeStudentCitizenId').value;

    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`/classes/api/subjects/${subjectId}/students`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify([citizenId])
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Xóa học sinh thất bại');
            }
            return response.json();
        })
        .then(data => {
            document.getElementById('removeStudentSuccess').textContent = 'Đã xóa học sinh khỏi môn học thành công!';
            document.getElementById('removeStudentSuccess').style.display = 'block';
            setTimeout(() => {
                closePopup('removeStudentPopup');
                loadStudents(); // Tải lại danh sách học sinh
            }, 1000);
        })
        .catch(error => {
            document.getElementById('removeStudentError').textContent = error.message;
            document.getElementById('removeStudentError').style.display = 'block';
        });
}

// Escape key handler
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closePopup('addClassPopup');
        closePopup('addSubjectPopup');
        closePopup('addStudentPopup');
    }
});