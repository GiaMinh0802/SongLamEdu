document.addEventListener('DOMContentLoaded', function () {
    const amountView   = document.getElementById('amountView');
    const amountInput  = document.getElementById('amount');
    const amountHidden = document.getElementById('amountHidden');

    function formatNumber(val) {
        if (!val) return '';
        return Number(val.replace(/\D/g, '')).toLocaleString('vi-VN');
    }

    function getRawNumber(val) {
        return val ? val.replace(/\D/g, '') : '';
    }

    if (amountView && amountInput) {
        const rawValue = amountInput.value;

        if (rawValue) {
            const formatted = formatNumber(rawValue);

            amountView.textContent = formatted;

            amountInput.value = formatted;

            amountHidden.value = rawValue;
        }
    }

    amountInput?.addEventListener('input', function () {
        const raw = getRawNumber(this.value);

        this.value = formatNumber(raw);
        amountHidden.value = raw;
    });
});
