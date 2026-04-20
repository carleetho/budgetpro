"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

export interface PresupuestoTotalesSummaryProps {
  costoTotal: number;
  precioVenta: number;
}

function formatMoney(value: number): string {
  return Number(value).toLocaleString("es-ES", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

/** Bloque reutilizable de CD / PV para el workspace de presupuesto. */
export function PresupuestoTotalesSummary({ costoTotal, precioVenta }: PresupuestoTotalesSummaryProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Resumen económico</CardTitle>
        <CardDescription>
          Valores calculados según el presupuesto actual. Actualización automática al cargar datos.
        </CardDescription>
      </CardHeader>
      <CardContent className="grid gap-6 sm:grid-cols-2">
        <div className="rounded-lg border bg-muted/30 p-6">
          <p className="text-sm font-medium text-muted-foreground">Costo directo (CD)</p>
          <p className="mt-2 text-2xl font-semibold font-mono tabular-nums">{formatMoney(costoTotal)}</p>
        </div>
        <div className="rounded-lg border bg-muted/30 p-6">
          <p className="text-sm font-medium text-muted-foreground">Precio de venta (PV)</p>
          <p className="mt-2 text-2xl font-semibold font-mono tabular-nums">{formatMoney(precioVenta)}</p>
        </div>
      </CardContent>
    </Card>
  );
}
