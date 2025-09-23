import axios from 'axios';

export const API_BASE = (import.meta?.env?.VITE_API_BASE_URL || 'http://localhost:8080') + '/api';

const api = axios.create({ baseURL: API_BASE });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
