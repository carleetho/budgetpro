"use client";

import { useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { API_BASE_URL } from "@/core/config/env";

const demoSchema = z.object({
  nombre: z.string().min(1, "El nombre es obligatorio"),
  email: z.string().email("El email no es válido"),
  telefono: z.string().min(1, "El teléfono es obligatorio"),
  empresa: z.string().min(1, "La empresa es obligatoria"),
  cargo: z.string().min(1, "El cargo es obligatorio"),
});

type DemoFormValues = z.infer<typeof demoSchema>;

export default function DemoForm() {
  const [isSuccess, setIsSuccess] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<DemoFormValues>({
    resolver: zodResolver(demoSchema),
  });

  const endpoint = useMemo(() => {
    const root = API_BASE_URL.replace(/\/api\/v1\/?$/, "");
    return `${root}/api/public/v1/demo-request`;
  }, []);

  const onSubmit = async (values: DemoFormValues) => {
    setSubmitError(null);
    try {
      const response = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          nombreContacto: values.nombre,
          email: values.email,
          telefono: values.telefono,
          nombreEmpresa: values.empresa,
          rol: values.cargo,
        }),
      });

      if (!response.ok) {
        throw new Error("No se pudo enviar la solicitud. Intenta nuevamente.");
      }

      setIsSuccess(true);
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "No se pudo enviar la solicitud. Intenta nuevamente.";
      setSubmitError(message);
    }
  };

  if (isSuccess) {
    return (
      <Card className="bg-white/95">
        <CardHeader>
          <CardTitle>Solicitud enviada</CardTitle>
          <CardDescription>
            ¡Gracias! Un especialista en ingeniería de costos te contactará en breve.
          </CardDescription>
        </CardHeader>
      </Card>
    );
  }

  return (
    <Card className="bg-white/95">
      <CardHeader>
        <CardTitle>Solicitar demo</CardTitle>
        <CardDescription>Déjanos tus datos y coordinamos una sesión técnica.</CardDescription>
      </CardHeader>
      <CardContent>
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-2">
            <Label htmlFor="nombre">Nombre</Label>
            <Input id="nombre" placeholder="Nombre y apellido" {...register("nombre")} />
            {errors.nombre && <p className="text-sm text-destructive">{errors.nombre.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="email">Email corporativo</Label>
            <Input id="email" type="email" placeholder="tu@empresa.com" {...register("email")} />
            {errors.email && <p className="text-sm text-destructive">{errors.email.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="telefono">Teléfono</Label>
            <Input id="telefono" placeholder="+51 999 999 999" {...register("telefono")} />
            {errors.telefono && <p className="text-sm text-destructive">{errors.telefono.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="empresa">Empresa</Label>
            <Input id="empresa" placeholder="Constructora Andina" {...register("empresa")} />
            {errors.empresa && <p className="text-sm text-destructive">{errors.empresa.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="cargo">Cargo</Label>
            <Input id="cargo" placeholder="Gerente de Proyectos" {...register("cargo")} />
            {errors.cargo && <p className="text-sm text-destructive">{errors.cargo.message}</p>}
          </div>

          {submitError && <p className="text-sm text-destructive">{submitError}</p>}

          <Button type="submit" className="w-full bg-[#1c398e] hover:bg-[#162f73]" disabled={isSubmitting}>
            {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Solicitar demo
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
