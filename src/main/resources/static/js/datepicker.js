/**
 * Custom Datepicker with dd/MM/yyyy format
 * Usage: new DatePicker(inputElement, options)
 */

class DatePicker {
    constructor(input, options = {}) {
        this.input = input;
        this.options = {
            format: 'dd/MM/yyyy',
            minDate: null,
            maxDate: null,
            defaultDate: null,
            onSelect: null,
            ...options
        };

        this.selectedDate = null;
        this.currentMonth = new Date().getMonth();
        this.currentYear = new Date().getFullYear();

        this.init();
    }

    init() {
        // Wrap input in container
        this.wrapper = document.createElement('div');
        this.wrapper.className = 'datepicker-wrapper';
        this.input.parentNode.insertBefore(this.wrapper, this.input);
        this.wrapper.appendChild(this.input);

        // Add icon
        const icon = document.createElement('span');
        icon.className = 'datepicker-icon';
        icon.innerHTML = '📅';
        this.wrapper.appendChild(icon);

        // Allow manual input
        this.input.placeholder = 'dd/mm/yyyy';
        this.input.setAttribute('maxlength', '10');

        // Create calendar
        this.createCalendar();

        // Event listeners
        this.input.addEventListener('click', () => this.toggle());
        icon.addEventListener('click', () => this.toggle());
        document.addEventListener('click', (e) => this.handleClickOutside(e));

        // Handle manual input
        this.input.addEventListener('input', (e) => this.handleManualInput(e));
        this.input.addEventListener('blur', (e) => this.handleManualInputBlur(e));

        // Set default value if provided
        if (this.input.value) {
            const date = this.parseDate(this.input.value);
            if (date) {
                this.selectDate(date);
            }
        }
    }

    createCalendar() {
        this.calendar = document.createElement('div');
        this.calendar.className = 'datepicker-calendar';
        this.wrapper.appendChild(this.calendar);

        // Prevent calendar from closing when clicking inside it
        this.calendar.addEventListener('click', (e) => {
            e.stopPropagation();
        });

        this.renderCalendar();
    }

    renderCalendar() {
        const monthNames = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
                           'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
        const weekdays = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];

        this.calendar.innerHTML = `
            <div class="datepicker-header">
                <button type="button" class="datepicker-prev-month">‹</button>
                <div class="datepicker-title">${monthNames[this.currentMonth]} ${this.currentYear}</div>
                <button type="button" class="datepicker-next-month">›</button>
            </div>
            <div class="datepicker-selectors">
                <select class="datepicker-month-select">
                    ${monthNames.map((name, i) =>
                        `<option value="${i}" ${i === this.currentMonth ? 'selected' : ''}>${name}</option>`
                    ).join('')}
                </select>
                <select class="datepicker-year-select">
                    ${this.generateYearOptions()}
                </select>
            </div>
            <div class="datepicker-weekdays">
                ${weekdays.map(day => `<div class="datepicker-weekday">${day}</div>`).join('')}
            </div>
            <div class="datepicker-days"></div>
            <div class="datepicker-footer">
                <button type="button" class="datepicker-btn-today">Hôm nay</button>
                <button type="button" class="datepicker-btn-clear">Xóa</button>
            </div>
        `;

