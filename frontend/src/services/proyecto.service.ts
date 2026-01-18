/**
 * Servicio para operaciones relacionadas con Proyectos.
 */

import { apiClient } from './api-client';
import type { Proyecto } from '@/core/types';

/**
 * Servicio de Proyectos.
 */
export class ProyectoService {
  /**
   * Crea un nuevo proyecto.
   */
  static async crear(data: { nombre: string; ubicacion?: string }): Promise<Proyecto> {
    return apiClient.post<Proyecto>('/proyectos', data);
  }

  /**
   * Obtiene todos los proyectos.
   */
  static async listar(): Promise<Proyecto[]> {
    return apiClient.get<Proyecto[]>('/proyectos');
  }

  /**
   * Obtiene un proyecto por ID.
   */
  static async obtenerPorId(id: string): Promise<Proyecto> {
    return apiClient.get<Proyecto>(`/proyectos/${id}`);
  }
}
