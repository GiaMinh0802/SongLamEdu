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

function openCreateRevenuePopup() {
    document.getElementById('createRevenuePopup').classList.add('active');
    document.body.style.overflow = 'hidden';
}

function closeCreateRevenuePopup() {
    document.getElementById('createRevenuePopup').classList.remove('active');
    document.body.style.overflow = '';
}

function clearSearchFilters() {
    document.getElementById('codeInput').value = '';
    document.getElementById('studentInput').value = '';
    document.getElementById('cashierInput').value = '';
    document.getElementById('fromInput').value = '';
    document.getElementById('toInput').value = '';
}

document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeCreateRevenuePopup();
    }
});

window.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('id')) {
        openCreateRevenuePopup();
    }
});
