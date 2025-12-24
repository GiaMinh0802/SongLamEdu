// Basic app interactions
document.addEventListener('DOMContentLoaded', function () {
  // Placeholder: could add sidebar toggle for mobile later
});

let infoDatepickers = [];
let infoDatepickerInitialized = false;

function toggleEditMode() {
  const container = document.getElementById('info-container');
  if (!container) return;

  const wasEditing = container.classList.contains('editing');
  container.classList.toggle('editing');
  const isEditing = container.classList.contains('editing');

  // Toggle view / edit
  container.querySelectorAll('[data-view]')
      .forEach(el => el.style.display = isEditing ? 'none' : 'block');

  container.querySelectorAll('[data-edit]')
      .forEach(el => el.classList.toggle('hidden', !isEditing));

  // Toggle buttons
  const updateBtn = document.getElementById('btn-update');
  const saveGroup = document.getElementById('save-group');
  if (updateBtn) updateBtn.classList.toggle('hidden', isEditing);
  if (saveGroup) saveGroup.classList.toggle('hidden', !isEditing);

  // Enter edit mode: init datepickers (scope to container)
  if (!wasEditing && isEditing) {
    infoDatepickers = [];
    container.querySelectorAll('.datepicker-edit').forEach(input => {
      infoDatepickers.push(new DatePicker(input));
    });
    infoDatepickerInitialized = true;
  }

  // Exit edit mode: destroy datepickers
  if (wasEditing && !isEditing) {
    infoDatepickers.forEach(dp => dp.destroy());
    infoDatepickers = [];
    infoDatepickerInitialized = false;

    removeAllValidationFeedback(container);
  }
}
