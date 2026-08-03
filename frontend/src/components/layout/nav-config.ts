import type { LucideIcon } from "lucide-react";
import {
  BarChart3,
  ClipboardCheck,
  DollarSign,
  Eye,
  FileText,
  FolderOpen,
  LayoutDashboard,
  TrendingUp,
  Truck,
  Users,
  Wallet,
} from "lucide-react";

export type NavLeaf = {
  id: string;
  label: string;
  href: string;
  icon?: LucideIcon;
};

export type NavSection = {
  id: string;
  label: string;
  icon: LucideIcon;
  /** Si existe, la sección es colapsable y agrupa sub-items */
  items?: NavLeaf[];
  /** Ítem directo (sin sub-items) */
  href?: string;
};

export const SIDEBAR_COLLAPSED_KEY = "budgetpro_sidebar_collapsed";
export const SIDEBAR_SECTIONS_KEY = "budgetpro_sidebar_sections";

export const NAV_SECTIONS: NavSection[] = [
  {
    id: "dashboard",
    label: "Dashboard",
    icon: LayoutDashboard,
    items: [
      { id: "overview", label: "Overview", href: "/dashboard/overview", icon: LayoutDashboard },
      { id: "executive", label: "Executive Summary", href: "/dashboard/executive", icon: BarChart3 },
      { id: "operations", label: "Operations Dashboard", href: "/dashboard/operations", icon: TrendingUp },
      { id: "financial", label: "Financial Dashboard", href: "/dashboard/financial", icon: Wallet },
    ],
  },
  {
    id: "reports",
    label: "Report Summaries",
    icon: FileText,
    items: [
      { id: "weekly", label: "Weekly Reports", href: "/reportes/semanal", icon: FileText },
      { id: "monthly", label: "Monthly Insights", href: "/reportes/mensual", icon: BarChart3 },
      { id: "quarterly", label: "Quarterly Analysis", href: "/reportes/trimestral", icon: Eye },
    ],
  },
  {
    id: "proyectos",
    label: "Proyectos",
    icon: FolderOpen,
    href: "/",
  },
  {
    id: "presupuestos",
    label: "Presupuestos",
    icon: DollarSign,
    href: "/presupuestos",
  },
  {
    id: "produccion",
    label: "Producción",
    icon: ClipboardCheck,
    href: "/produccion",
  },
  {
    id: "logistica",
    label: "Logística",
    icon: Truck,
    href: "/logistica",
  },
  {
    id: "rrhh",
    label: "RRHH",
    icon: Users,
    href: "/rrhh",
  },
  {
    id: "finanzas",
    label: "Finanzas",
    icon: Wallet,
    href: "/finanzas",
  },
];

export function isNavHrefActive(pathname: string, href: string): boolean {
  if (href === "/") {
    return pathname === "/" || pathname.startsWith("/proyectos");
  }
  return pathname === href || pathname.startsWith(`${href}/`);
}
