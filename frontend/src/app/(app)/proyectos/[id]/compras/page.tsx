"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { ComprasService, type OrdenCompraDto, type ProveedorDto } from "@/services/compras.service";
import { InventarioService, type InventarioItemDto } from "@/services/inventario.service";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";

export default function ComprasInventarioPage() {
  const params = useParams();
  const proyectoId = params.id as string;
  const [proveedores, setProveedores] = useState<ProveedorDto[]>([]);
  const [ordenes, setOrdenes] = useState<OrdenCompraDto[]>([]);
  const [inventario, setInventario] = useState<InventarioItemDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [razonSocial, setRazonSocial] = useState("");
  const [ruc, setRuc] = useState("");
  const [saving, setSaving] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const [p, o, inv] = await Promise.all([
        ComprasService.listarProveedores().catch(() => [] as ProveedorDto[]),
        ComprasService.listarOrdenes(proyectoId).catch(() => [] as OrdenCompraDto[]),
        InventarioService.listarPorProyecto(proyectoId).catch(() => [] as InventarioItemDto[]),
      ]);
      setProveedores(p);
      setOrdenes(o);
      setInventario(inv);
    } catch (e) {
      if (BudgetProApiError.isInstance(e)) toast.error(`[${e.businessCode}] ${e.message}`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [proyectoId]);

  const crearProveedor = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!razonSocial.trim() || !ruc.trim()) {
      toast.error("Razón social y RUC son obligatorios.");
      return;
    }
    setSaving(true);
    try {
      await ComprasService.crearProveedor({ razonSocial: razonSocial.trim(), ruc: ruc.trim() });
      toast.success("Proveedor creado.");
      setRazonSocial("");
      setRuc("");
      await load();
    } catch (err) {
      if (BudgetProApiError.isInstance(err)) toast.error(`[${err.businessCode}] ${err.message}`);
      else toast.error("No se pudo crear el proveedor.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Compras e inventario</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Proveedores, órdenes de compra e inventario del proyecto.
        </p>
      </div>

      {loading && (
        <div className="flex items-center gap-2 text-muted-foreground">
          <Loader2 className="h-4 w-4 animate-spin" /> Cargando…
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Nuevo proveedor</CardTitle>
          <CardDescription>`POST /proveedores`</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={crearProveedor} className="flex flex-wrap gap-3 items-end">
            <div className="space-y-1">
              <Label>Razón social</Label>
              <Input value={razonSocial} onChange={(e) => setRazonSocial(e.target.value)} />
            </div>
            <div className="space-y-1">
              <Label>RUC</Label>
              <Input value={ruc} onChange={(e) => setRuc(e.target.value)} />
            </div>
            <Button type="submit" disabled={saving}>
              Crear
            </Button>
          </form>
          <ul className="mt-4 divide-y max-h-48 overflow-auto">
            {proveedores.map((p) => (
              <li key={p.id} className="py-2 text-sm flex justify-between gap-2">
                <span>
                  {p.razonSocial} <span className="text-muted-foreground font-mono text-xs">({p.ruc})</span>
                </span>
                <Badge variant="outline">{p.estado ?? "—"}</Badge>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Órdenes de compra</CardTitle>
          <CardDescription>`GET /ordenes-compra?proyectoId=`</CardDescription>
        </CardHeader>
        <CardContent>
          {ordenes.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              Sin OC. Crea con `POST /ordenes-compra` (proveedor + detalles).
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Número</TableHead>
                  <TableHead>Proveedor</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead className="text-right">Total</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {ordenes.map((o) => (
                  <TableRow key={o.id}>
                    <TableCell>{o.numero ?? o.id.slice(0, 8)}</TableCell>
                    <TableCell>{o.proveedor?.razonSocial ?? "—"}</TableCell>
                    <TableCell>
                      <Badge variant="outline">{o.estado ?? "—"}</Badge>
                    </TableCell>
                    <TableCell className="text-right font-mono">
                      {Number(o.montoTotal ?? 0).toLocaleString("es-ES")}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Inventario</CardTitle>
          <CardDescription>`GET /proyectos/{"{id}"}/inventario`</CardDescription>
        </CardHeader>
        <CardContent>
          {inventario.length === 0 ? (
            <p className="text-sm text-muted-foreground">Sin stock registrado.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Recurso</TableHead>
                  <TableHead>Ubicación</TableHead>
                  <TableHead className="text-right">Cantidad</TableHead>
                  <TableHead className="text-right">Costo prom.</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {inventario.map((i) => (
                  <TableRow key={i.id}>
                    <TableCell className="font-mono text-xs">{i.recursoId.slice(0, 8)}…</TableCell>
                    <TableCell>{i.ubicacion ?? "—"}</TableCell>
                    <TableCell className="text-right">{i.cantidadFisica}</TableCell>
                    <TableCell className="text-right">{Number(i.costoPromedio ?? 0).toLocaleString("es-ES")}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
