function clearSearchFilters() {
    document.getElementById('citizenIdInput').value = '';
    document.getElementById('fullNameInput').value = '';
    document.getElementById('phoneInput').value = '';
}

function changePageSize(size) {
    const url = new URL(window.location.href);
    url.searchParams.set('size', size);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}
