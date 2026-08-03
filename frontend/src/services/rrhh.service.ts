/**
 * RRHH: empleados, asistencias, cuadrillas.
 */

import { apiClient } from "@/services/api-client";

export interface EmpleadoDto {
  id: string;
  nombre: string;
  apellido: string;
  numeroIdentificacion: string;
  email?: string;
  telefono?: string;
  estado?: string;
  salarioActual?: number;
  puestoActual?: string;
  tipo?: string;
  fechaContratacion?: string;
}

export interface AsistenciaDto {
  id: string;
  fecha: string;
  horaEntrada?: string;
  horaSalida?: string;
  horasTrabajadas?: number;
  horasExtras?: number;
}

export interface CuadrillaDto {
  id: string;
  proyectoId: string;
  nombre: string;
  tipo?: string;
  liderId?: string;
  estado?: string;
  miembrosIds?: string[];
}

export class RrhhService {
  static async listarEmpleados(estado?: string): Promise<EmpleadoDto[]> {
    return apiClient.get<EmpleadoDto[]>("/rrhh/empleados", {
      params: estado ? { estado } : undefined,
    });
  }

  static async crearEmpleado(body: {
    nombre: string;
    apellido: string;
    numeroIdentificacion: string;
    email?: string;
    telefono?: string;
    direccion?: string;
    fechaContratacion: string;
    salarioInicial: number;
    puestoInicial: string;
    tipo: string;
  }): Promise<EmpleadoDto> {
    return apiClient.post<EmpleadoDto>("/rrhh/empleados", body);
  }

  static async listarAsistencias(params: {
    proyectoId?: string;
    empleadoId?: string;
    fechaInicio: string;
    fechaFin: string;
  }): Promise<AsistenciaDto[]> {
    return apiClient.get<AsistenciaDto[]>("/rrhh/asistencias", { params });
  }

  static async registrarAsistencia(body: {
    empleadoId: string;
    proyectoId: string;
    fecha: string;
    horaEntrada: string;
    horaSalida: string;
    ubicacion?: string;
  }): Promise<AsistenciaDto> {
    return apiClient.post<AsistenciaDto>("/rrhh/asistencias", body);
  }

  static async listarCuadrillas(): Promise<CuadrillaDto[]> {
    return apiClient.get<CuadrillaDto[]>("/rrhh/cuadrillas");
  }

  static async crearCuadrilla(body: {
    proyectoId: string;
    nombre: string;
    tipo?: string;
    liderEmpleadoId: string;
    miembrosInicialesIds?: string[];
  }): Promise<CuadrillaDto> {
    return apiClient.post<CuadrillaDto>("/rrhh/cuadrillas", body);
  }
}
