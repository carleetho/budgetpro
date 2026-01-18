/**
 * Servicio para operaciones relacionadas con Presupuestos.
 */

import { apiClient } from './api-client';
import type { Presupuesto } from '@/core/types';
import type { ItemPresupuesto, CrearItemPresupuestoCommand } from '@/core/types/presupuesto';

/**
 * Servicio de Presupuestos.
 */
export class PresupuestoService {
  /**
   * Crea un nuevo presupuesto.
   */
  static async crear(data: { proyectoId: string; nombre: string }): Promise<Presupuesto> {
    return apiClient.post<Presupuesto>('/presupuestos', data);
  }

  /**
   * Obtiene un presupuesto por ID.
   */
  static async obtenerPorId(id: string): Promise<Presupuesto> {
    return apiClient.get<Presupuesto>(`/presupuestos/${id}`);
  }

  /**
   * Aprueba un presupuesto.
   */
  static async aprobar(id: string): Promise<void> {
    return apiClient.post<void>(`/presupuestos/${id}/aprobar`);
  }

  /**
   * Obtiene el reporte de control de costos (Plan vs Real).
   */
  static async obtenerControlCostos(id: string) {
    return apiClient.get(`/presupuestos/${id}/control-costos`);
  }

  /**
   * Obtiene el árbol jerárquico de partidas del presupuesto.
   * 
   * TODO: Reemplazar con llamada real al backend cuando esté disponible.
   * Por ahora retorna datos mock para desarrollo.
   */
  static async obtenerArbol(proyectoId: string): Promise<ItemPresupuesto[]> {
    // Simular delay de red
    await new Promise(resolve => setTimeout(resolve, 500));

    // Datos mock para desarrollo
    return [
      {
        id: '1',
        codigo: '1',
        descripcion: 'OBRAS PRELIMINARES',
        nivel: 'CAPITULO',
        hijos: [
          {
            id: '1.1',
            codigo: '1.01',
            descripcion: 'Limpieza y desmonte',
            nivel: 'PARTIDA',
            unidad: 'm²',
            metrado: 500,
            precioUnitario: 2.5,
            parcial: 1250,
            padreId: '1',
            hijos: []
          },
          {
            id: '1.2',
            codigo: '1.02',
            descripcion: 'Nivelación y compactación',
            nivel: 'PARTIDA',
            unidad: 'm²',
            metrado: 500,
            precioUnitario: 3.0,
            parcial: 1500,
            padreId: '1',
            hijos: []
          }
        ],
        parcial: 2750
      },
      {
        id: '2',
        codigo: '2',
        descripcion: 'CIMENTACIONES',
        nivel: 'CAPITULO',
        hijos: [
          {
            id: '2.1',
            codigo: '2.01',
            descripcion: 'Excavación manual',
            nivel: 'SUBCAPITULO',
            hijos: [
              {
                id: '2.1.1',
                codigo: '2.01.01',
                descripcion: 'Excavación en tierra',
                nivel: 'PARTIDA',
                unidad: 'm³',
                metrado: 120,
                precioUnitario: 8.5,
                parcial: 1020,
                padreId: '2.1',
                hijos: []
              },
              {
                id: '2.1.2',
                codigo: '2.01.02',
                descripcion: 'Excavación en roca',
                nivel: 'PARTIDA',
                unidad: 'm³',
                metrado: 30,
                precioUnitario: 25.0,
                parcial: 750,
                padreId: '2.1',
                hijos: []
              }
            ],
            parcial: 1770
          },
          {
            id: '2.2',
            codigo: '2.02',
            descripcion: 'Concreto estructural',
            nivel: 'PARTIDA',
            unidad: 'm³',
            metrado: 45,
            precioUnitario: 150.0,
            parcial: 6750,
            padreId: '2',
            hijos: []
          }
        ],
        parcial: 8520
      },
      {
        id: '3',
        codigo: '3',
        descripcion: 'ESTRUCTURA',
        nivel: 'CAPITULO',
        hijos: [
          {
            id: '3.1',
            codigo: '3.01',
            descripcion: 'Acero de refuerzo',
            nivel: 'PARTIDA',
            unidad: 'kg',
            metrado: 3500,
            precioUnitario: 1.2,
            parcial: 4200,
            padreId: '3',
            hijos: []
          }
        ],
        parcial: 4200
      }
    ];
  }

  /**
   * Crea o actualiza un item del presupuesto.
   * 
   * TODO: Reemplazar con llamada real al backend cuando esté disponible.
   * Por ahora simula el guardado.
   */
  static async crearItem(proyectoId: string, item: CrearItemPresupuestoCommand): Promise<ItemPresupuesto> {
    // Simular delay de red
    await new Promise(resolve => setTimeout(resolve, 300));

    // Calcular parcial si es PARTIDA
    const parcial = item.metrado && item.precioUnitario
      ? item.metrado * item.precioUnitario
      : undefined;

    // Retornar item simulado
    return {
      id: `mock-${Date.now()}`,
      codigo: item.codigo,
      descripcion: item.descripcion,
      nivel: item.nivel,
      unidad: item.unidad,
      metrado: item.metrado,
      precioUnitario: item.precioUnitario,
      parcial,
      padreId: item.padreId || null,
      hijos: []
    };
  }

  /**
   * Elimina un item del presupuesto.
   * 
   * TODO: Reemplazar con llamada real al backend cuando esté disponible.
   */
  static async eliminarItem(itemId: string): Promise<void> {
    // Simular delay de red
    await new Promise(resolve => setTimeout(resolve, 200));
  }
}
