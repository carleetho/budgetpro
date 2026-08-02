"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "@/components/ui/button";
import {
  LayoutDashboard,
  DollarSign,
  ClipboardCheck,
  ArrowLeft,
  LogOut,
  CalendarRange,
  FileSpreadsheet,
  ShoppingCart,
  Users,
  LineChart,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { AuthService } from "@/services/auth.service";

interface ProjectSidebarProps {
  proyectoId: string;
}

/**
 * Sidebar lateral para la gestión de un proyecto individual.
 */
export function ProjectSidebar({ proyectoId }: ProjectSidebarProps) {
  const pathname = usePathname();
  const base = `/proyectos/${proyectoId}`;

  const menuItems: {
    label: string;
    href: string;
    icon: typeof LayoutDashboard;
    isActiveOverride?: (pathname: string) => boolean;
  }[] = [
    {
      label: "General",
      href: base,
      icon: LayoutDashboard,
    },
    {
      label: "Presupuestos",
      href: `${base}#presupuestos`,
      icon: DollarSign,
      isActiveOverride: (p) => p.startsWith(`${base}/presupuestos`),
    },
    {
      label: "Producción",
      href: `${base}/produccion/nuevo`,
      icon: ClipboardCheck,
    },
    {
      label: "Estimaciones",
      href: `${base}/estimaciones`,
      icon: FileSpreadsheet,
    },
    {
      label: "Cronograma",
      href: `${base}/cronograma`,
      icon: CalendarRange,
    },
    {
      label: "Compras",
      href: `${base}/compras`,
      icon: ShoppingCart,
    },
    {
      label: "RRHH",
      href: `${base}/rrhh`,
      icon: Users,
    },
    {
      label: "EVM / Caja",
      href: `${base}/control`,
      icon: LineChart,
    },
  ];

  const isActive = (href: string) => {
    if (href === base) {
      return pathname === href;
    }
    return pathname.startsWith(href);
  };

  return (
    <aside className="fixed left-0 top-0 h-screen w-64 border-r bg-background flex flex-col z-10">
      <div className="p-4 border-b">
        <Link href="/">
          <Button variant="ghost" size="sm" className="w-full justify-start">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Volver a Proyectos
          </Button>
        </Link>
      </div>

      <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const active = item.isActiveOverride
            ? item.isActiveOverride(pathname)
            : isActive(item.href);

          return (
            <Link key={item.href} href={item.href}>
              <Button
                variant={active ? "secondary" : "ghost"}
                className={cn("w-full justify-start", active && "bg-accent font-medium")}
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
