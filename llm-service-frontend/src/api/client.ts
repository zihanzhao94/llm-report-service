// src/api/client.ts
import axios from 'axios'

// Create axios instance, configure base URL
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Optional: Add request interceptor (e.g. add token)
// apiClient.interceptors.request.use(config => { ... })

// Optional: Add response interceptor (e.g. handle errors)
// apiClient.interceptors.response.use(response => { ... }, error => { ... })