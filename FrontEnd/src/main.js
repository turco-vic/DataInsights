import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import { useAuthStore } from './stores/authStore';
import './style.css';

// O histórico de planilhas agora fica no servidor: remove as cópias antigas guardadas no navegador.
Object.keys(localStorage)
  .filter((chave) => chave.startsWith('di_planilha'))
  .forEach((chave) => localStorage.removeItem(chave));

const app = createApp(App).use(createPinia()).use(router);

const auth = useAuthStore();
if (auth.logado) {
  auth.agendarRenovacao();
  auth.carregarPerfil();
}

app.mount('#app');
