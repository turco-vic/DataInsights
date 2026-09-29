import axios from 'axios';
import { useAuthStore } from '../stores/authStore';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const { token } = useAuthStore();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default api;
