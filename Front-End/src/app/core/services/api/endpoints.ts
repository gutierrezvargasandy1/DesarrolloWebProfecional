export const API_ENDPOINTS = {
  // ==================== AUTH ====================
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH_TOKEN: '/auth/refresh',
    FORGOT_PASSWORD: '/auth/recuperar',
    VERIFY_CODE: '/auth/verificar-codigo',
    CHANGE_PASSWORD: '/auth/cambiar-password',
  },

  // ==================== USERS ====================
  USERS: {
    BASE: '/users',
    GET_ALL: '/users',
    GET_BY_ID: (id: number | string) => `/users/${id}`,
    CREATE: '/users',
    UPDATE: (id: number | string) => `/users/${id}`,
    DELETE: (id: number | string) => `/users/${id}`,
    UPDATE_STATUS: (id: number | string) => `/users/${id}/status`,
    ASSIGN_ROLE: (id: number | string) => `/users/${id}/role`,
    SEARCH: '/users/search'
  },

  // ==================== PRODUCTS ====================
  PRODUCTS: {
    BASE: '/products',
    GET_ALL: '/products',
    GET_BY_ID: (id: number | string) => `/products/${id}`,
    CREATE: '/products',
    UPDATE: (id: number | string) => `/products/${id}`,
    DELETE: (id: number | string) => `/products/${id}`,
    CATEGORIES: '/products/categories',
    SEARCH: '/products/search',
    STOCK: (id: number | string) => `/products/${id}/stock`
  },

  // ==================== REPORTS ====================
  REPORTS: {
    BASE: '/reports',
    SALES: '/reports/sales',
    USERS: '/reports/users',
    PRODUCTS: '/reports/products',
    EXPORT: '/reports/export',
    DOWNLOAD: (id: string) => `/reports/download/${id}`
  },

  // ==================== DASHBOARD ====================
  DASHBOARD: {
    STATS: '/dashboard/stats',
    CHARTS: '/dashboard/charts',
    ACTIVITY: '/dashboard/activity'
  },

  // ==================== UPLOADS ====================
  UPLOADS: {
    SINGLE: '/uploads/single',
    MULTIPLE: '/uploads/multiple',
    AVATAR: '/uploads/avatar',
    DELETE: (filename: string) => `/uploads/${filename}`
  }
} as const;

// Tipos para autocompletado
export type ApiEndpoints = typeof API_ENDPOINTS;