<script setup>
defineProps({ name: { type: String, required: true } });

const paths = {
  dashboard: '<rect x="3" y="3" width="7.5" height="7.5" rx="1.5"/><rect x="13.5" y="3" width="7.5" height="7.5" rx="1.5"/><rect x="3" y="13.5" width="7.5" height="7.5" rx="1.5"/><rect x="13.5" y="13.5" width="7.5" height="7.5" rx="1.5"/>',
  upload: '<path d="M12 15V4"/><path d="M8 8l4-4 4 4"/><path d="M4 15v3a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-3"/>',
  files: '<rect x="4" y="3" width="16" height="18" rx="2"/><path d="M4 9h16"/><path d="M4 15h16"/><path d="M10 9v12"/>',
  dataset: '<rect x="4" y="3" width="16" height="18" rx="2"/><path d="M8 16.5V13"/><path d="M12 16.5v-6"/><path d="M16 16.5v-3"/>',
  users: '<circle cx="9" cy="8" r="3.2"/><path d="M15 6.2a3 3 0 0 1 0 5.6"/><path d="M3.5 20a5.5 5.5 0 0 1 11 0"/><path d="M17 14.6a5.5 5.5 0 0 1 3.5 5.4"/>',
  settings: '<circle cx="16" cy="6" r="2"/><path d="M4 6h10"/><path d="M18 6h2"/><circle cx="8" cy="12" r="2"/><path d="M4 12h2"/><path d="M10 12h10"/><circle cx="16" cy="18" r="2"/><path d="M4 18h10"/><path d="M18 18h2"/>',
  search: '<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.35-4.35"/>',
  logout: '<path d="M14 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h8"/><path d="M18 15l3-3-3-3"/><path d="M10 12h11"/>',
  plus: '<path d="M12 5v14"/><path d="M5 12h14"/>',
  'trend-up': '<path d="M3 17l6-6 4 4 8-8"/><path d="M15 7h6v6"/>',
  'trend-down': '<path d="M3 7l6 6 4-4 8 8"/><path d="M15 17h6v-6"/>',
  check: '<path d="M5 12.5l4.5 4.5L19 7"/>',
  mail: '<rect x="3" y="5" width="18" height="14" rx="2"/><path d="M4 7l8 6 8-6"/>',
  'arrow-left': '<path d="M20 12H4"/><path d="M10 6l-6 6 6 6"/>',
  document: '<path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5"/>',
  menu: '<path d="M4 6h16"/><path d="M4 12h16"/><path d="M4 18h16"/>',
  briefcase: '<rect x="3" y="7" width="18" height="13" rx="2"/><path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><path d="M3 12.5h18"/>',
  user: '<circle cx="12" cy="8" r="3.5"/><path d="M5 20a7 7 0 0 1 14 0"/>',
  layers: '<path d="M12 3l9 5-9 5-9-5z"/><path d="M3 13l9 5 9-5"/>',
  bulb: '<path d="M9 18h6"/><path d="M10 21h4"/><path d="M12 3a6 6 0 0 0-3.5 10.9c.6.4 1 1.1 1 1.9V16h5v-.2c0-.8.4-1.5 1-1.9A6 6 0 0 0 12 3z"/>',
  activity: '<path d="M3 12h4l3-8 4 16 3-8h4"/>',
  edit: '<path d="M4 20h4L19 9l-4-4L4 16z"/><path d="M13.5 6.5l4 4"/>',
  trash: '<path d="M4 7h16"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M6 7l1 13h10l1-13"/><path d="M9 7V4h6v3"/>',
  refresh: '<path d="M20 11a8 8 0 0 0-14.9-3"/><path d="M4 4v4h4"/><path d="M4 13a8 8 0 0 0 14.9 3"/><path d="M20 20v-4h-4"/>',
  close: '<path d="M6 6l12 12"/><path d="M18 6L6 18"/>',
};

function svg(name) {
  return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" style="width:100%;height:100%;display:block">${paths[name] || ''}</svg>`;
}
</script>

<template>
  <span class="inline-block" aria-hidden="true" v-html="svg(name)"></span>
</template>
