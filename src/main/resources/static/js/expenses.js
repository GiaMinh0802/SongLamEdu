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

// Initialize when page loads
document.addEventListener('DOMContentLoaded', function() {
    setupFormSubmitHandler();
});

// ========== Popup Functions ==========

function openCreateExpensePopup() {
    document.getElementById('createExpensePopup').classList.add('active');
}

function closeCreateExpensePopup() {
    document.getElementById('createExpensePopup').classList.remove('active');
    clearExpenseForm();
}

function clearExpenseForm() {
    document.getElementById('receiverNameInput').value = '';
    document.getElementById('receiverAddressInput').value = '';
    document.getElementById('amountInput').value = '';
    document.getElementById('amountHidden').value = '';
    document.getElementById('reasonInput').value = '';
    document.getElementById('createExpenseError').style.display = 'none';
}

function closePopupOnOverlay(event, popupId) {
    if (event.target.id === popupId) {
        document.getElementById(popupId).classList.remove('active');
        if (popupId === 'createExpensePopup') {
            clearExpenseForm();
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
    const form = document.getElementById('createExpenseForm');
    if (form) {
        form.addEventListener('submit', function() {
            const amountInput = document.getElementById('amountInput');
            const amountHidden = document.getElementById('amountHidden');
            amountHidden.value = amountInput.value.replace(/\./g, '');
        });
    }
}

function clearSearchFilters() {
    document.getElementById('codeInput').value = '';
    document.getElementById('receiverInput').value = '';
    document.getElementById('cashierInput').value = '';
    document.getElementById('fromInput').value = '';
    document.getElementById('toInput').value = '';
    document.getElementById('searchForm').submit();
}

function changePageSize(size) {
    const url = new URL(window.location.href);
    url.searchParams.set('size', size);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}

// Escape key handler
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeCreateExpensePopup();
    }
});
