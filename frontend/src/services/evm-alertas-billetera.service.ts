/**
 * EVM, alertas y billetera.
 */

import { apiClient } from "@/services/api-client";

export interface EvmSnapshotDto {
  pv?: number;
  ev?: number;
  ac?: number;
  bac?: number;
  cv?: number;
  sv?: number;
  cpi?: number;
  spi?: number;
  eac?: number;
  etc?: number;
  vac?: number;
  interpretacion?: string;
  [key: string]: unknown;
}

export interface AlertaItemDto {
  tipoAlerta?: string;
  nivel?: string;
  partidaId?: string;
  recursoId?: string;
  mensaje?: string;
  sugerencia?: string;
}

export interface AnalisisAlertasDto {
  id?: string;
  presupuestoId?: string;
  fechaAnalisis?: string;
  alertas?: AlertaItemDto[];
  [key: string]: unknown;
}

export interface BilleteraSaldoDto {
  id: string;
  proyectoId?: string;
  moneda?: string;
  saldoActual?: number;
}

export interface MovimientoCajaDto {
  id: string;
  monto: number;
  moneda?: string;
  tipo?: string;
  fecha?: string;
  referencia?: string;
  estado?: string;
}

export class EvmService {
  static async snapshot(proyectoId: string, fechaCorte?: string): Promise<EvmSnapshotDto> {
    return apiClient.get<EvmSnapshotDto>(`/evm/${proyectoId}`, {
      params: fechaCorte ? { fechaCorte } : undefined,
    });
  }
}

export class AlertasService {
  static async porPresupuesto(presupuestoId: string): Promise<AnalisisAlertasDto> {
    return apiClient.get<AnalisisAlertasDto>(`/analisis/alertas/${presupuestoId}`);
  }
}

export class BilleteraService {
  static async saldo(billeteraId: string): Promise<BilleteraSaldoDto> {
    return apiClient.get<BilleteraSaldoDto>(`/billeteras/${billeteraId}/saldo`);
  }

  static async movimientos(billeteraId: string): Promise<MovimientoCajaDto[]> {
    return apiClient.get<MovimientoCajaDto[]>(`/billeteras/${billeteraId}/movimientos`);
  }

  static async crearMovimiento(
    billeteraId: string,
    body: {
      monto: number;
      moneda: string;
      tipo: string;
      referencia: string;
      evidenciaUrl?: string;
    }
  ): Promise<MovimientoCajaDto> {
    return apiClient.post<MovimientoCajaDto>(`/billeteras/${billeteraId}/movimientos`, body);
  }
}
