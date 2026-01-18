"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "@/components/ui/button";
import { 
  LayoutDashboard, 
  DollarSign, 
  Calendar, 
  FileText,
  ClipboardCheck,
  ArrowLeft,
  LogOut
} from "lucide-react";
import { cn } from "@/lib/utils";
import { AuthService } from "@/services/auth.service";

interface ProjectSidebarProps {
  proyectoId: string;
}

/**
 * Sidebar lateral para la gestión de un proyecto individual.
 * Muestra enlaces a las diferentes secciones del proyecto.
 */
export function ProjectSidebar({ proyectoId }: ProjectSidebarProps) {
  const pathname = usePathname();

  const menuItems = [
    {
      label: "General",
      href: `/proyectos/${proyectoId}`,
      icon: LayoutDashboard,
    },
    {
      label: "Presupuesto",
      href: `/proyectos/${proyectoId}/presupuesto`,
      icon: DollarSign,
    },
    {
      label: "Producción",
      href: `/proyectos/${proyectoId}/produccion/nuevo`,
      icon: ClipboardCheck,
    },
    {
      label: "Cronograma",
      href: `/proyectos/${proyectoId}/cronograma`,
      icon: Calendar,
    },
    {
      label: "Estimaciones",
      href: `/proyectos/${proyectoId}/estimaciones`,
      icon: FileText,
    },
  ];

  const isActive = (href: string) => {
    if (href === `/proyectos/${proyectoId}`) {
      return pathname === href;
    }
    return pathname.startsWith(href);
  };

  return (
    <aside className="fixed left-0 top-0 h-screen w-64 border-r bg-background flex flex-col z-10">
      {/* Header del Sidebar */}
      <div className="p-4 border-b">
        <Link href="/">
          <Button variant="ghost" size="sm" className="w-full justify-start">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Volver a Proyectos
          </Button>
        </Link>
      </div>

      {/* Menú de Navegación */}
      <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const active = isActive(item.href);
          
          return (
            <Link key={item.href} href={item.href}>
              <Button
                variant={active ? "secondary" : "ghost"}
                className={cn(
                  "w-full justify-start",
                  active && "bg-accent font-medium"
                )}
              >
                <Icon className="h-4 w-4 mr-2" />
                {item.label}
              </Button>
            </Link>
          );
        })}
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
