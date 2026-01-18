"use client";

import { useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { PresupuestoService } from "@/services/presupuesto.service";
import { ProduccionService } from "@/services/produccion.service";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

interface ReportePartidaDTO {
  id: string;
  item: string;
  descripcion: string;
  unidad?: string | null;
  nivel?: number | null;
  metrado?: number | null;
  gastoAcumulado?: number | null;
  saldo?: number | null;
  hijos?: ReportePartidaDTO[] | null;
}

interface ReporteControlCostosResponse {
  partidas: ReportePartidaDTO[];
}

interface PartidaRow {
  id: string;
  codigo: string;
  descripcion: string;
  metradoTotal: number;
  acumuladoActual: number;
}

interface RPCFormProps {
  proyectoId?: string;
}

const formatNumber = (value: number) =>
  value.toLocaleString("es-ES", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

const isValidUuid = (value: string) =>
  /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/.test(
    value
  );

const flattenPartidas = (partidas: ReportePartidaDTO[]): ReportePartidaDTO[] => {
  return partidas.flatMap((partida) => {
    const hijos = partida.hijos ?? [];
    if (hijos.length === 0) {
      return [partida];
    }
    return flattenPartidas(hijos);
  });
};

export default function RPCForm({ proyectoId }: RPCFormProps) {
  const params = useParams();
  const routeId = Array.isArray(params?.id) ? params.id[0] : params?.id;
  const resolvedProyectoId = proyectoId ?? routeId ?? "";
  const [partidas, setPartidas] = useState<PartidaRow[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [avances, setAvances] = useState<Record<string, string>>({});

  useEffect(() => {
    const loadPartidas = async () => {
      if (!resolvedProyectoId || !isValidUuid(resolvedProyectoId)) {
        setLoadError("ID de proyecto no válido.");
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setLoadError(null);

      try {
        const response = await PresupuestoService.obtenerControlCostos(resolvedProyectoId);
        const controlCostos = response as ReporteControlCostosResponse;
        const hojas = flattenPartidas(controlCostos.partidas || []);
        const rows = hojas.map((partida) => ({
          id: partida.id,
          codigo: partida.item,
          descripcion: partida.descripcion,
          metradoTotal: Number(partida.metrado ?? 0),
          acumuladoActual: Number(partida.gastoAcumulado ?? 0),
        }));
        setPartidas(rows);
      } catch (error) {
        console.error("Error al cargar partidas:", error);
        const errorMessage =
          error instanceof Error
            ? error.message
            : "Error al cargar partidas del presupuesto.";
        setLoadError(errorMessage);
      } finally {
        setIsLoading(false);
      }
    };

    loadPartidas();
  }, [resolvedProyectoId]);

  const totalRows = useMemo(() => partidas.length, [partidas]);

  const handleAvanceChange = (partidaId: string, value: string) => {
    setAvances((prev) => ({
      ...prev,
      [partidaId]: value,
    }));
  };

  const handleSubmit = async () => {
    if (isSubmitting) return;
    setSubmitError(null);

    const items = Object.entries(avances)
      .map(([partidaId, value]) => ({
        partidaId,
        cantidad: Number.parseFloat(value),
      }))
      .filter((item) => Number.isFinite(item.cantidad) && item.cantidad > 0);

    if (items.length === 0) {
      toast.error("Ingresa un avance válido para al menos una partida.");
      return;
    }

    const fechaReporte = new Date().toISOString().slice(0, 10);

    setIsSubmitting(true);
    try {
      await ProduccionService.crear(resolvedProyectoId, {
        fechaReporte,
        items,
      });
      toast.success("Reporte de producción registrado correctamente.");
      setAvances({});
    } catch (error) {
      const status = (error as { status?: number }).status;
      const message =
        error instanceof Error
          ? error.message
          : "Error al registrar el reporte de producción.";

      if (status === 409) {
        setSubmitError(message);
        return;
      }

      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        <span className="ml-2 text-muted-foreground">Cargando partidas...</span>
      </div>
    );
  }

  if (loadError) {
    return (
      <Alert variant="destructive">
        <AlertTitle>Error al cargar</AlertTitle>
        <AlertDescription>{loadError}</AlertDescription>
      </Alert>
    );
  }

  return (
    <div className="space-y-4">
      {submitError && (
        <Alert variant="destructive">
          <AlertTitle>Regla de negocio</AlertTitle>
          <AlertDescription>{submitError}</AlertDescription>
        </Alert>
      )}

      <div className="flex items-center justify-between text-sm text-muted-foreground">
        <span>{totalRows} partidas disponibles</span>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Descripción</TableHead>
              <TableHead className="text-right">Metrado Total</TableHead>
              <TableHead className="text-right">Acumulado Actual</TableHead>
              <TableHead className="text-right">Avance Hoy</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {partidas.map((partida) => (
              <TableRow key={partida.id}>
                <TableCell className="font-medium">{partida.codigo}</TableCell>
                <TableCell>{partida.descripcion}</TableCell>
                <TableCell className="text-right">{formatNumber(partida.metradoTotal)}</TableCell>
                <TableCell className="text-right">{formatNumber(partida.acumuladoActual)}</TableCell>
                <TableCell className="text-right">
                  <Input
                    type="number"
                    min={0}
                    step="0.01"
                    value={avances[partida.id] ?? ""}
                    onChange={(event) => handleAvanceChange(partida.id, event.target.value)}
                    className="max-w-[140px] ml-auto text-right"
                    placeholder="0.00"
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <div className="flex justify-end">
        <Button onClick={handleSubmit} disabled={isSubmitting}>
          {isSubmitting ? "Certificando..." : "Certificar Avance"}
        </Button>
      </div>
    </div>
  );
}
