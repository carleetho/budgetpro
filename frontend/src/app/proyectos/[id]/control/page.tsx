"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import {
  AlertasService,
  BilleteraService,
  EvmService,
  type AlertaItemDto,
  type EvmSnapshotDto,
  type MovimientoCajaDto,
} from "@/services/evm-alertas-billetera.service";
import { PresupuestoApiService } from "@/services/presupuesto-api.service";
import { getTenantIdForApi } from "@/lib/jwt-tenant";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";

export default function ControlFinancieroPage() {
  const params = useParams();
  const proyectoId = params.id as string;
  const [evm, setEvm] = useState<EvmSnapshotDto | null>(null);
  const [alertas, setAlertas] = useState<AlertaItemDto[]>([]);
  const [billeteraId, setBilleteraId] = useState("");
  const [saldo, setSaldo] = useState<number | null>(null);
  const [movimientos, setMovimientos] = useState<MovimientoCajaDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [monto, setMonto] = useState("");
  const [referencia, setReferencia] = useState("");
  const [tipoMov, setTipoMov] = useState("INGRESO");

  useEffect(() => {
    let cancelled = false;
    void (async () => {
      setLoading(true);
      try {
        const snap = await EvmService.snapshot(proyectoId).catch(() => null);
        if (!cancelled) setEvm(snap);

        const presupuestos = await PresupuestoApiService.listarTodosPorProyecto(
          getTenantIdForApi(),
          proyectoId
        ).catch(() => []);
        const first = presupuestos[0];
        if (first) {
          const analisis = await AlertasService.porPresupuesto(first.id).catch(() => null);
          if (!cancelled) setAlertas(analisis?.alertas ?? []);
        }
      } catch (e) {
        if (BudgetProApiError.isInstance(e)) toast.error(`[${e.businessCode}] ${e.message}`);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [proyectoId]);

  const cargarBilletera = async () => {
    if (!billeteraId.trim()) {
      toast.error("Indica el UUID de billetera.");
      return;
    }
    try {
      const [s, m] = await Promise.all([
        BilleteraService.saldo(billeteraId.trim()),
        BilleteraService.movimientos(billeteraId.trim()),
      ]);
      setSaldo(s.saldoActual ?? null);
      setMovimientos(m ?? []);
    } catch (e) {
      if (BudgetProApiError.isInstance(e)) toast.error(`[${e.businessCode}] ${e.message}`);
      else toast.error("No se pudo cargar la billetera.");
    }
  };

  const crearMovimiento = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!billeteraId.trim()) return;
    try {
      await BilleteraService.crearMovimiento(billeteraId.trim(), {
        monto: Number(monto) || 0,
        moneda: "USD",
        tipo: tipoMov,
        referencia: referencia.trim() || "movimiento-ui",
      });
      toast.success("Movimiento registrado.");
      setMonto("");
      setReferencia("");
      await cargarBilletera();
    } catch (err) {
      if (BudgetProApiError.isInstance(err)) toast.error(`[${err.businessCode}] ${err.message}`);
      else toast.error("No se pudo registrar el movimiento.");
    }
  };

  const metric = (label: string, value: unknown) => (
    <div className="rounded-md border p-3">
      <div className="text-xs text-muted-foreground">{label}</div>
      <div className="text-lg font-semibold font-mono mt-1">
        {value == null || value === "" ? "—" : Number(value).toLocaleString("es-ES", { maximumFractionDigits: 3 })}
      </div>
    </div>
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">EVM, alertas y billetera</h1>
        <p className="text-sm text-muted-foreground mt-1">Control de valor ganado y caja del proyecto.</p>
      </div>

      {loading && (
        <div className="flex items-center gap-2 text-muted-foreground">
          <Loader2 className="h-4 w-4 animate-spin" /> Cargando EVM…
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Snapshot EVM</CardTitle>
          <CardDescription>`GET /evm/{"{proyectoId}"}`</CardDescription>
        </CardHeader>
        <CardContent>
          {!evm ? (
            <p className="text-sm text-muted-foreground">Sin datos EVM (falta baseline o avances).</p>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              {metric("PV", evm.pv)}
              {metric("EV", evm.ev)}
              {metric("AC", evm.ac)}
              {metric("BAC", evm.bac)}
              {metric("CPI", evm.cpi)}
              {metric("SPI", evm.spi)}
              {metric("EAC", evm.eac)}
              {metric("VAC", evm.vac)}
            </div>
          )}
          {evm?.interpretacion && (
            <p className="text-sm text-muted-foreground mt-4">{String(evm.interpretacion)}</p>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Alertas de análisis</CardTitle>
          <CardDescription>`GET /analisis/alertas/{"{presupuestoId}"}`</CardDescription>
        </CardHeader>
        <CardContent>
          {alertas.length === 0 ? (
            <p className="text-sm text-muted-foreground">Sin alertas.</p>
          ) : (
            <ul className="space-y-2">
              {alertas.map((a, i) => (
                <li key={i} className="border rounded-md p-3 text-sm">
                  <div className="flex gap-2 items-center mb-1">
                    <Badge variant="outline">{a.nivel ?? "—"}</Badge>
                    <span className="font-medium">{a.tipoAlerta ?? "Alerta"}</span>
                  </div>
                  <p>{a.mensaje}</p>
                  {a.sugerencia && (
                    <p className="text-muted-foreground text-xs mt-1">{a.sugerencia}</p>
                  )}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Billetera</CardTitle>
          <CardDescription>
            Introduce el UUID de billetera del proyecto (`GET /billeteras/{"{id}"}/saldo`).
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex flex-wrap gap-2 items-end">
            <div className="space-y-1 flex-1 min-w-[240px]">
              <Label>Billetera ID</Label>
              <Input value={billeteraId} onChange={(e) => setBilleteraId(e.target.value)} placeholder="uuid" />
            </div>
            <Button type="button" variant="secondary" onClick={() => void cargarBilletera()}>
              Consultar
            </Button>
          </div>
          {saldo != null && (
            <p className="text-sm">
              Saldo actual:{" "}
              <span className="font-mono font-semibold">{saldo.toLocaleString("es-ES")}</span>
            </p>
          )}
          <form onSubmit={crearMovimiento} className="grid gap-3 sm:grid-cols-4 items-end">
            <div className="space-y-1">
              <Label>Tipo</Label>
              <select
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 text-sm"
                value={tipoMov}
                onChange={(e) => setTipoMov(e.target.value)}
              >
                <option value="INGRESO">INGRESO</option>
                <option value="EGRESO">EGRESO</option>
              </select>
            </div>
            <div className="space-y-1">
              <Label>Monto</Label>
              <Input type="number" value={monto} onChange={(e) => setMonto(e.target.value)} />
            </div>
            <div className="space-y-1">
              <Label>Referencia</Label>
              <Input value={referencia} onChange={(e) => setReferencia(e.target.value)} />
            </div>
            <Button type="submit">Registrar</Button>
          </form>
          <ul className="divide-y max-h-48 overflow-auto">
            {movimientos.map((m) => (
              <li key={m.id} className="py-2 text-sm flex justify-between gap-2">
                <span>
                  {m.tipo} — {m.referencia}
                </span>
                <span className="font-mono">{Number(m.monto).toLocaleString("es-ES")}</span>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}
