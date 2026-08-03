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

/** Contrato backend `GET /api/v1/auth/me` (AuthMeResponse). */
interface AuthMeApiResponse {
  usuarioId: string;
  nombreCompleto: string;
  email: string;
  rol: string;
}

/** Perfil normalizado para UI del shell (REQ-70). */
export interface AuthMe {
  id: string;
  nombre: string;
  email: string;
  rol: string;
  avatarUrl: string | null;
}

export class AuthService {
  static async login(data: LoginRequest): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>("/auth/login", data);
  }

  static async register(data: { nombreCompleto: string; email: string; password: string }): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>("/auth/register", data);
  }

  /**
   * Perfil del usuario autenticado.
   * Mapea `GET /api/v1/auth/me` al shape de UI (no existe `/api/user/me`).
   */
  static async me(): Promise<AuthMe> {
    const raw = await apiClient.get<AuthMeApiResponse>("/auth/me");
    return {
      id: raw.usuarioId,
      nombre: raw.nombreCompleto,
      email: raw.email,
      rol: raw.rol,
      avatarUrl: null,
    };
  }

  static logout() {
    if (typeof window === "undefined") return;
    localStorage.removeItem("auth_token");
    localStorage.removeItem("auth_user");
  }
}
