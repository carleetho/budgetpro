/**
 * Stub de módulo aún no implementado (REQ-70).
 */
export function ComingSoonPage({
  title,
  description,
}: {
  title: string;
  description?: string;
}) {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-2 px-6 text-center">
      <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
      <p className="max-w-md text-sm text-muted-foreground">
        {description ?? "Próximamente. Este módulo estará disponible en una próxima entrega."}
      </p>
    </div>
  );
}
