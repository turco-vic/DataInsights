import { createRouter, createWebHistory } from 'vue-router';
import LandingPage from '../views/LandingPage.vue';
import AppShell from '../layouts/AppShell.vue';
import { useAuthStore } from '../stores/authStore';

// As telas são carregadas sob demanda: cada uma vira um arquivo separado no build.
const EDITORES = ['admin', 'analista'];

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, from, salvo) {
    if (salvo) return salvo;
    if (to.hash) return { el: to.hash, behavior: 'smooth', top: 80 };
    return { top: 0 };
  },
  routes: [
    { path: '/', component: LandingPage },
    { path: '/login', component: () => import('../views/Login.vue'), meta: { somenteVisitante: true } },
    { path: '/cadastro', component: () => import('../views/Signup.vue'), meta: { somenteVisitante: true } },
    { path: '/recuperar-senha', component: () => import('../views/ForgotPassword.vue'), meta: { somenteVisitante: true } },
    { path: '/redefinir-senha', component: () => import('../views/RedefinirSenha.vue') },
    { path: '/termos', component: () => import('../views/Termos.vue') },
    { path: '/privacidade', component: () => import('../views/Privacidade.vue') },
    {
      path: '/app',
      component: AppShell,
      meta: { requiresAuth: true },
      children: [
        { path: '', component: () => import('../views/app/Dashboard.vue') },
        { path: 'analise', component: () => import('../views/app/Analise.vue') },
        { path: 'upload', component: () => import('../views/app/Upload.vue'), meta: { papeis: EDITORES } },
        { path: 'relatorio', component: () => import('../views/app/Relatorio.vue'), meta: { papeis: EDITORES } },
        { path: 'planilhas', component: () => import('../views/app/Files.vue') },
        { path: 'dataset', component: () => import('../views/app/Dataset.vue') },
        { path: 'clientes', component: () => import('../views/app/Clientes.vue') },
        { path: 'clientes/:id', component: () => import('../views/app/ClienteDetalhe.vue'), props: true },
        { path: 'consultores', component: () => import('../views/app/Consultores.vue') },
        { path: 'servicos', component: () => import('../views/app/Servicos.vue') },
        { path: 'insights', component: () => import('../views/app/Insights.vue') },
        { path: 'usuarios', component: () => import('../views/app/Users.vue'), meta: { papeis: ['admin'] } },
        { path: 'telemetria', component: () => import('../views/app/Telemetria.vue'), meta: { papeis: ['admin'] } },
        { path: 'configuracoes', component: () => import('../views/app/Settings.vue') },
      ],
    },
    { path: '/:pathMatch(.*)*', component: () => import('../views/NotFound.vue') },
  ],
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  if (to.meta.requiresAuth && !auth.logado) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  if (to.meta.somenteVisitante && auth.logado) {
    return { path: '/app' };
  }
  const papeis = to.matched.flatMap((r) => r.meta.papeis || []);
  if (papeis.length && !papeis.includes(auth.papel)) {
    return { path: '/app' };
  }
});

export default router;
