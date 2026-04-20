"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  AlertTriangle,
  Building2,
  CheckCircle2,
  Circle,
  HelpCircle,
  ImageIcon,
  Loader2,
  Save,
} from "lucide-react";
import { toast } from "sonner";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { Proyecto } from "@/core/types";
import type { EstadoPresupuestoRest } from "@/core/types/presupuesto-contract";
import {
  defaultParametrosDraft,
  loadParametrosDraft,
  saveParametrosDraft,
  type PresupuestoParametrosDraft,
  type TipoApuCabecera,
} from "@/modules/presupuestos/types/presupuesto-parametros-draft";
import { cn } from "@/lib/utils";

/** Demo: grupo → subgrupos de catálogo de obra. */
const CATALOGO_GRUPOS: Record<string, string[]> = {
  "": [],
  viales: ["Puente", "Pavimento", "Movimiento de tierras"],
  edificaciones: ["Estructura", "Arquitectura", "Instalaciones"],
};

export interface PresupuestoParametrosFormProps {
  presupuestoId: string;
  proyectoId: string;
  nombrePresupuestoApi: string;
  estado: EstadoPresupuestoRest;
  precioVentaApi: number;
  proyecto: Proyecto | null;
  proyectoLoading?: boolean;
  readOnly: boolean;
}

