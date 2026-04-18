import { redirect } from "next/navigation";

/**
 * Compatibilidad: enlaces antiguos o login que apuntaban a `/proyectos` (sin id).
 * La lista canónica vive en `/`.
 */
export default function ProyectosIndexRedirectPage() {
  redirect("/");
}
