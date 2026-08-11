import { createRouter, createWebHistory } from 'vue-router'
import LandingPage from '@/views/LandingPage.vue'
import Login from '@/views/Login.vue'

const routes = [
    { path: '/', name: 'home', component: LandingPage },
    { path: '/login', name: 'login', component: Login },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router