function previewDecimal(decimals: number): string {
  const n = 123.456789;
  return n.toLocaleString("es-PE", {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

export function PresupuestoParametrosForm({
  presupuestoId,
  proyectoId,
  nombrePresupuestoApi,
  estado,
  precioVentaApi,
  proyecto,
  proyectoLoading,
  readOnly,
}: PresupuestoParametrosFormProps) {
  const [draft, setDraft] = useState<PresupuestoParametrosDraft>(() =>
    defaultParametrosDraft({
      nombrePresupuesto: nombrePresupuestoApi,
    })
  );
  const [hydrated, setHydrated] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const stored = loadParametrosDraft(presupuestoId);
    if (stored) {
      setDraft(() => ({
        ...defaultParametrosDraft({ nombrePresupuesto: nombrePresupuestoApi }),
        ...stored,
        nombrePresupuesto: stored.nombrePresupuesto || nombrePresupuestoApi,
      }));
    } else {
      setDraft((d) => ({ ...d, nombrePresupuesto: nombrePresupuestoApi }));
    }
    setHydrated(true);
  }, [presupuestoId, nombrePresupuestoApi]);

  const migasCatalogo = useMemo(() => {
    const g = draft.catalogoGrupo;
    const s = draft.catalogoSubgrupo;
    if (!g) {
      return "Sin selección";
    }
    const gl =
      g === "viales"
        ? "Obras viales"
        : g === "edificaciones"
          ? "Edificaciones"
          : g;
    return s ? `${gl} › ${s}` : gl;
  }, [draft.catalogoGrupo, draft.catalogoSubgrupo]);

  const subgrupos = CATALOGO_GRUPOS[draft.catalogoGrupo] ?? [];

  const handleGuardarBorrador = useCallback(() => {
    if (readOnly) {
      return;
    }
    setSaving(true);
    try {
      saveParametrosDraft(presupuestoId, draft);
      toast.success(
        "Borrador guardado en este navegador. Cuando exista API, se sincronizará con el servidor."
      );
    } finally {
      setSaving(false);
    }
  }, [draft, presupuestoId, readOnly]);

  const onChangeFechaDistrito = (next: Partial<PresupuestoParametrosDraft>) => {
    setDraft((prev) => {
      const fechaChanged =
        next.fechaElaboracion !== undefined && next.fechaElaboracion !== prev.fechaElaboracion;
      const lugarChanged =
        next.distritoTexto !== undefined && next.distritoTexto !== prev.distritoTexto;
      let historico = prev.historicoFechaLugar;
      let invalidado = prev.preciosContextoInvalidado;
      if (fechaChanged || lugarChanged) {
        invalidado = true;
        const lugar = next.distritoTexto ?? prev.distritoTexto;
        const fecha = next.fechaElaboracion ?? prev.fechaElaboracion;
        if (lugar || fecha) {
          historico = [{ fecha, lugar: lugar || "—" }, ...historico].slice(0, 5);
        }
      }
      return { ...prev, ...next, historicoFechaLugar: historico, preciosContextoInvalidado: invalidado };
    });
  };

  if (!hydrated) {
    return (
      <div className="flex items-center gap-2 py-12 text-muted-foreground">
        <Loader2 className="h-6 w-6 animate-spin" />
        Cargando borrador…
      </div>
    );
  }

  const clienteNombre = proyecto?.clienteNombre?.trim();
  const tieneClienteCatalogo = Boolean(clienteNombre);

  return (
    <div className="space-y-6 pb-28">
      {/* Barra de estado global */}
      <div className="flex flex-wrap items-center gap-2 border-b pb-4">
        <Badge variant="outline" className="gap-1">
          Estado: <span className="font-semibold">{estado}</span>
        </Badge>
        <Badge
          variant={draft.preciosContextoInvalidado ? "destructive" : "secondary"}
          className="gap-1"
        >
          Precios:{" "}
          {draft.preciosContextoInvalidado
            ? "contexto cambiado (reprocesar)"
            : "sin cambio de fecha/lugar"}
        </Badge>
        {draft.requiereFormulaPolinomica && (
          <Badge variant="outline" className="border-amber-500/60 text-amber-800 dark:text-amber-300">
            Fórmula polinómica: pendiente tras procesar
          </Badge>
        )}
      </div>

      {!tieneClienteCatalogo && (
        <Alert variant="destructive">
          <AlertTriangle className="h-4 w-4" />
          <AlertTitle>Cliente del proyecto no registrado</AlertTitle>
          <AlertDescription className="flex flex-wrap items-center gap-3">
            Completa el cliente en la ficha del proyecto cuando la API exponga el vínculo al catálogo de
            identificadores.
            <Button variant="secondary" size="sm" asChild>
              <Link href={`/proyectos/${proyectoId}`}>Ir al proyecto</Link>
            </Button>
          </AlertDescription>
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-5">
        {/* Columna principal ~60% */}
        <div className="space-y-6 lg:col-span-3">
          <Card id="bloque-identidad">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-lg">
                <Building2 className="h-5 w-5" />
                Identidad
              </CardTitle>
              <CardDescription>Nombre, subpresupuesto y propietario (referencia proyecto).</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-4 sm:grid-cols-1">
              <div className="space-y-2">
                <div className="flex justify-between">
                  <Label htmlFor="nombre-pres">Nombre del presupuesto</Label>
                  <span className="text-xs text-muted-foreground">
                    {draft.nombrePresupuesto.length}/200
                  </span>
                </div>
                <Input
                  id="nombre-pres"
                  maxLength={200}
                  value={draft.nombrePresupuesto}
                  onChange={(e) => setDraft((d) => ({ ...d, nombrePresupuesto: e.target.value }))}
                  disabled={readOnly}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="subpre">Subpresupuesto</Label>
                <Select
                  value={draft.subpresupuestoNombre}
                  onValueChange={(v) => setDraft((d) => ({ ...d, subpresupuestoNombre: v }))}
                  disabled={readOnly}
                >
                  <SelectTrigger id="subpre">
                    <SelectValue placeholder="Especialidad" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Principal">Principal</SelectItem>
                    <SelectItem value="Estructuras">Estructuras</SelectItem>
                    <SelectItem value="Eléctricas">Eléctricas</SelectItem>
                    <SelectItem value="Instalaciones sanitarias">Instalaciones sanitarias</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Propietario / obra (solo lectura)</Label>
                <div className="rounded-md border bg-muted/40 px-3 py-2 text-sm">
                  {proyectoLoading ? (
                    "Cargando proyecto…"
                  ) : proyecto ? (
                    <>
                      <span className="font-medium">{proyecto.nombre}</span>
                      {proyecto.ubicacion ? (
                        <span className="text-muted-foreground"> — {proyecto.ubicacion}</span>
                      ) : null}
                    </>
                  ) : (
                    <span className="text-muted-foreground">No se cargó el proyecto.</span>
                  )}
                  <Button variant="link" size="sm" className="h-auto p-0 pl-2" asChild>
                    <Link href={`/proyectos/${proyectoId}`}>Editar en ficha del proyecto</Link>
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card id="bloque-catalogo">
            <CardHeader>
              <CardTitle className="text-lg">Catálogo de presupuesto</CardTitle>
              <CardDescription>
                Clasificación por grupo y subgrupo (listado alfabético y recientes).
              </CardDescription>
            </CardHeader>
            <CardContent className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label>Grupo</Label>
                <Select
                  value={draft.catalogoGrupo || "none"}
                  onValueChange={(v) => {
                    const val = v === "none" ? "" : v;
                    setDraft((d) => ({
                      ...d,
                      catalogoGrupo: val,
                      catalogoSubgrupo: "",
                    }));
                  }}
                  disabled={readOnly}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione grupo" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">—</SelectItem>
                    <SelectItem value="viales">Obras viales</SelectItem>
                    <SelectItem value="edificaciones">Edificaciones</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Subgrupo</Label>
                <Select
                  value={draft.catalogoSubgrupo || "none"}
                  onValueChange={(v) =>
                    setDraft((d) => ({
                      ...d,
                      catalogoSubgrupo: v === "none" ? "" : v,
                    }))
                  }
                  disabled={readOnly || subgrupos.length === 0}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Subgrupo" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">—</SelectItem>
                    {subgrupos.map((s) => (
                      <SelectItem key={s} value={s}>
                        {s}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="sm:col-span-2 rounded-md bg-muted/50 px-3 py-2 text-sm">
                <span className="text-muted-foreground">Ruta seleccionada: </span>
                <span className="font-medium">{migasCatalogo}</span>
              </div>
            </CardContent>
          </Card>

          <Card id="bloque-apu">
            <CardHeader>
              <CardTitle className="text-lg">APU y decimales</CardTitle>
              <CardDescription>Decimales en metrados, tipo de cálculo APU y fórmula polinómica.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label>Decimales en metrados</Label>
                <Select
                  value={String(draft.decimalesMetrados)}
                  onValueChange={(v) =>
                    setDraft((d) => ({ ...d, decimalesMetrados: Number.parseInt(v, 10) }))
                  }
                  disabled={readOnly}
                >
                  <SelectTrigger className="w-[120px]">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {[0, 1, 2, 3, 4].map((n) => (
                      <SelectItem key={n} value={String(n)}>
                        {n}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <p className="text-xs text-muted-foreground">
                  Vista previa: {previewDecimal(draft.decimalesMetrados)}
                </p>
              </div>
              <div className="space-y-2">
                <Label>Tipo de APU</Label>
                <div className="flex flex-col gap-3 sm:flex-row">
                  <label
                    className={cn(
                      "flex cursor-pointer items-start gap-2 rounded-lg border p-3",
                      draft.tipoApu === "EDIFICACIONES" && "border-primary bg-primary/5"
                    )}
                  >
                    <input
                      type="radio"
                      name="tipoApu"
                      className="mt-1"
                      checked={draft.tipoApu === "EDIFICACIONES"}
                      onChange={() =>
                        setDraft((d) => ({ ...d, tipoApu: "EDIFICACIONES" as TipoApuCabecera }))
                      }
                      disabled={readOnly}
                    />
                    <span>
                      <span className="font-medium">Edificaciones</span>
                      <span className="block text-xs text-muted-foreground">
                        Rendimientos estándar en APU.
                      </span>
                    </span>
                  </label>
                  <label
                    className={cn(
                      "flex cursor-pointer items-start gap-2 rounded-lg border p-3",
                      draft.tipoApu === "CARRETERAS" && "border-primary bg-primary/5"
                    )}
                  >
                    <input
                      type="radio"
                      name="tipoApu"
                      className="mt-1"
                      checked={draft.tipoApu === "CARRETERAS"}
                      onChange={() =>
                        setDraft((d) => ({ ...d, tipoApu: "CARRETERAS" as TipoApuCabecera }))
                      }
                      disabled={readOnly}
                    />
                    <span>
                      <span className="font-medium">Carreteras / alto volumen</span>
                      <span className="block text-xs text-muted-foreground">
                        Movimiento de tierras y grandes volúmenes; obras de arte en otro presupuesto tipo
                        edificaciones.
                      </span>
                    </span>
                  </label>
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-3 rounded-lg border p-3">
                <input
                  id="poli"
                  type="checkbox"
                  className="size-4 rounded border-input"
                  checked={draft.requiereFormulaPolinomica}
                  onChange={(e) =>
                    setDraft((d) => ({ ...d, requiereFormulaPolinomica: e.target.checked }))
                  }
                  disabled={readOnly}
                />
                <Label htmlFor="poli" className="cursor-pointer font-normal">
                  Requiere fórmula polinómica
                </Label>
              </div>
              {draft.requiereFormulaPolinomica && (
                <Alert className="border-amber-500/50 bg-amber-50 dark:bg-amber-950/30">
                  <AlertTriangle className="h-4 w-4 text-amber-700" />
                  <AlertTitle className="text-amber-900 dark:text-amber-200">
                    Tras procesar el presupuesto
                  </AlertTitle>
                  <AlertDescription className="text-amber-900/90 dark:text-amber-200/90">
                    Complete la fórmula polinómica. El sistema puede marcar ítems en ámbar hasta cerrar ese
                    requisito.
                  </AlertDescription>
                </Alert>
              )}
            </CardContent>
          </Card>

          <Card id="bloque-cliente">
            <CardHeader>
              <CardTitle className="text-lg">Cliente del proyecto</CardTitle>
              <CardDescription>
                En BudgetPro el alta del cliente es en Proyecto; aquí solo referencia visual.
              </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-wrap items-center gap-2">
              <span className="text-sm">
                Cliente del proyecto:{" "}
                <strong>{clienteNombre || "Pendiente (definir en proyecto)"}</strong>
              </span>
              {tieneClienteCatalogo && (
                <Badge variant="secondary" className="gap-1">
                  <CheckCircle2 className="h-3 w-3" />
                  Catálogo OK
                </Badge>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Columna derecha */}
        <div className="space-y-6 lg:col-span-2">
          <Card id="bloque-ubicacion">
            <CardHeader>
              <CardTitle className="text-lg">Ubicación y vigencia</CardTitle>
              <CardDescription>Precios según fecha y ubicación de la obra.</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-4">
              <div className="space-y-2">
                <Label htmlFor="distrito">Distrito / ubicación</Label>
                <Input
                  id="distrito"
                  placeholder="Ej. Urubamba — Cusco"
                  value={draft.distritoTexto}
                  onChange={(e) => onChangeFechaDistrito({ distritoTexto: e.target.value })}
                  disabled={readOnly}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="fecha">Fecha de elaboración</Label>
                <Input
                  id="fecha"
                  type="date"
                  value={draft.fechaElaboracion}
                  onChange={(e) => onChangeFechaDistrito({ fechaElaboracion: e.target.value })}
                  disabled={readOnly}
                />
                <p className="text-xs text-muted-foreground">
                  Los precios del catálogo se resuelven contra la fecha y el lugar configurados.
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="plazo">Plazo (días calendario)</Label>
                <Input
                  id="plazo"
                  type="number"
                  min={0}
                  value={draft.plazoDias}
                  onChange={(e) =>
                    setDraft((d) => ({ ...d, plazoDias: Number.parseInt(e.target.value, 10) || 0 }))
                  }
                  disabled={readOnly}
                />
                <p className="text-xs text-muted-foreground">
                  Informativo; el plazo contractual final puede venir del cronograma.
                </p>
              </div>
              <div className="space-y-2">
                <div className="flex items-center gap-1">
                  <Label htmlFor="jornada">Jornada diaria (h)</Label>
                  <span title="Influye en rendimiento HH/HM en APU" className="text-muted-foreground">
                    <HelpCircle className="h-3.5 w-3.5" />
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    size="icon"
                    disabled={readOnly || draft.jornadaDiaria <= 1}
                    onClick={() =>
                      setDraft((d) => ({
                        ...d,
                        jornadaDiaria: Math.max(1, d.jornadaDiaria - 0.5),
                      }))
                    }
                  >
                    −
                  </Button>
                  <Input
                    id="jornada"
                    type="number"
                    step={0.5}
                    min={1}
                    max={24}
                    className="w-24 text-center"
                    value={draft.jornadaDiaria}
                    onChange={(e) =>
                      setDraft((d) => ({
                        ...d,
                        jornadaDiaria: Number.parseFloat(e.target.value) || 8,
                      }))
                    }
                    disabled={readOnly}
                  />
                  <Button
                    type="button"
                    variant="outline"
                    size="icon"
                    disabled={readOnly || draft.jornadaDiaria >= 24}
                    onClick={() =>
                      setDraft((d) => ({
                        ...d,
                        jornadaDiaria: Math.min(24, d.jornadaDiaria + 0.5),
                      }))
                    }
                  >
                    +
                  </Button>
                </div>
                <button
                  type="button"
                  className="text-xs text-primary underline-offset-4 hover:underline"
                >
                  Afecta rendimiento APU
                </button>
              </div>
            </CardContent>
          </Card>

          <Card id="bloque-monedas">
            <CardHeader>
              <CardTitle className="text-lg">Monedas y conversión</CardTitle>
              <CardDescription>Doble moneda y factor para reportes.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label>Moneda base</Label>
                <Select
                  value={draft.monedaBase}
                  onValueChange={(v) => setDraft((d) => ({ ...d, monedaBase: v }))}
                  disabled={readOnly}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="PEN">Soles (PEN)</SelectItem>
                    <SelectItem value="USD">Dólares (USD)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="flex flex-wrap items-center gap-3 rounded-lg border p-3">
                <input
                  id="doble"
                  type="checkbox"
                  className="size-4 rounded border-input"
                  checked={draft.dobleMoneda}
                  onChange={(e) => setDraft((d) => ({ ...d, dobleMoneda: e.target.checked }))}
                  disabled={readOnly}
                />
                <Label htmlFor="doble" className="cursor-pointer font-normal">
                  Trabajar con segunda moneda en insumos / reportes
                </Label>
              </div>
              {draft.dobleMoneda && (
                <>
                  <div className="space-y-2">
                    <Label>Moneda alterna</Label>
                    <Select
                      value={draft.monedaAlterna || "USD"}
                      onValueChange={(v) => setDraft((d) => ({ ...d, monedaAlterna: v }))}
                      disabled={readOnly}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="USD">USD</SelectItem>
                        <SelectItem value="PEN">PEN</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="factor">Factor 1 / tipo de cambio (opcional)</Label>
                    <Input
                      id="factor"
                      inputMode="decimal"
                      placeholder="Ej. 3.75"
                      value={draft.factorCambio}
                      onChange={(e) => setDraft((d) => ({ ...d, factorCambio: e.target.value }))}
                      disabled={readOnly}
                    />
                    <p className="text-xs text-muted-foreground">
                      Para imprimir en moneda alterna según configuración de reporte (no sustituye
                      opciones de impresión).
                    </p>
                    <Button variant="link" className="h-auto p-0 text-xs" type="button" disabled>
                      Opciones de impresión (próximamente)
                    </Button>
                  </div>
                </>
              )}
            </CardContent>
          </Card>

          <Card id="bloque-totales">
            <CardHeader>
              <CardTitle className="text-lg">Totales informativos</CardTitle>
              <CardDescription>Base (licitación) y oferta (procesado).</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="pbase">Presupuesto base (referencia licitación)</Label>
                <Input
                  id="pbase"
                  inputMode="decimal"
                  placeholder="Opcional"
                  value={draft.presupuestoBaseReferencia}
                  onChange={(e) =>
                    setDraft((d) => ({ ...d, presupuestoBaseReferencia: e.target.value }))
                  }
                  disabled={readOnly}
                />
              </div>
              <div className="space-y-2">
                <Label>Presupuesto oferta (solo lectura)</Label>
                <div className="rounded-lg border bg-muted/30 px-4 py-6 text-center text-2xl font-semibold tracking-tight">
                  {Number(precioVentaApi).toLocaleString("es-PE", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })}{" "}
                  <span className="text-base font-normal text-muted-foreground">
                    ({draft.monedaBase})
                  </span>
                </div>
                <p className="text-xs text-muted-foreground text-center">
                  Valor desde API (`precioVenta`). Se actualiza al procesar la hoja en backend.
                </p>
              </div>
            </CardContent>
          </Card>

          <Card id="bloque-extra">
            <CardHeader>
              <CardTitle className="text-lg">Subpresupuesto — histórico y logotipo</CardTitle>
              <CardDescription>Histórico de contexto y marca para informes.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {draft.preciosContextoInvalidado && (
                <Alert variant="destructive">
                  <AlertTriangle className="h-4 w-4" />
                  <AlertTitle>Cambio de fecha o lugar</AlertTitle>
                  <AlertDescription>
                    Si cambias fecha o ubicación, los precios pueden reiniciarse hasta reprocesar desde la hoja
                    del presupuesto.
                  </AlertDescription>
                </Alert>
              )}
              <div>
                <Label className="mb-2 block">Últimos contextos fecha / lugar</Label>
                {draft.historicoFechaLugar.length === 0 ? (
                  <p className="text-sm text-muted-foreground">Sin movimientos registrados en borrador.</p>
                ) : (
                  <ul className="max-h-36 space-y-1 overflow-auto rounded-md border text-sm">
                    {draft.historicoFechaLugar.map((h, i) => (
                      <li key={`${h.fecha}-${h.lugar}-${i}`} className="border-b px-3 py-2 last:border-0">
                        <span className="font-mono text-xs">{h.fecha}</span> — {h.lugar}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
              <div className="space-y-2">
                <Label>Logotipo en informes</Label>
                <div className="flex cursor-not-allowed flex-col items-center justify-center rounded-lg border border-dashed bg-muted/20 py-8 text-muted-foreground">
                  <ImageIcon className="mb-2 h-10 w-10 opacity-50" />
                  <span className="text-sm">Arrastrar imagen (mock UI)</span>
                  <div className="mt-3 flex items-center gap-2">
                    <input
                      id="logo"
                      type="checkbox"
                      className="size-4 rounded border-input"
                      checked={draft.tieneLogotipo}
                      onChange={(e) =>
                        setDraft((d) => ({ ...d, tieneLogotipo: e.target.checked }))
                      }
                      disabled={readOnly}
                    />
                    <Label htmlFor="logo" className="cursor-pointer font-normal">
                      Marcar como con logotipo asignado
                    </Label>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-medium">Lista de comprobación</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <ChecklistRow ok={Boolean(draft.catalogoGrupo)} label="Catálogo seleccionado" />
              <ChecklistRow ok={draft.tipoApu !== undefined} label="Tipo APU definido" />
              <ChecklistRow ok={tieneClienteCatalogo} label="Referencia de proyecto / cliente" />
              <ChecklistRow ok={Boolean(draft.fechaElaboracion)} label="Fecha de elaboración" />
              <ChecklistRow ok={Boolean(draft.distritoTexto.trim())} label="Distrito / ubicación" />
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Barra sticky guardar */}
      <div
        className={cn(
          "fixed inset-x-0 bottom-0 z-40 border-t bg-background/95 py-3 backdrop-blur supports-[backdrop-filter]:bg-background/80",
          "md:left-64"
        )}
      >
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-4">
          <p className="text-xs text-muted-foreground">
            Guardar persiste solo el <strong>borrador local</strong>. Endpoint REST de cabecera pendiente.
          </p>
          <Button type="button" onClick={() => void handleGuardarBorrador()} disabled={readOnly || saving}>
            {saving ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Save className="mr-2 h-4 w-4" />}
            Guardar borrador
          </Button>
        </div>
      </div>
    </div>
  );
}

function ChecklistRow({ ok, label }: { ok: boolean; label: string }) {
  return (
    <div className="flex items-center gap-2">
      {ok ? (
        <CheckCircle2 className="h-4 w-4 shrink-0 text-green-600 dark:text-green-400" />
      ) : (
        <Circle className="h-4 w-4 shrink-0 text-muted-foreground opacity-50" />
      )}
      <span className={ok ? "" : "text-muted-foreground"}>{label}</span>
    </div>
  );
}
