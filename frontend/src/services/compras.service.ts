/**
 * Compras: proveedores y órdenes de compra.
 */

import { apiClient } from "@/services/api-client";

export interface ProveedorDto {
  id: string;
  razonSocial: string;
  ruc: string;
  estado?: string;
  contacto?: string;
  direccion?: string;
}

export interface OrdenCompraDto {
  id: string;
  numero?: string;
  proyectoId: string;
  estado?: string;
  montoTotal?: number;
  fecha?: string;
  proveedor?: ProveedorDto;
}

export class ComprasService {
  static async listarProveedores(page = 0, size = 50): Promise<ProveedorDto[]> {
    const res = await apiClient.get<ProveedorDto[] | { content: ProveedorDto[] }>("/proveedores", {
      params: { page, size },
    });
    return Array.isArray(res) ? res : (res.content ?? []);
  }

  static async crearProveedor(body: {
    razonSocial: string;
    ruc: string;
    contacto?: string;
    direccion?: string;
  }): Promise<ProveedorDto> {
    return apiClient.post<ProveedorDto>("/proveedores", body);
  }

  static async listarOrdenes(proyectoId: string): Promise<OrdenCompraDto[]> {
    const res = await apiClient.get<OrdenCompraDto[] | { content: OrdenCompraDto[] }>(
      "/ordenes-compra",
      { params: { proyectoId, page: 0, size: 50 } }
    );
    return Array.isArray(res) ? res : (res.content ?? []);
  }

  static async crearOrden(body: {
    proyectoId: string;
    proveedorId: string;
    fecha: string;
    condicionesPago?: string;
    observaciones?: string;
    detalles: Array<{
      partidaId: string;
      descripcion: string;
      cantidad: number;
      unidad?: string;
      precioUnitario: number;
    }>;
  }): Promise<OrdenCompraDto> {
    return apiClient.post<OrdenCompraDto>("/ordenes-compra", body);
  }
}
