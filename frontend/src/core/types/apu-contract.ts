/**
 * Contratos REST para APU (`ApuController` / `ApuResponse`).
 */

export interface ApuInsumoResponseDto {
  id: string;
  recursoId: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface ApuResponseDto {
  id: string;
  partidaId: string;
  rendimiento: number | null;
  unidad: string | null;
  costoTotal: number;
  version: number | null;
  insumos: ApuInsumoResponseDto[];
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CrearApuInsumoRequestDto {
  recursoId: string;
  cantidad: number;
  precioUnitario: number;
}

export interface CrearApuRequestDto {
  rendimiento?: number | null;
  unidad?: string | null;
  insumos: CrearApuInsumoRequestDto[];
}
