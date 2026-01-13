function formatCurrency(input) {
    let value = input.value.replace(/\D/g, '');

    if (value) {
        value = value.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
        input.value = value;

        document.getElementById('amountHidden').value = value.replace(/\./g, '');
    } else {
        input.value = '';
        document.getElementById('amountHidden').value = '';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const amountInput = document.getElementById('amountInput');
    if (amountInput) {
        amountInput.addEventListener('input', function() {
            formatCurrency(this);
        });

        amountInput.addEventListener('keypress', function(e) {
            if (e.which < 48 || e.which > 57) {
                e.preventDefault();
            }
        });
    }
});

document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeCreateRevenuePopup();
    }
});

window.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);

    if (urlParams.get('openPopup') === 'true') {
        // Fill form fields from URL params
        document.getElementById('yearIdInput').value = urlParams.get('yearId') || '';
        document.getElementById('yearTextInput').value = urlParams.get('yearText') || '';
        document.getElementById('classIdInput').value = urlParams.get('classId') || '';
        document.getElementById('classTextInput').value = urlParams.get('classText') || '';
        document.getElementById('subjectIdInput').value = urlParams.get('subjectId') || '';
        document.getElementById('subjectTextInput').value = urlParams.get('subjectText') || '';
        document.getElementById('studentIdInput').value = urlParams.get('citizenId') || '';
        document.getElementById('studentNameInput').value = urlParams.get('studentName') || '';

        // Open popup
        openCreateRevenuePopup();

        // Clean URL without reloading
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});

// Revenue Management JavaScript

let studentSearchTimeout = null;
let currentSuggestionIndex = -1;

// Initialize when page loads
document.addEventListener('DOMContentLoaded', function() {
    loadBranches('branchSearchSelect');
    checkUrlParamsAndOpenPopup();
    setupFormSubmitHandler();
    setupKeyboardNavigation();
});

