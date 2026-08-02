"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { EstimacionService } from "@/services/estimacion.service";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";

type EstimacionRow = {
  id: string;
  numeroEstimacion?: number | string;
  estado?: string;
  montoNetoPagar?: number;
  fechaCorte?: string;
};

export default function EstimacionesPage() {
  const params = useParams();
  const proyectoId = params.id as string;
  const [rows, setRows] = useState<EstimacionRow[]>([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const data = (await EstimacionService.listar(proyectoId)) as EstimacionRow[];
      setRows(data ?? []);
    } catch (e) {
      if (BudgetProApiError.isInstance(e)) toast.error(`[${e.businessCode}] ${e.message}`);
      else toast.error("No se pudieron cargar las estimaciones.");
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [proyectoId]);

  const aprobar = async (id: string) => {
    try {
      await EstimacionService.aprobar(id);
      toast.success("Estimación aprobada.");
      await load();
    } catch (e) {
      if (BudgetProApiError.isInstance(e)) toast.error(`[${e.businessCode}] ${e.message}`);
      else toast.error("No se pudo aprobar.");
    }
  };

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Estimaciones</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Listado y aprobación de valuaciones del proyecto.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Estimaciones registradas</CardTitle>
          <CardDescription>`GET /proyectos/{"{id}"}/estimaciones`</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex items-center gap-2 text-muted-foreground py-8">
              <Loader2 className="h-5 w-5 animate-spin" /> Cargando…
            </div>
          ) : rows.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              No hay estimaciones. Créalas vía API con detalles de avance y PU.
            </p>
          ) : (
            <ul className="divide-y">
              {rows.map((e) => (
                <li key={e.id} className="py-3 flex flex-wrap items-center justify-between gap-2">
                  <div>
                    <div className="font-medium">
                      #{e.numeroEstimacion ?? e.id.slice(0, 8)} — {e.fechaCorte ?? "sin fecha"}
                    </div>
                    <div className="text-sm text-muted-foreground">
                      Neto: {Number(e.montoNetoPagar ?? 0).toLocaleString("es-ES")}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge variant="outline">{e.estado ?? "—"}</Badge>
                    {e.estado !== "APROBADA" && (
                      <Button size="sm" variant="secondary" onClick={() => void aprobar(e.id)}>
                        Aprobar
                      </Button>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
