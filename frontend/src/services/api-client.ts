/**
 * Cliente HTTP para comunicación con el Backend.
 * 
 * Centraliza todas las peticiones HTTP usando Fetch API.
 * Proporciona métodos helper para GET, POST, PUT, DELETE.
 */

import { API_BASE_URL, API_TIMEOUT } from '@/core/config/env';
import type { ApiResponse, PaginatedResponse, PaginationParams, SearchParams } from '@/core/types';

/**
 * Opciones de configuración para peticiones HTTP.
 */
interface RequestOptions extends RequestInit {
  params?: SearchParams | PaginationParams;
  timeout?: number;
}

/**
 * Clase para manejar peticiones HTTP al backend.
 */
class ApiClient {
  private baseURL: string;
  private defaultTimeout: number;

  constructor(baseURL: string = API_BASE_URL, timeout: number = API_TIMEOUT) {
    this.baseURL = baseURL;
    this.defaultTimeout = timeout;
  }

  /**
   * Construye la URL completa con query parameters.
   */
  private buildURL(endpoint: string, params?: SearchParams | PaginationParams): string {
    const url = new URL(`${this.baseURL}${endpoint}`);
    
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          url.searchParams.append(key, String(value));
        }
      });
    }
    
    return url.toString();
  }

  /**
   * Realiza una petición HTTP con timeout.
   */
  private async fetchWithTimeout(url: string, options: RequestInit, timeout: number): Promise<Response> {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeout);
    
    try {
      const response = await fetch(url, {
        ...options,
        signal: controller.signal,
      });
      clearTimeout(timeoutId);
      return response;
    } catch (error) {
      clearTimeout(timeoutId);
      throw error;
    }
  }

  /**
   * Maneja errores HTTP y lanza excepciones apropiadas.
   */
  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      if (response.status === 401 && typeof window !== 'undefined') {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('auth_user');
        window.location.href = '/login';
      }
      let errorMessage = response.statusText;
      let errorDetails: any = null;
      
      // Clonar la respuesta para poder leer el body múltiples veces si es necesario
      const clonedResponse = response.clone();
      
      try {
        const contentType = response.headers.get('content-type');
        const text = await response.text();
        
        if (text) {
          // Intentar parsear como JSON
          if (contentType && contentType.includes('application/json')) {
            try {
              const error = JSON.parse(text);
              errorDetails = error;
              
              // Intentar extraer el mensaje de error de diferentes formatos
              if (error.message) {
                errorMessage = error.message;
              } else if (error.error) {
                errorMessage = error.error;
              } else if (Array.isArray(error.errors) && error.errors.length > 0) {
                errorMessage = error.errors[0];
              } else if (typeof error === 'string') {
                errorMessage = error;
              }
            } catch (jsonError) {
              // Si no es JSON válido, usar el texto directamente
              errorMessage = text;
            }
          } else {
            // Si no es JSON, usar el texto directamente
            errorMessage = text;
          }
        }
      } catch (parseError) {
        // Si falla completamente, usar el statusText
        console.error('Error parsing error response:', parseError);
      }
      
      // Log detallado del error para debugging
      console.error('API Error Details:', {
        status: response.status,
        statusText: response.statusText,
        url: response.url,
        errorMessage,
        errorDetails,
      });
      
      // Crear un error más informativo
      const error = new Error(errorMessage || `HTTP ${response.status}: ${response.statusText}`);
      (error as any).status = response.status;
      (error as any).details = errorDetails;
      throw error;
    }
    
    // Si la respuesta está vacía (204 No Content)
    if (response.status === 204) {
      return undefined as T;
    }
    
    return response.json();
  }

  /**
   * Realiza una petición GET.
   */
  async get<T>(endpoint: string, options?: RequestOptions): Promise<T> {
    const url = this.buildURL(endpoint, options?.params);
    const timeout = options?.timeout || this.defaultTimeout;
    
    const response = await this.fetchWithTimeout(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(typeof window !== 'undefined' && localStorage.getItem('auth_token')
          ? { Authorization: `Bearer ${localStorage.getItem('auth_token')}` }
          : {}),
        ...options?.headers,
      },
      ...options,
    }, timeout);
    
    return this.handleResponse<T>(response);
  }

  /**
   * Realiza una petición PATCH.
   */
  async patch<T>(endpoint: string, data?: any, options?: RequestOptions): Promise<T> {
    const url = this.buildURL(endpoint, options?.params);
    const response = await this.fetchWithTimeout(
      url,
      {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...(typeof window !== 'undefined' && localStorage.getItem('auth_token')
            ? { Authorization: `Bearer ${localStorage.getItem('auth_token')}` }
            : {}),
          ...(options?.headers || {}),
        },
        body: data ? JSON.stringify(data) : undefined,
        ...options,
      },
      options?.timeout || this.defaultTimeout
    );
    return this.handleResponse<T>(response);
  }

  /**
   * Realiza una petición POST.
   */
  async post<T>(endpoint: string, data?: unknown, options?: RequestOptions): Promise<T> {
    const url = this.buildURL(endpoint, options?.params);
    const timeout = options?.timeout || this.defaultTimeout;
    
    const response = await this.fetchWithTimeout(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(typeof window !== 'undefined' && localStorage.getItem('auth_token')
          ? { Authorization: `Bearer ${localStorage.getItem('auth_token')}` }
          : {}),
        ...options?.headers,
      },
      body: data ? JSON.stringify(data) : undefined,
      ...options,
    }, timeout);
    
    return this.handleResponse<T>(response);
  }

  /**
   * Realiza una petición PUT.
   */
  async put<T>(endpoint: string, data?: unknown, options?: RequestOptions): Promise<T> {
    const url = this.buildURL(endpoint, options?.params);
    const timeout = options?.timeout || this.defaultTimeout;
    
    const response = await this.fetchWithTimeout(url, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(typeof window !== 'undefined' && localStorage.getItem('auth_token')
          ? { Authorization: `Bearer ${localStorage.getItem('auth_token')}` }
          : {}),
        ...options?.headers,
      },
      body: data ? JSON.stringify(data) : undefined,
      ...options,
    }, timeout);
    
    return this.handleResponse<T>(response);
  }

  /**
   * Realiza una petición DELETE.
   */
  async delete<T>(endpoint: string, options?: RequestOptions): Promise<T> {
    const url = this.buildURL(endpoint, options?.params);
    const timeout = options?.timeout || this.defaultTimeout;
    
    const response = await this.fetchWithTimeout(url, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        ...(typeof window !== 'undefined' && localStorage.getItem('auth_token')
          ? { Authorization: `Bearer ${localStorage.getItem('auth_token')}` }
          : {}),
        ...options?.headers,
      },
      ...options,
    }, timeout);
    
    return this.handleResponse<T>(response);
  }
}

// Exportar instancia singleton
export const apiClient = new ApiClient();

// Exportar clase para testing
export { ApiClient };
