/**
 * Configuración de variables de entorno y constantes globales.
 * 
 * Centraliza todas las configuraciones del frontend para facilitar
 * el mantenimiento y evitar hardcoding de valores.
 */

/**
 * URL base del backend API.
 * Por defecto: http://localhost:8080/api/v1
 */
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1';

/**
 * Timeout para peticiones HTTP (en milisegundos).
 */
export const API_TIMEOUT = 30000; // 30 segundos

/**
 * Configuración de paginación por defecto.
 */
export const DEFAULT_PAGE_SIZE = 20;
export const DEFAULT_PAGE = 0;

/**
 * Configuración de la aplicación.
 */
export const APP_CONFIG = {
  name: 'BudgetPro',
  version: '1.0.0',
  description: 'Sistema de Control Técnico-Financiero',
} as const;
