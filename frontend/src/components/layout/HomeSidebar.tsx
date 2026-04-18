"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "@/components/ui/button";
import { FolderKanban, LogOut } from "lucide-react";
import { cn } from "@/lib/utils";
import { AuthService } from "@/services/auth.service";

/**
 * Barra lateral para la vista global (lista de proyectos en `/`).
 * Mantiene el mismo ancho y estilo que {@link ProjectSidebar} para coherencia visual.
 */
export function HomeSidebar() {
  const pathname = usePathname();
  const enListaProyectos = pathname === "/";

  return (
    <aside className="fixed left-0 top-0 h-screen w-64 border-r bg-background flex flex-col z-10">
      <div className="p-4 border-b">
        <span className="text-sm font-semibold tracking-tight">BudgetPro</span>
        <p className="text-xs text-muted-foreground mt-1">Panel de proyectos</p>
      </div>

      <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
        <Link href="/">
          <Button
            variant={enListaProyectos ? "secondary" : "ghost"}
            className={cn("w-full justify-start", enListaProyectos && "bg-accent font-medium")}
          >
            <FolderKanban className="h-4 w-4 mr-2" />
            Proyectos
          </Button>
        </Link>
      </nav>

      <div className="p-4 border-t">
        <Button
          variant="ghost"
          className="w-full justify-start text-destructive hover:text-destructive"
          onClick={() => {
            AuthService.logout();
            window.location.href = "/login";
          }}
        >
          <LogOut className="h-4 w-4 mr-2" />
          Cerrar sesión
        </Button>
      </div>
    </aside>
  );
}
