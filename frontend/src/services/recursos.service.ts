/**
 * Servicio para operaciones relacionadas con Recursos (Catálogo).
 */

import type { Recurso, TipoRecurso } from '@/core/types/recursos';

/**
 * Servicio de Recursos.
 */
export class RecursosService {
  /**
   * Obtiene todos los recursos del catálogo.
   * 
   * TODO: Reemplazar con llamada real al backend cuando esté disponible.
   * Por ahora retorna datos mock para desarrollo.
   */
  static async listar(): Promise<Recurso[]> {
    // Simular delay de red
    await new Promise(resolve => setTimeout(resolve, 300));

    // Datos mock para desarrollo
    return [
      // MATERIALES
      {
        id: 'mat-1',
        nombre: 'Cemento Portland Tipo I',
        unidad: 'bolsa',
        precioBase: 8.50,
        tipo: 'MATERIAL' as TipoRecurso,
        codigo: 'MAT-001',
        descripcion: 'Cemento gris estándar 42.5 kg'
      },
      {
        id: 'mat-2',
        nombre: 'Arena Fina',
        unidad: 'm³',
        precioBase: 25.00,
        tipo: 'MATERIAL' as TipoRecurso,
        codigo: 'MAT-002',
        descripcion: 'Arena de río lavada'
      },
      {
        id: 'mat-3',
        nombre: 'Piedrín 3/4"',
        unidad: 'm³',
        precioBase: 28.00,
        tipo: 'MATERIAL' as TipoRecurso,
        codigo: 'MAT-003',
        descripcion: 'Agregado grueso para concreto'
      },
      {
        id: 'mat-4',
        nombre: 'Ladrillo de Arcilla',
        unidad: 'unidad',
        precioBase: 0.35,
        tipo: 'MATERIAL' as TipoRecurso,
        codigo: 'MAT-004',
        descripcion: 'Ladrillo estándar 6x12x24 cm'
      },
      {
        id: 'mat-5',
        nombre: 'Acero de Refuerzo #3',
        unidad: 'kg',
        precioBase: 1.20,
        tipo: 'MATERIAL' as TipoRecurso,
        codigo: 'MAT-005',
        descripcion: 'Varilla corrugada 3/8"'
      },
      {
        id: 'mat-6',
        nombre: 'Alambre de Amarre',
        unidad: 'kg',
        precioBase: 2.50,
        tipo: 'MATERIAL' as TipoRecurso,
        codigo: 'MAT-006',
        descripcion: 'Alambre negro calibre 16'
      },
      
      // MANO DE OBRA
      {
        id: 'mo-1',
        nombre: 'Peón',
        unidad: 'día',
        precioBase: 25.00,
        tipo: 'MANO_DE_OBRA' as TipoRecurso,
        codigo: 'MO-001',
        descripcion: 'Obrero general'
      },
      {
        id: 'mo-2',
        nombre: 'Operario',
        unidad: 'día',
        precioBase: 35.00,
        tipo: 'MANO_DE_OBRA' as TipoRecurso,
        codigo: 'MO-002',
        descripcion: 'Obrero especializado'
      },
      {
        id: 'mo-3',
        nombre: 'Maestro de Obra',
        unidad: 'día',
        precioBase: 50.00,
        tipo: 'MANO_DE_OBRA' as TipoRecurso,
        codigo: 'MO-003',
        descripcion: 'Capataz con experiencia'
      },
      {
        id: 'mo-4',
        nombre: 'Ayudante de Albañil',
        unidad: 'día',
        precioBase: 22.00,
        tipo: 'MANO_DE_OBRA' as TipoRecurso,
        codigo: 'MO-004',
        descripcion: 'Asistente de albañil'
      },
      
      // EQUIPOS
      {
        id: 'eq-1',
        nombre: 'Mezcladora de Concreto 1.5 m³',
        unidad: 'día',
        precioBase: 45.00,
        tipo: 'EQUIPO' as TipoRecurso,
        codigo: 'EQ-001',
        descripcion: 'Mezcladora portátil con motor'
      },
      {
        id: 'eq-2',
        nombre: 'Vibrador de Concreto',
        unidad: 'día',
        precioBase: 15.00,
        tipo: 'EQUIPO' as TipoRecurso,
        codigo: 'EQ-002',
        descripcion: 'Vibrador eléctrico portátil'
      },
      {
        id: 'eq-3',
        nombre: 'Andamio Metálico',
        unidad: 'día',
        precioBase: 12.00,
        tipo: 'EQUIPO' as TipoRecurso,
        codigo: 'EQ-003',
        descripcion: 'Andamio tubular estándar'
      },
      {
        id: 'eq-4',
        nombre: 'Planta de Concreto',
        unidad: 'día',
        precioBase: 200.00,
        tipo: 'EQUIPO' as TipoRecurso,
        codigo: 'EQ-004',
        descripcion: 'Planta estacionaria 10 m³/h'
      },
      
      // SUBCONTRATOS
      {
        id: 'sub-1',
        nombre: 'Transporte de Materiales',
        unidad: 'viaje',
        precioBase: 80.00,
        tipo: 'SUBCONTRATO' as TipoRecurso,
        codigo: 'SUB-001',
        descripcion: 'Camión de 6 m³'
      }
    ];
  }

  /**
   * Busca recursos por término.
   * 
   * TODO: Reemplazar con llamada real al backend cuando esté disponible.
   */
  static async buscar(termino: string, tipo?: TipoRecurso): Promise<Recurso[]> {
    const todos = await this.listar();
    
    let filtrados = todos;
    
    // Filtrar por tipo si se especifica
    if (tipo) {
      filtrados = filtrados.filter(r => r.tipo === tipo);
    }
    
    // Filtrar por término de búsqueda
    if (termino.trim()) {
      const terminoLower = termino.toLowerCase();
      filtrados = filtrados.filter(r =>
        r.nombre.toLowerCase().includes(terminoLower) ||
        r.codigo?.toLowerCase().includes(terminoLower) ||
        r.descripcion?.toLowerCase().includes(terminoLower)
      );
    }
    
    return filtrados;
  }

  /**
   * Obtiene un recurso por ID.
   * 
   * TODO: Reemplazar con llamada real al backend cuando esté disponible.
   */
  static async obtenerPorId(id: string): Promise<Recurso | null> {
    const todos = await this.listar();
    return todos.find(r => r.id === id) || null;
  }
}
