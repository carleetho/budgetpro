"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { RrhhService, type CuadrillaDto, type EmpleadoDto } from "@/services/rrhh.service";
import { RrhhFsrConfigService } from "@/services/rrhh-fsr-config.service";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";

export default function RrhhPage() {
  const params = useParams();
  const proyectoId = params.id as string;
  const [empleados, setEmpleados] = useState<EmpleadoDto[]>([]);
  const [cuadrillas, setCuadrillas] = useState<CuadrillaDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [nombre, setNombre] = useState("");
  const [apellido, setApellido] = useState("");
  const [doc, setDoc] = useState("");
  const [salario, setSalario] = useState("1500");
  const [puesto, setPuesto] = useState("Operario");

  const [fsrDiasAguinaldo, setFsrDiasAguinaldo] = useState("30");
  const [fsrVacaciones, setFsrVacaciones] = useState("15");
  const [fsrSegSocial, setFsrSegSocial] = useState("9");

  const load = async () => {
    setLoading(true);
    try {
      const [e, c] = await Promise.all([
        RrhhService.listarEmpleados().catch(() => [] as EmpleadoDto[]),
        RrhhService.listarCuadrillas().catch(() => [] as CuadrillaDto[]),
      ]);
      setEmpleados(e);
      setCuadrillas(c.filter((x) => x.proyectoId === proyectoId || !x.proyectoId));
    } catch (err) {
      if (BudgetProApiError.isInstance(err)) toast.error(`[${err.businessCode}] ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [proyectoId]);

  const crearEmpleado = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await RrhhService.crearEmpleado({
        nombre: nombre.trim(),
        apellido: apellido.trim(),
        numeroIdentificacion: doc.trim(),
        fechaContratacion: new Date().toISOString().slice(0, 10),
        salarioInicial: Number(salario) || 0,
        puestoInicial: puesto.trim() || "Operario",
        tipo: "OBRERO",
      });
      toast.success("Empleado creado.");
      setNombre("");
      setApellido("");
      setDoc("");
      await load();
    } catch (err) {
      if (BudgetProApiError.isInstance(err)) toast.error(`[${err.businessCode}] ${err.message}`);
      else toast.error("No se pudo crear el empleado.");
    } finally {
      setSaving(false);
    }
  };

  const guardarFsr = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await RrhhFsrConfigService.configurarPorProyecto(proyectoId, {
        fechaInicio: new Date().toISOString().slice(0, 10),
        diasAguinaldo: Number(fsrDiasAguinaldo) || 30,
        diasVacaciones: Number(fsrVacaciones) || 15,
        porcentajeSeguridadSocial: Number(fsrSegSocial) || 9,
        diasNoTrabajados: 0,
        diasLaborablesAno: 312,
        factorHorasExtras: 1.5,
        factorTurnoNocturno: 1.25,
        factorRiesgo: 1,
        factorRegional: 1,
      });
      toast.success("Configuración FSR guardada.");
    } catch (err) {
      if (BudgetProApiError.isInstance(err)) toast.error(`[${err.businessCode}] ${err.message}`);
      else toast.error("No se pudo guardar FSR.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">RRHH</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Empleados, cuadrillas y factor salarial (FSR) del proyecto.
        </p>
      </div>

      {loading && (
        <div className="flex items-center gap-2 text-muted-foreground">
          <Loader2 className="h-4 w-4 animate-spin" /> Cargando…
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Nuevo empleado</CardTitle>
          <CardDescription>`POST /rrhh/empleados`</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={crearEmpleado} className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            <div className="space-y-1">
              <Label>Nombre</Label>
              <Input value={nombre} onChange={(e) => setNombre(e.target.value)} required />
            </div>
            <div className="space-y-1">
              <Label>Apellido</Label>
              <Input value={apellido} onChange={(e) => setApellido(e.target.value)} required />
            </div>
            <div className="space-y-1">
              <Label>Documento</Label>
              <Input value={doc} onChange={(e) => setDoc(e.target.value)} required />
            </div>
            <div className="space-y-1">
              <Label>Salario inicial</Label>
              <Input type="number" value={salario} onChange={(e) => setSalario(e.target.value)} />
            </div>
            <div className="space-y-1">
              <Label>Puesto</Label>
              <Input value={puesto} onChange={(e) => setPuesto(e.target.value)} />
            </div>
            <div className="flex items-end">
              <Button type="submit" disabled={saving}>
                Crear
              </Button>
            </div>
          </form>
          <ul className="mt-4 divide-y max-h-56 overflow-auto">
            {empleados.map((emp) => (
              <li key={emp.id} className="py-2 text-sm flex justify-between gap-2">
                <span>
                  {emp.nombre} {emp.apellido}{" "}
                  <span className="text-muted-foreground text-xs">({emp.numeroIdentificacion})</span>
                </span>
                <Badge variant="outline">{emp.estado ?? emp.puestoActual ?? "—"}</Badge>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Cuadrillas</CardTitle>
          <CardDescription>`GET /rrhh/cuadrillas`</CardDescription>
        </CardHeader>
        <CardContent>
          {cuadrillas.length === 0 ? (
            <p className="text-sm text-muted-foreground">Sin cuadrillas para este proyecto.</p>
          ) : (
            <ul className="divide-y">
              {cuadrillas.map((c) => (
                <li key={c.id} className="py-2 text-sm flex justify-between">
                  <span>{c.nombre}</span>
                  <Badge variant="outline">{c.estado ?? "—"}</Badge>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Configuración FSR</CardTitle>
          <CardDescription>`PUT /rrhh/configuracion/proyectos/{"{id}"}`</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={guardarFsr} className="grid gap-3 sm:grid-cols-3 max-w-2xl">
            <div className="space-y-1">
              <Label>Días aguinaldo</Label>
              <Input value={fsrDiasAguinaldo} onChange={(e) => setFsrDiasAguinaldo(e.target.value)} />
            </div>
            <div className="space-y-1">
              <Label>Días vacaciones</Label>
              <Input value={fsrVacaciones} onChange={(e) => setFsrVacaciones(e.target.value)} />
            </div>
            <div className="space-y-1">
              <Label>% seguridad social</Label>
              <Input value={fsrSegSocial} onChange={(e) => setFsrSegSocial(e.target.value)} />
            </div>
            <Button type="submit" disabled={saving}>
              Guardar FSR
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
