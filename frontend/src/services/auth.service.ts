/**
 * Servicio para autenticación.
 */

import { apiClient } from "./api-client";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  usuarioId: string;
  email: string;
  rol: string;
}

export interface AuthMeResponse {
  id?: string;
  usuarioId?: string;
  email?: string;
  rol?: string;
  nombreCompleto?: string;
  [key: string]: unknown;
}

export class AuthService {
  static async login(data: LoginRequest): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>("/auth/login", data);
  }

  static async register(data: {
    nombreCompleto: string;
    email: string;
    password: string;
  }): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>("/auth/register", data);
  }

  /** `GET /api/v1/auth/me` — valida sesión y refresca perfil. */
  static async me(): Promise<AuthMeResponse> {
    return apiClient.get<AuthMeResponse>("/auth/me");
  }

  static logout() {
    if (typeof window === "undefined") return;
    localStorage.removeItem("auth_token");
    localStorage.removeItem("auth_user");
  }
}
