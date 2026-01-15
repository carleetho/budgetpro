/**
 * Tipos relacionados con las respuestas y requests de la API.
 */

/**
 * Respuesta estándar de la API.
 */
export interface ApiResponse<T> {
  data: T;
  message?: string;
  errors?: string[];
}

/**
 * Respuesta paginada de la API.
 */
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * Parámetros de paginación.
 */
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

/**
 * Parámetros de búsqueda.
 */
export interface SearchParams {
  search?: string;
  [key: string]: string | number | boolean | undefined;
}