        this.renderDays();
        this.attachCalendarEvents();
    }

    generateYearOptions() {
        const currentYear = new Date().getFullYear();
        const startYear = currentYear - 100;
        const endYear = currentYear + 10;
        let options = '';

        for (let year = endYear; year >= startYear; year--) {
            options += `<option value="${year}" ${year === this.currentYear ? 'selected' : ''}>${year}</option>`;
        }

        return options;
    }

    renderDays() {
        const daysContainer = this.calendar.querySelector('.datepicker-days');
        daysContainer.innerHTML = '';

        const firstDay = new Date(this.currentYear, this.currentMonth, 1).getDay();
        const daysInMonth = new Date(this.currentYear, this.currentMonth + 1, 0).getDate();
        const daysInPrevMonth = new Date(this.currentYear, this.currentMonth, 0).getDate();

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        // Previous month days
        for (let i = firstDay - 1; i >= 0; i--) {
            const day = daysInPrevMonth - i;
            const button = this.createDayButton(day, 'other-month');
            daysContainer.appendChild(button);
        }

        // Current month days
        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(this.currentYear, this.currentMonth, day);
            const button = this.createDayButton(day);

            if (this.isDateDisabled(date)) {
                button.classList.add('disabled');
            }

            if (date.getTime() === today.getTime()) {
                button.classList.add('today');
            }

            if (this.selectedDate && date.getTime() === this.selectedDate.getTime()) {
                button.classList.add('selected');
            }

            daysContainer.appendChild(button);
        }

        // Next month days
        const totalCells = daysContainer.children.length;
        const remainingCells = 42 - totalCells; // 6 rows * 7 days
        for (let day = 1; day <= remainingCells; day++) {
            const button = this.createDayButton(day, 'other-month');
            daysContainer.appendChild(button);
        }
    }

    createDayButton(day, className = '') {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = `datepicker-day ${className}`;
        button.textContent = day;

        if (!className.includes('other-month')) {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                const date = new Date(this.currentYear, this.currentMonth, day);
                if (!this.isDateDisabled(date)) {
                    this.selectDate(date);
                    this.hide();
                }
            });
        }

        return button;
    }

    attachCalendarEvents() {
        const prevBtn = this.calendar.querySelector('.datepicker-prev-month');
        const nextBtn = this.calendar.querySelector('.datepicker-next-month');
        const monthSelect = this.calendar.querySelector('.datepicker-month-select');
        const yearSelect = this.calendar.querySelector('.datepicker-year-select');
        const todayBtn = this.calendar.querySelector('.datepicker-btn-today');
        const clearBtn = this.calendar.querySelector('.datepicker-btn-clear');

        prevBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.prevMonth();
        });

        nextBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.nextMonth();
        });

        monthSelect.addEventListener('change', (e) => {
            this.currentMonth = parseInt(e.target.value);
            this.renderDays();
        });

        yearSelect.addEventListener('change', (e) => {
            this.currentYear = parseInt(e.target.value);
            this.renderDays();
        });

        todayBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            const today = new Date();
            this.selectDate(today);
            this.hide();
        });

        clearBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.clear();
            this.hide();
        });
    }

    prevMonth() {
        if (this.currentMonth === 0) {
            this.currentMonth = 11;
            this.currentYear--;
        } else {
            this.currentMonth--;
        }
        this.renderCalendar();
    }

    nextMonth() {
        if (this.currentMonth === 11) {
            this.currentMonth = 0;
            this.currentYear++;
        } else {
            this.currentMonth++;
        }
        this.renderCalendar();
    }

    selectDate(date) {
        this.selectedDate = new Date(date);
        this.selectedDate.setHours(0, 0, 0, 0);
        this.input.value = this.formatDate(this.selectedDate);
        this.renderDays();

        if (this.options.onSelect) {
            this.options.onSelect(this.selectedDate);
        }

        // Trigger change event
        this.input.dispatchEvent(new Event('change', { bubbles: true }));
    }

    formatDate(date) {
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    }

    parseDate(dateStr) {
        // Parse dd/MM/yyyy format
        const parts = dateStr.split('/');
        if (parts.length === 3) {
            const day = parseInt(parts[0], 10);
            const month = parseInt(parts[1], 10) - 1;
            const year = parseInt(parts[2], 10);
            const date = new Date(year, month, day);
            if (!isNaN(date.getTime())) {
                return date;
            }
        }
        return null;
    }

    isDateDisabled(date) {
        if (this.options.minDate && date < this.options.minDate) {
            return true;
        }
        if (this.options.maxDate && date > this.options.maxDate) {
            return true;
        }
        return false;
    }

    toggle() {
        if (this.calendar.classList.contains('active')) {
            this.hide();
        } else {
            this.show();
        }
    }

    show() {
        // Set current month/year to selected date or today
        if (this.selectedDate) {
            this.currentMonth = this.selectedDate.getMonth();
            this.currentYear = this.selectedDate.getFullYear();
        } else {
            const today = new Date();
            this.currentMonth = today.getMonth();
            this.currentYear = today.getFullYear();
        }

        this.renderCalendar();
        this.calendar.classList.add('active');
    }

    hide() {
        this.calendar.classList.remove('active');
    }

    clear() {
        this.selectedDate = null;
        this.input.value = '';
        this.renderDays();

        if (this.options.onSelect) {
            this.options.onSelect(null);
        }

        this.input.dispatchEvent(new Event('change', { bubbles: true }));
    }

    handleClickOutside(e) {
        if (!this.wrapper.contains(e.target)) {
            this.hide();
        }
    }

    handleManualInput(e) {
        let value = e.target.value;

        // Remove non-numeric characters except /
        value = value.replace(/[^\d/]/g, '');

        // Auto-add slashes
        if (value.length === 2 && !value.includes('/')) {
            value += '/';
        } else if (value.length === 5 && value.split('/').length === 2) {
            value += '/';
        }

        // Update input value
        e.target.value = value;
    }

    handleManualInputBlur(e) {
        const value = e.target.value.trim();

        if (value === '') {
            this.selectedDate = null;
            return;
        }

        // Try to parse the date
        const date = this.parseDate(value);

        if (date && !this.isDateDisabled(date)) {
            // Valid date
            this.selectDate(date);
        } else if (value.length > 0) {
            // Invalid date - clear if user wants strict validation
            // Or keep the value and let form validation handle it
            // For now, we'll keep the value and let register.js validate
        }
    }

    getValue() {
        return this.selectedDate;
    }

    setValue(date) {
        if (date) {
            this.selectDate(date);
        } else {
            this.clear();
        }
    }

    destroy() {
        this.calendar.remove();
        this.wrapper.replaceWith(this.input);
    }
}

// Auto-initialize datepickers with class 'datepicker'
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.datepicker').forEach(input => {
        new DatePicker(input);
    });
});
