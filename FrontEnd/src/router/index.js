import { createRouter, createWebHistory } from 'vue-router';
import LandingPage from '../views/LandingPage.vue';
import Login from '../views/Login.vue';
import Signup from '../views/Signup.vue';
import ForgotPassword from '../views/ForgotPassword.vue';
import NotFound from '../views/NotFound.vue';
import AppShell from '../layouts/AppShell.vue';
import Dashboard from '../views/app/Dashboard.vue';
import Upload from '../views/app/Upload.vue';
import Relatorio from '../views/app/Relatorio.vue';
import Files from '../views/app/Files.vue';
import Dataset from '../views/app/Dataset.vue';
import Users from '../views/app/Users.vue';
import Settings from '../views/app/Settings.vue';
import { useAuthStore } from '../stores/authStore';

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/', component: LandingPage },
    { path: '/login', component: Login },
    { path: '/cadastro', component: Signup },
    { path: '/recuperar-senha', component: ForgotPassword },
    {
      path: '/app',
      component: AppShell,
      meta: { requiresAuth: true },
      children: [
        { path: '', component: Dashboard },
        { path: 'upload', component: Upload },
        { path: 'relatorio', component: Relatorio },
        { path: 'planilhas', component: Files },
        { path: 'dataset', component: Dataset },
        { path: 'usuarios', component: Users },
        { path: 'configuracoes', component: Settings },
      ],
    },
    { path: '/:pathMatch(.*)*', component: NotFound },
  ],
});

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !useAuthStore().logado) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
});

export default router;
