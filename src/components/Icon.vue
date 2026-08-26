<script setup>
// Set de ícones SVG inline (traço, herda currentColor). Sem dependências externas.
// Uso: <Icon name="upload" class="h-5 w-5" />
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
};

function svg(name) {
  return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" style="width:100%;height:100%;display:block">${paths[name] || ''}</svg>`;
}
</script>

<template>
  <span class="inline-block" aria-hidden="true" v-html="svg(name)"></span>
</template>
