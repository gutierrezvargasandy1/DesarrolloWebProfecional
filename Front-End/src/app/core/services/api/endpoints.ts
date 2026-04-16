export const API_ENDPOINTS = {
  // ==================== AUTH ====================
  AUTH: {
    LOGIN: '/auth/login',
    REFRESH_TOKEN: '/auth/refresh',
    FORGOT_PASSWORD: '/auth/recuperar',
    VERIFY_CODE: '/auth/verificar-codigo',
    CHANGE_PASSWORD: '/auth/cambiar-password',
  },

    // ==================== MASCOTAS ====================
MASCOTAS: {
  BASE: '/mascotas/',
  GET_ALL: '/mascotas/',
  GET_BY_ID: (id: number) => `/mascotas/${id}`,
  CREATE: '/mascotas/',
  UPDATE: (id: number) => `/mascotas/${id}`,
  DELETE: (id: number) => `/mascotas/${id}`,
},

  // ==================== REPORTES ====================
  REPORTES: {
    BASE: '/reportes',

    // ========== CRUD REPORTES ==========
    // ✅ Agregamos barra final para rutas raíz
    GET_ALL: '/reportes/',      // antes '/reportes'
    GET_BY_ID: (id: number) => `/reportes/${id}`, // sin barra final, correcto

    CREATE: '/reportes/',       // antes '/reportes'
    UPDATE: (id: number) => `/reportes/${id}`,
    DELETE: (id: number) => `/reportes/${id}`,

    // ========== FULL ==========
    GET_ALL_FULL: '/reportes/full',
    GET_FULL_BY_ID: (id: number) => `/reportes/${id}/full`,

    // ========== PUBLIC ==========
    GET_ALL_PUBLIC: '/reportes/public',
    GET_PUBLIC_BY_ID: (id: number) => `/reportes/public/${id}`,

    // ========== AVISTAMIENTOS ==========
    CREATE_AVISTAMIENTO: (idReporte: number) =>
      `/reportes/${idReporte}/avistamientos`,

    UPDATE_AVISTAMIENTO: (idAvistamiento: number) =>
      `/reportes/avistamientos/${idAvistamiento}`,
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