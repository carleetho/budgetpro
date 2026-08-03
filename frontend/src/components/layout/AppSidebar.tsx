"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  Building2,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  LogOut,
  Search,
  Settings,
  User,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { cn } from "@/lib/utils";
import { AuthService, type AuthMe } from "@/services/auth.service";
import { useSidebarState } from "@/components/layout/SidebarStateProvider";
import {
  NAV_SECTIONS,
  isNavHrefActive,
  type NavSection,
} from "@/components/layout/nav-config";

function initialsFromName(nombre: string): string {
  const parts = nombre.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "U";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
}

function NavLinkRow({
  href,
  label,
  icon: Icon,
  active,
  collapsed,
  indented,
}: {
  href: string;
  label: string;
  icon?: React.ComponentType<{ className?: string }>;
  active: boolean;
  collapsed: boolean;
  indented?: boolean;
}) {
  const content = (
    <Link
      href={href}
      className={cn(
        "flex items-center gap-2.5 rounded-md text-[13px] transition-colors",
        collapsed ? "h-10 w-10 justify-center mx-auto" : "px-3 py-2 mx-2",
        indented && !collapsed && "pl-8 text-[12.5px]",
        active
          ? "bg-sidebar-accent text-sidebar-accent-foreground font-medium"
          : "text-sidebar-foreground/80 hover:bg-sidebar-accent/70 hover:text-sidebar-accent-foreground"
      )}
    >
      {Icon ? (
        <Icon className={cn("shrink-0", collapsed ? "h-[18px] w-[18px]" : "h-[15px] w-[15px]")} />
      ) : (
        !collapsed && (
          <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-sidebar-foreground/40" />
        )
      )}
      {!collapsed && <span className="flex-1 truncate text-left">{label}</span>}
    </Link>
  );

  if (!collapsed) return content;

  return (
    <Tooltip>
      <TooltipTrigger asChild>{content}</TooltipTrigger>
      <TooltipContent side="right">{label}</TooltipContent>
    </Tooltip>
  );
}

function SectionBlock({
  section,
  collapsed,
  open,
  onToggle,
  pathname,
  filter,
}: {
  section: NavSection;
  collapsed: boolean;
  open: boolean;
  onToggle: () => void;
  pathname: string;
  filter: string;
}) {
  const Icon = section.icon;
  const items = section.items ?? [];
  const filteredItems = filter
    ? items.filter((i) => i.label.toLowerCase().includes(filter))
    : items;

  if (section.href) {
    const labelMatch = !filter || section.label.toLowerCase().includes(filter);
    if (!labelMatch) return null;
    return (
      <NavLinkRow
        href={section.href}
        label={section.label}
        icon={Icon}
        active={isNavHrefActive(pathname, section.href)}
        collapsed={collapsed}
      />
    );
  }

  if (filter && filteredItems.length === 0 && !section.label.toLowerCase().includes(filter)) {
    return null;
  }

  if (collapsed) {
    return (
      <Popover>
        <PopoverTrigger asChild>
          <button
            type="button"
            className={cn(
              "mx-auto flex h-10 w-10 items-center justify-center rounded-md transition-colors",
              "text-sidebar-foreground/80 hover:bg-sidebar-accent/70 hover:text-sidebar-accent-foreground",
              items.some((i) => isNavHrefActive(pathname, i.href)) &&
                "bg-sidebar-accent text-sidebar-accent-foreground"
            )}
            aria-label={section.label}
          >
            <Icon className="h-[18px] w-[18px]" />
          </button>
        </PopoverTrigger>
        <PopoverContent
          side="right"
          className="dark w-52 border-sidebar-border bg-sidebar p-2 text-sidebar-foreground"
        >
          <p className="px-2 py-1 text-[10px] font-semibold uppercase tracking-wider text-sidebar-foreground/50">
            {section.label}
          </p>
          <div className="space-y-0.5">
            {items.map((item) => (
              <Link
                key={item.id}
                href={item.href}
                className={cn(
                  "block rounded-md px-2 py-1.5 text-sm",
                  isNavHrefActive(pathname, item.href)
                    ? "bg-sidebar-accent text-sidebar-accent-foreground"
                    : "hover:bg-sidebar-accent/70"
                )}
              >
                {item.label}
              </Link>
            ))}
          </div>
        </PopoverContent>
      </Popover>
    );
  }

  return (
    <div className="space-y-0.5">
      <button
        type="button"
        onClick={onToggle}
        className={cn(
          "mx-2 flex w-[calc(100%-1rem)] items-center gap-2.5 rounded-md px-3 py-2 text-[13px] transition-colors",
          "text-sidebar-foreground/80 hover:bg-sidebar-accent/70 hover:text-sidebar-accent-foreground"
        )}
      >
        <Icon className="h-[15px] w-[15px] shrink-0" />
        <span className="flex-1 truncate text-left">{section.label}</span>
        <ChevronDown
          className={cn(
            "h-3 w-3 shrink-0 text-sidebar-foreground/50 transition-transform",
            open && "rotate-180"
          )}
        />
      </button>
      {open &&
        (filter ? filteredItems : items).map((item) => (
          <NavLinkRow
            key={item.id}
            href={item.href}
            label={item.label}
            active={isNavHrefActive(pathname, item.href)}
            collapsed={false}
            indented
          />
        ))}
    </div>
  );
}

