import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import RPCForm from "./RPCForm";

interface ProduccionNuevoPageProps {
  params: { id: string };
}

/**
 * Página para crear un nuevo Reporte de Producción (RPC).
 */
export default function ProduccionNuevoPage({ params }: ProduccionNuevoPageProps) {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Reporte de Producción</h1>
        <p className="text-muted-foreground mt-2">
          Registra el avance diario por partida y certifica la producción de campo.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Registro de Avances</CardTitle>
          <CardDescription>
            Ingresa el avance de hoy por partida y certifica la ejecución.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <RPCForm proyectoId={params.id} />
        </CardContent>
      </Card>
    </div>
  );
}