// Check URL params and open popup if needed
function checkUrlParamsAndOpenPopup() {
    const urlParams = new URLSearchParams(window.location.search);

    if (urlParams.get('openPopup') === 'true') {
        openCreateRevenuePopup();

        // After popup opens, load dropdowns and set values from URL
        loadRevenueAcademicYears().then(() => {
            const yearId = urlParams.get('yearId');
            if (yearId) {
                document.getElementById('revenueYearSelect').value = yearId;
                return loadRevenueClasses();
            }
        }).then(() => {
            const classId = urlParams.get('classId');
            if (classId) {
                document.getElementById('revenueClassSelect').value = classId;
                return loadRevenueSubjects();
            }
        }).then(() => {
            const subjectId = urlParams.get('subjectId');
            if (subjectId) {
                document.getElementById('revenueSubjectSelect').value = subjectId;
            }

            // Fill student info
            document.getElementById('studentIdInput').value = urlParams.get('citizenId') || '';
            document.getElementById('studentNameInput').value = urlParams.get('studentName') || '';
            document.getElementById('studentSearchInput').value = urlParams.get('studentName') || '';
        });

        // Clean URL without reloading
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}

// ========== Dropdown Load Functions (Reuse from classes.js pattern) ==========

function loadRevenueAcademicYears() {
    return fetch('/classes/api/academic-years')
        .then(response => response.json())
        .then(data => {
            const select = document.getElementById('revenueYearSelect');
            select.innerHTML = ''; // No default option - auto select current year

            // Determine current academic year (starts Sep 1)
            const now = new Date();
            const currentMonth = now.getMonth() + 1; // 1-12
            const currentCalendarYear = now.getFullYear();
            // Academic year: if month >= 9, it's currentYear-nextYear, else previousYear-currentYear
            const academicStartYear = currentMonth >= 9 ? currentCalendarYear : currentCalendarYear - 1;

            let selectedYearId = null;

            data.forEach(year => {
                const option = document.createElement('option');
                option.value = year.id;
                option.textContent = year.name || year.yearName;
                select.appendChild(option);

                // Try to match current academic year (e.g., "2024-2025" or contains "2024")
                const yearName = year.name || year.yearName || '';
                if (yearName.includes(academicStartYear.toString())) {
                    selectedYearId = year.id;
                }
            });

            // Auto select current year or first option
            if (selectedYearId) {
                select.value = selectedYearId;
            } else if (data.length > 0) {
                select.value = data[0].id;
            }

            // Trigger load classes
            loadRevenueClasses();
        })
        .catch(error => {
            console.error('Error loading academic years:', error);
        });
}

function loadRevenueClasses() {
    const yearId = document.getElementById('revenueYearSelect').value;
    const classSelect = document.getElementById('revenueClassSelect');
    const subjectSelect = document.getElementById('revenueSubjectSelect');

    classSelect.innerHTML = '<option value="">-- Chọn lớp học --</option>';
    subjectSelect.innerHTML = '<option value="">-- Chọn môn học --</option>';

    if (!yearId) return Promise.resolve();

    return fetch(`/classes/api/classes?academicYearId=${yearId}`)
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

function loadRevenueSubjects() {
    const classId = document.getElementById('revenueClassSelect').value;
    const subjectSelect = document.getElementById('revenueSubjectSelect');

    subjectSelect.innerHTML = '<option value="">-- Chọn môn học --</option>';

    if (!classId) return Promise.resolve();

    return fetch(`/classes/api/subjects?classId=${classId}`)
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

// ========== Student Autocomplete Functions ==========

function debounceStudentSearch() {
    clearTimeout(studentSearchTimeout);
    studentSearchTimeout = setTimeout(() => {
        searchStudents();
    }, 300);
}

function searchStudents() {
    const searchValue = document.getElementById('studentSearchInput').value.trim();
    const suggestionsDiv = document.getElementById('studentSuggestions');

    // Get selected filter values
    const yearId = document.getElementById('revenueYearSelect').value;
    const classId = document.getElementById('revenueClassSelect').value;
    const subjectId = document.getElementById('revenueSubjectSelect').value;

    // If no keyword and no filters, hide suggestions
    if (searchValue.length < 2 && !yearId && !classId && !subjectId) {
        suggestionsDiv.classList.remove('active');
        suggestionsDiv.innerHTML = '';
        return;
    }

    // Build query params
    const params = new URLSearchParams({
        size: 50
    });

    // Only add keyword if it has value
    if (searchValue.length >= 1) {
        params.append('keyword', searchValue);
    }

    if (yearId) params.append('yearId', yearId);
    if (classId) params.append('classId', classId);
    if (subjectId) params.append('subjectId', subjectId);

    fetch(`/students/api/search?${params.toString()}`)
        .then(response => response.json())
        .then(data => {
            const students = data.content || data || [];

            if (students.length === 0) {
                suggestionsDiv.innerHTML = '<div class="suggestion-empty">Không tìm thấy học sinh</div>';
                suggestionsDiv.classList.add('active');
                return;
            }

            suggestionsDiv.innerHTML = '';
            students.forEach((student, index) => {
                const item = document.createElement('div');
                item.className = 'suggestion-item';
                item.dataset.index = index;
                item.innerHTML = `
                    <span class="suggestion-cccd">${student.citizenId}</span>
                    <span class="suggestion-name">- ${student.fullName || student.person?.fullName || ''}</span>
                `;
                item.onclick = () => selectStudent(student);
                suggestionsDiv.appendChild(item);
            });

            currentSuggestionIndex = -1;
            suggestionsDiv.classList.add('active');
        })
        .catch(error => {
            console.error('Error searching students:', error);
            suggestionsDiv.innerHTML = '<div class="suggestion-empty">Có lỗi xảy ra</div>';
            suggestionsDiv.classList.add('active');
        });
}

function onFilterChange() {
    // Trigger search when year/class/subject changes
    searchStudents();
}

function selectStudent(student) {
    const fullName = student.fullName || student.person?.fullName || '';

    document.getElementById('studentIdInput').value = student.citizenId;
    document.getElementById('studentNameInput').value = fullName;
    document.getElementById('studentSearchInput').value = fullName;

    // Hide suggestions
    document.getElementById('studentSuggestions').classList.remove('active');
}

function setupKeyboardNavigation() {
    const searchInput = document.getElementById('studentSearchInput');

    if (!searchInput) return;

    searchInput.addEventListener('keydown', function(e) {
        const suggestionsDiv = document.getElementById('studentSuggestions');
        const items = suggestionsDiv.querySelectorAll('.suggestion-item');

        if (!suggestionsDiv.classList.contains('active') || items.length === 0) {
            return;
        }

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                currentSuggestionIndex = Math.min(currentSuggestionIndex + 1, items.length - 1);
                updateSuggestionSelection(items);
                break;

            case 'ArrowUp':
                e.preventDefault();
                currentSuggestionIndex = Math.max(currentSuggestionIndex - 1, 0);
                updateSuggestionSelection(items);
                break;

            case 'Enter':
                e.preventDefault();
                if (currentSuggestionIndex >= 0 && items[currentSuggestionIndex]) {
                    items[currentSuggestionIndex].click();
                }
                break;

            case 'Escape':
                suggestionsDiv.classList.remove('active');
                currentSuggestionIndex = -1;
                break;
        }
    });

    // Close suggestions when clicking outside
    document.addEventListener('click', function(e) {
        const container = document.querySelector('.autocomplete-container');
        const suggestionsDiv = document.getElementById('studentSuggestions');

        if (container && !container.contains(e.target)) {
            suggestionsDiv.classList.remove('active');
        }
    });
}

function updateSuggestionSelection(items) {
    items.forEach((item, index) => {
        if (index === currentSuggestionIndex) {
            item.classList.add('selected');
            item.scrollIntoView({ block: 'nearest' });
        } else {
            item.classList.remove('selected');
        }
    });
}

// ========== Popup Functions ==========

function openCreateRevenuePopup() {
    loadRevenueAcademicYears();
    loadBranches('branchSelect');
    document.getElementById('createRevenuePopup').classList.add('active');
}

function closeCreateRevenuePopup() {
    document.getElementById('createRevenuePopup').classList.remove('active');
    clearRevenueForm();
}

function clearRevenueForm() {
    document.getElementById('revenueYearSelect').innerHTML = '<option value="">-- Chọn năm học --</option>';
    document.getElementById('revenueClassSelect').innerHTML = '<option value="">-- Chọn lớp học --</option>';
    document.getElementById('revenueSubjectSelect').innerHTML = '<option value="">-- Chọn môn học --</option>';
    document.getElementById('branchSelect').innerHTML = '<option value="">-- Chọn cơ sở --</option>';
    document.getElementById('studentSearchInput').value = '';
    document.getElementById('studentIdInput').value = '';
    document.getElementById('studentNameInput').value = '';
    document.getElementById('amountInput').value = '';
    document.getElementById('amountHidden').value = '';
    document.getElementById('reasonInput').value = '';
    document.getElementById('studentSuggestions').classList.remove('active');
    document.getElementById('createRevenueError').style.display = 'none';
}

function closePopupOnOverlay(event, popupId) {
    if (event.target.id === popupId) {
        document.getElementById(popupId).classList.remove('active');
        if (popupId === 'createRevenuePopup') {
            clearRevenueForm();
        }
    }
}

// ========== Form Handling ==========

function formatAmountInput(input) {
    // Remove non-digits
    let value = input.value.replace(/\D/g, '');

    // Format with dots
    if (value) {
        value = parseInt(value).toLocaleString('vi-VN');
    }

    input.value = value;
    document.getElementById('amountHidden').value = value.replace(/\./g, '');
}

function setupFormSubmitHandler() {
    const form = document.getElementById('createRevenueForm');
    if (form) {
        form.addEventListener('submit', function() {
            const amountInput = document.getElementById('amountInput');
            const amountHidden = document.getElementById('amountHidden');
            amountHidden.value = amountInput.value.replace(/\./g, '');
        });
    }
}

function clearSearchFilters() {
    document.getElementById('branchSearchSelect').value = '';
    document.getElementById('codeInput').value = '';
    document.getElementById('studentInput').value = '';
    document.getElementById('cashierInput').value = '';
    document.getElementById('fromInput').value = '';
    document.getElementById('toInput').value = '';
}

function changePageSize(size) {
    const url = new URL(window.location.href);
    url.searchParams.set('size', size);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}

function loadBranches(selectId) {
    fetch('/transactions/api/branches')
        .then(response => response.json())
        .then(data => {
            const branchSelect = document.getElementById(selectId);
            const selectedBranchId = document.getElementById('selectedBranchId')?.value;

            data.forEach(branch => {
                const option = document.createElement('option');
                option.value = branch.id;
                option.textContent = branch.name;
                if (selectId === 'branchSearchSelect' && branch.id.toString() === selectedBranchId) {
                    option.selected = true;
                }
                branchSelect.appendChild(option);
            });
        })
        .catch(error => console.error('Error loading branches:', error));
}

// Escape key handler
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeCreateRevenuePopup();
    }
});