export function AppSidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const { collapsed, hydrated, sectionsOpen, setCollapsed, toggleSection } = useSidebarState();
  const [query, setQuery] = useState("");
  const [user, setUser] = useState<AuthMe | null>(null);
  const [userFallback, setUserFallback] = useState(false);

  useEffect(() => {
    let cancelled = false;
    AuthService.me()
      .then((me) => {
        if (!cancelled) {
          setUser(me);
          setUserFallback(false);
        }
      })
      .catch((err: { status?: number }) => {
        if (cancelled) return;
        if (err?.status === 401) {
          router.replace("/login");
          return;
        }
        setUserFallback(true);
        setUser(null);
      });
    return () => {
      cancelled = true;
    };
  }, [router]);

  const filter = query.trim().toLowerCase();
  const displayName = userFallback ? "Usuario" : user?.nombre ?? "Usuario";
  const displayEmail = userFallback ? "" : user?.email ?? "";
  const avatarText = userFallback ? "U" : initialsFromName(user?.nombre ?? "Usuario");

  return (
    <aside
      className={cn(
        "dark fixed left-0 top-0 z-20 flex h-screen flex-col border-r border-sidebar-border bg-sidebar text-sidebar-foreground transition-[width] duration-200 ease-in-out",
        collapsed ? "w-16 min-w-16" : "w-60 min-w-60",
        !hydrated && "invisible"
      )}
      data-collapsed={collapsed || undefined}
    >
      <div
        className={cn(
          "flex items-center gap-2.5 border-b border-sidebar-border",
          collapsed ? "justify-center px-0 py-4" : "px-4 py-4"
        )}
      >
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-indigo-500 to-violet-500">
          <Building2 className="h-4 w-4 text-white" />
        </div>
        {!collapsed && (
          <div className="min-w-0">
            <div className="text-sm font-bold tracking-tight text-sidebar-accent-foreground">
              BudgetPRO
            </div>
            <div className="text-[10px] text-sidebar-foreground/50">v2.0 · ERP Construcción</div>
          </div>
        )}
      </div>

      <div className={cn(collapsed ? "flex justify-center py-3" : "p-3", "border-b border-sidebar-border")}>
        {collapsed ? (
          <Tooltip>
            <TooltipTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="h-9 w-9 text-sidebar-foreground/70 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
                onClick={() => setCollapsed(false)}
                aria-label="Buscar"
              >
                <Search className="h-4 w-4" />
              </Button>
            </TooltipTrigger>
            <TooltipContent side="right">Buscar</TooltipContent>
          </Tooltip>
        ) : (
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-sidebar-foreground/40" />
            <Input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Buscar tareas, proyectos..."
              className="h-8 border-sidebar-border bg-white/5 pl-8 text-xs text-sidebar-foreground placeholder:text-sidebar-foreground/40 focus-visible:ring-sidebar-ring"
            />
          </div>
        )}
      </div>

      <nav className="flex-1 space-y-1 overflow-y-auto py-2">
        {NAV_SECTIONS.map((section) => (
          <SectionBlock
            key={section.id}
            section={section}
            collapsed={collapsed}
            open={sectionsOpen[section.id] ?? false}
            onToggle={() => toggleSection(section.id)}
            pathname={pathname}
            filter={filter}
          />
        ))}
      </nav>

      <div className="space-y-1 border-t border-sidebar-border p-2">
        {collapsed ? (
          <Tooltip>
            <TooltipTrigger asChild>
              <Link
                href="/settings"
                className="mx-auto flex h-10 w-10 items-center justify-center rounded-md text-sidebar-foreground/80 hover:bg-sidebar-accent/70"
                aria-label="Configuración"
              >
                <Settings className="h-[18px] w-[18px]" />
              </Link>
            </TooltipTrigger>
            <TooltipContent side="right">Configuración</TooltipContent>
          </Tooltip>
        ) : (
          <Link
            href="/settings"
            className="flex items-center gap-2.5 rounded-md px-3 py-2 text-[13px] text-sidebar-foreground/80 hover:bg-sidebar-accent/70"
          >
            <Settings className="h-[15px] w-[15px]" />
            <span>Configuración</span>
          </Link>
        )}

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button
              type="button"
              className={cn(
                "flex items-center gap-2.5 rounded-md transition-colors hover:bg-sidebar-accent/70",
                collapsed ? "mx-auto h-10 w-10 justify-center p-0" : "w-full px-3 py-2"
              )}
            >
              <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500 to-violet-500 text-[11px] font-bold text-white">
                {avatarText}
              </div>
              {!collapsed && (
                <div className="min-w-0 flex-1 text-left">
                  <div className="truncate text-[12.5px] font-medium text-sidebar-accent-foreground">
                    {displayName}
                  </div>
                  {displayEmail && (
                    <div className="truncate text-[11px] text-sidebar-foreground/50">{displayEmail}</div>
                  )}
                </div>
              )}
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent side={collapsed ? "right" : "top"} align="start" className="w-52">
            <DropdownMenuItem asChild>
              <Link href="/perfil">
                <User className="h-4 w-4" />
                Perfil
              </Link>
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              variant="destructive"
              onClick={() => {
                AuthService.logout();
                window.location.href = "/login";
              }}
            >
              <LogOut className="h-4 w-4" />
              Cerrar sesión
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <Button
        variant="ghost"
        size="icon"
        type="button"
        aria-label={collapsed ? "Expandir sidebar" : "Colapsar sidebar"}
        onClick={() => setCollapsed(!collapsed)}
        className="absolute -right-3 top-1/2 z-30 h-6 w-6 -translate-y-1/2 rounded-full border border-sidebar-border bg-sidebar text-sidebar-foreground shadow-sm hover:bg-sidebar-accent"
      >
        {collapsed ? <ChevronRight className="h-3 w-3" /> : <ChevronLeft className="h-3 w-3" />}
      </Button>
    </aside>
  );
}
