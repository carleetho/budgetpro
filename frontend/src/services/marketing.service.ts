/**
 * Servicio público para solicitudes de demo.
 */

import { API_BASE_URL } from "@/core/config/env";
import { ApiClient } from "./api-client";

const apiRoot = API_BASE_URL.replace(/\/api\/v1\/?$/, "");
const publicClient = new ApiClient(apiRoot);

export interface CrearLeadRequest {
  nombreContacto: string;
  email?: string;
  telefono?: string;
  nombreEmpresa?: string;
  rol?: string;
}

export interface LeadResponse {
  id: string;
  estado: "NUEVO" | "CONTACTADO" | "CONVERTIDO";
  fechaSolicitud: string;
}

export class MarketingService {
  static async crearLead(data: CrearLeadRequest): Promise<LeadResponse> {
    return publicClient.post<LeadResponse>("/api/public/v1/demo-request", data);
  }
}
