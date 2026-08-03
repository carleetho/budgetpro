"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "@/components/ui/button";
import {
  LayoutDashboard,
  DollarSign,
  ClipboardCheck,
  CalendarRange,
  FileSpreadsheet,
  ShoppingCart,
  Users,
  LineChart,
} from "lucide-react";
import { cn } from "@/lib/utils";

interface ProjectSubnavProps {
  proyectoId: string;
}

/**
 * Subnavegación del hub de proyecto (complementa el AppShell global de REQ-70).
 */
export function ProjectSubnav({ proyectoId }: ProjectSubnavProps) {
  const pathname = usePathname();
  const base = `/proyectos/${proyectoId}`;

  const menuItems: {
    label: string;
    href: string;
    icon: typeof LayoutDashboard;
    isActiveOverride?: (pathname: string) => boolean;
  }[] = [
    { label: "General", href: base, icon: LayoutDashboard },
    {
      label: "Presupuestos",
      href: `${base}#presupuestos`,
      icon: DollarSign,
      isActiveOverride: (p) => p.startsWith(`${base}/presupuestos`),
    },
    { label: "Producción", href: `${base}/produccion/nuevo`, icon: ClipboardCheck },
    { label: "Estimaciones", href: `${base}/estimaciones`, icon: FileSpreadsheet },
    { label: "Cronograma", href: `${base}/cronograma`, icon: CalendarRange },
    { label: "Compras", href: `${base}/compras`, icon: ShoppingCart },
    { label: "RRHH", href: `${base}/rrhh`, icon: Users },
    { label: "EVM / Caja", href: `${base}/control`, icon: LineChart },
  ];

  const isActive = (href: string) => {
    if (href === base) return pathname === href;
    return pathname.startsWith(href);
  };

  return (
    <nav className="mb-6 flex flex-wrap gap-2 border-b pb-4">
      {menuItems.map((item) => {
        const Icon = item.icon;
        const active = item.isActiveOverride
          ? item.isActiveOverride(pathname)
          : isActive(item.href);
        return (
          <Link key={item.href} href={item.href}>
            <Button
              variant={active ? "secondary" : "ghost"}
              size="sm"
              className={cn(active && "bg-accent font-medium")}
            >
              <Icon className="mr-1.5 h-4 w-4" />
              {item.label}
            </Button>
          </Link>
        );
      })}
    </nav>
  );
}
