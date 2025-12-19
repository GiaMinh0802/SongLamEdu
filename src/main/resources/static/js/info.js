let infoDatepickers = [];
let infoDatepickerInitialized = false;

function toggleEditMode() {
  const container = document.getElementById('info-container');
  if (!container) return;

  const wasEditing = container.classList.contains('editing');
  container.classList.toggle('editing');
  const isEditing = container.classList.contains('editing');

  // Toggle view / edit
  document.querySelectorAll('[data-view]')
      .forEach(el => el.style.display = isEditing ? 'none' : 'block');

  document.querySelectorAll('[data-edit]')
      .forEach(el => el.classList.toggle('hidden', !isEditing));

  // Toggle buttons
  const updateBtn = document.getElementById('btn-update');
  const saveGroup = document.getElementById('save-group');
  if (updateBtn) updateBtn.classList.toggle('hidden', isEditing);
  if (saveGroup) saveGroup.classList.toggle('hidden', !isEditing);

  /* ================================
     ENTER EDIT MODE
  ================================= */
  if (!wasEditing && isEditing) {
    infoDatepickers = [];
    document.querySelectorAll('.datepicker-edit').forEach(input => {
      infoDatepickers.push(new DatePicker(input));
    });
  }

  /* ================================
     EXIT EDIT MODE (HỦY)
  ================================= */
  if (wasEditing && !isEditing) {
    infoDatepickers.forEach(dp => dp.destroy());
    infoDatepickers = [];
  }
}
