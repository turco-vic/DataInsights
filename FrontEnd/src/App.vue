<script setup>
import Notificacoes from './components/ui/Notificacoes.vue';
import DialogoConfirmacao from './components/ui/DialogoConfirmacao.vue';
</script>

<template>
  <router-view />
  <Notificacoes />
  <DialogoConfirmacao />
</template>
