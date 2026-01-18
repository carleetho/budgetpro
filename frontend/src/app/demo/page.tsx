"use client";

import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { MarketingService } from "@/services/marketing.service";

export default function DemoRequestPage() {
  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = event.currentTarget;
    const formData = new FormData(form);

    try {
      await MarketingService.crearLead({
        nombreContacto: String(formData.get("nombre") || ""),
        email: String(formData.get("correo") || ""),
        telefono: String(formData.get("telefono") || ""),
        nombreEmpresa: String(formData.get("empresa") || ""),
        rol: String(formData.get("cargo") || ""),
      });
      toast.success("Solicitud enviada. Nos pondremos en contacto pronto.");
      form.reset();
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "No se pudo enviar la solicitud. Intenta nuevamente.";
      toast.error(message);
    }
  };

  return (
    <div className="min-h-screen bg-[#f8fafc] px-6 py-12">
      <div className="mx-auto max-w-3xl">
        <Card>
          <CardHeader>
            <CardTitle>Solicitar demo técnica</CardTitle>
            <CardDescription>
              Cuéntanos sobre tu proyecto y coordinaremos una sesión personalizada.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form className="grid gap-6" onSubmit={handleSubmit}>
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="nombre">Nombre completo</Label>
                  <Input id="nombre" name="nombre" placeholder="Nombre y apellido" required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="cargo">Cargo</Label>
                  <Input id="cargo" name="cargo" placeholder="Gerente de proyecto" required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="empresa">Empresa</Label>
                  <Input id="empresa" name="empresa" placeholder="Constructora Andina" required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="correo">Correo corporativo</Label>
                  <Input id="correo" name="correo" type="email" placeholder="tu@empresa.com" required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="telefono">Teléfono</Label>
                  <Input id="telefono" name="telefono" type="tel" placeholder="+51 999 999 999" />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="proyectos">Número de proyectos activos</Label>
                  <Input id="proyectos" name="proyectos" type="number" min={1} placeholder="3" />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="mensaje">Cuéntanos sobre tu reto principal</Label>
                <textarea
                  id="mensaje"
                  name="mensaje"
                  className="min-h-[120px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-xs placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                  placeholder="Qué necesitas auditar, controlar o mejorar en tus obras..."
                />
              </div>
              <div className="flex flex-wrap gap-4">
                <Button type="submit" className="bg-[#1c398e] hover:bg-[#162f73]">
                  Enviar solicitud
                </Button>
                <Link href="/landing" className="text-sm text-muted-foreground underline">
                  Volver a la landing
                </Link>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
