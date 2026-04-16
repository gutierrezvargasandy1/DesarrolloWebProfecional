export const API_ENDPOINTS = {
  // ==================== AUTH ====================
  AUTH: {
    LOGIN: '/auth/login',
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

  // ==================== REPORTES ====================
  REPORTES: {
    BASE: '/reportes',
    GET_ALL: '/reportes',
    GET_BY_ID: (id: number) => `/reportes/${id}`,
    GET_ALL_PUBLIC: '/reportes/public',
    GET_PUBLIC_BY_ID: (id: number) => `/reportes/public/${id}`,
    GET_ALL_FULL: '/reportes/full',
    GET_FULL_BY_ID: (id: number) => `/reportes/${id}/full`,
    CREATE: '/reportes',
    UPDATE: (id: number) => `/reportes/${id}`,
    DELETE: (id: number) => `/reportes/${id}`,
    CREATE_AVISTAMIENTO: (idReporte: number) => `/reportes/${idReporte}/avistamientos`,
    UPDATE_AVISTAMIENTO: (idAvistamiento: number) => `/reportes/avistamientos/${idAvistamiento}`,
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