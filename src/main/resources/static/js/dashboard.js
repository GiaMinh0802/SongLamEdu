let currentTab = 'month';

function switchDashboardTab(tab) {
    currentTab = tab;

    document.querySelectorAll('.dashboard-tab').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tab}"]`).classList.add('active');

    document.getElementById('monthFilter').classList.add('hidden');
    document.getElementById('quarterFilter').classList.add('hidden');
    document.getElementById('yearFilter').classList.add('hidden');

    document.getElementById(tab + 'Filter').classList.remove('hidden');

    loadStats();
}

function loadStats() {
    let url = '/api/dashboard/stats?type=' + currentTab;

    if (currentTab === 'month') {
        const month = document.getElementById('monthSelect').value;
        const year = document.getElementById('monthYearSelect').value;
        url += `&month=${month}&year=${year}`;
    } else if (currentTab === 'quarter') {
        const quarter = document.getElementById('quarterSelect').value;
        const year = document.getElementById('quarterYearSelect').value;
        url += `&quarter=${quarter}&year=${year}`;
    } else {
        const year = document.getElementById('yearSelect').value;
        url += `&year=${year}`;
    }

    fetch(url)
        .then(response => response.json())
        .then(data => {
            updateStats(data);
        })
        .catch(error => {
            console.error('Error loading stats:', error);
        });
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
}

function updateStats(data) {
    document.getElementById('totalRevenue').textContent = formatCurrency(data.totalRevenue);
    document.getElementById('totalExpense').textContent = formatCurrency(data.totalExpense);
    document.getElementById('revenueCount').textContent = data.revenueCount + ' giao dịch';
    document.getElementById('expenseCount').textContent = data.expenseCount + ' giao dịch';

    const balanceEl = document.getElementById('balance');
    balanceEl.textContent = formatCurrency(data.balance);
    if (data.balance < 0) {
        balanceEl.classList.add('negative');
        balanceEl.classList.remove('positive');
    } else {
        balanceEl.classList.add('positive');
        balanceEl.classList.remove('negative');
    }

    updateBarChart('subjectChart', data.bySubject, data.totalRevenue);
    updateBarChart('classChart', data.byClass, data.totalRevenue);

    document.getElementById('timelineChartTitle').textContent = '📈 ' + data.timelineTitle;
    updateTimelineChart(data.timelineData);
}

function updateBarChart(containerId, items, total) {
    const container = document.getElementById(containerId);

    if (!items || items.length === 0) {
        container.innerHTML = '<div class="empty-chart">Không có dữ liệu</div>';
        return;
    }

    let html = '<div class="bar-chart">';
    items.forEach(item => {
        const percentage = total > 0 ? (item.amount / total * 100).toFixed(1) : 0;
        html += `
            <div class="bar-item">
                <div class="bar-label" title="${item.name}">${item.name}</div>
                <div class="bar-wrapper">
                    <div class="bar-fill" style="width: ${percentage}%"></div>
                    <span class="bar-percentage">${percentage}%</span>
                </div>
                <div class="bar-value">${formatCurrency(item.amount)}</div>
            </div>
        `;
    });
    html += '</div>';
    container.innerHTML = html;
}

function updateTimelineChart(timelineData) {
    const container = document.getElementById('timelineChart');

    if (!timelineData || timelineData.length === 0) {
        container.innerHTML = '<div class="empty-chart">Không có dữ liệu</div>';
        return;
    }

    let maxValue = 0;
    timelineData.forEach(item => {
        if (item.revenue > maxValue) maxValue = item.revenue;
        if (item.expense > maxValue) maxValue = item.expense;
    });

    if (maxValue === 0) maxValue = 1;

    let html = '<div class="timeline-chart">';
    html += '<div class="timeline-bars">';

    timelineData.forEach(item => {
        const revenueHeight = (item.revenue / maxValue * 160);
        const expenseHeight = (item.expense / maxValue * 160);
        html += `
            <div class="timeline-group">
                <div class="timeline-bar-pair">
                    <div class="timeline-bar revenue-bar" style="height: ${revenueHeight}px" title="Thu: ${formatCurrency(item.revenue)}"></div>
                    <div class="timeline-bar expense-bar" style="height: ${expenseHeight}px" title="Chi: ${formatCurrency(item.expense)}"></div>
                </div>
                <div class="timeline-label">${item.label}</div>
            </div>
        `;
    });

    html += '</div></div>';
    container.innerHTML = html;
}

function exportReport() {
    let url = '/api/dashboard/export?type=' + currentTab;

    if (currentTab === 'month') {
        const month = document.getElementById('monthSelect').value;
        const year = document.getElementById('monthYearSelect').value;
        url += `&month=${month}&year=${year}`;
    } else if (currentTab === 'quarter') {
        const quarter = document.getElementById('quarterSelect').value;
        const year = document.getElementById('quarterYearSelect').value;
        url += `&quarter=${quarter}&year=${year}`;
    } else {
        const year = document.getElementById('yearSelect').value;
        url += `&year=${year}`;
    }

    window.location.href = url;
}

document.addEventListener('DOMContentLoaded', function() {
    const initMonth = document.getElementById('initMonth').value;
    const initQuarter = document.getElementById('initQuarter').value;

    document.getElementById('monthSelect').value = initMonth;
    document.getElementById('quarterSelect').value = initQuarter;

    loadStats();
});