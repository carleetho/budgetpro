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

export class AuthService {
  static async login(data: LoginRequest): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>("/auth/login", data);
  }

  static async register(data: { nombreCompleto: string; email: string; password: string }): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>("/auth/register", data);
  }

  static logout() {
    if (typeof window === "undefined") return;
    localStorage.removeItem("auth_token");
    localStorage.removeItem("auth_user");
  }
}
