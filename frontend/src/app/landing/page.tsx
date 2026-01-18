import { Arimo } from "next/font/google";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Check, ArrowRight } from "lucide-react";
import DemoForm from "@/components/landing/DemoForm";

const arimo = Arimo({
  subsets: ["latin"],
  weight: ["400", "700"],
  style: ["normal", "italic"],
});

const heroImage = "https://www.figma.com/api/mcp/asset/532bfe91-9168-48e3-a8cf-c25a2407defb";
const constructionImage = "https://www.figma.com/api/mcp/asset/a27149dd-7512-4cbb-a541-f14bb68f33d9";

const problemCards = [
  {
    title: "📉 La Ilusión del 80%",
    body: "Tus residentes dicen que van al 80%, tu financiero dice que gastaste el 90%, pero la realidad física es apenas el 60%.",
  },
  {
    title: "💸 El Agujero Negro de Bodega",
    body: "Material que sale de bodega se considera \"gastado\", pero nadie valida si realmente se instaló o se desperdició.",
  },
  {
    title: "🐌 Reacción Tardía",
    body: "Te enteras de las pérdidas al final del proyecto, cuando ya no puedes corregir nada.",
  },
];

const solutionCards = [
  {
    title: "El Triple Match",
    subtitle: "Bodega vs. Campo vs. Presupuesto",
    body:
      "Nuestro motor cruza automáticamente lo que compraste, lo que salió de bodega y lo que realmente se construyó (RPC). Si los números no cuadran, el sistema dispara una alerta forense.",
    bullets: [
      "Reconciliación automática entre 3 fuentes de verdad",
      "Detección de desperdicios y robos en tiempo real",
      "Alertas configurables por desviación porcentual",
    ],
  },
  {
    title: "Avance Certificado (RPC)",
    subtitle: "No Estimado",
    body:
      "Prohibimos los porcentajes subjetivos. En BudgetPro, el avance se mide en metros cúbicos y unidades reales, certificadas digitalmente por supervisión. Si no está aprobado, no suma valor (EV).",
    bullets: [
      "Medición física cuantificable (m³, m², kg, unidades)",
      "Firma digital de supervisión obligatoria",
      "Avance medido según producción real instalada",
    ],
  },
  {
    title: "Rentabilidad Forense",
    subtitle: "No Contabilidad",
    body:
      "Visualiza el margen real por partida y detecta desviaciones críticas antes de que se conviertan en pérdidas irreversibles.",
    bullets: [
      "Drill-down desde resumen hasta documento fuente",
      "Reportes forenses exportables",
      "Cumplimiento normativo y auditoría",
    ],
  },
];

const roleCards = [
  {
    tag: "Residente de Obra",
    title: "Reportes de Producción de Campo (RPC)",
    body: "Certifique avance en sitio desde el móvil y olvídese de los informes semanales en Excel.",
    bullets: [
      "App móvil para certificación en campo",
      "Captura de avance con unidades físicas reales",
      "Carga de evidencia fotográfica",
      "Aprobación digital con firma electrónica",
    ],
    accent: "border-[#f54900] bg-[#fff7ed] text-[#f54900]",
  },
  {
    tag: "Contralor / Auditor",
    title: "Trazabilidad Total",
    body: "Desde la partida presupuestaria hasta la factura del clavo. Todo vinculado.",
    bullets: [
      "Historial completo de transacciones",
      "Reporte de desviaciones por centro de costo",
      "Exportación de reportes forenses",
      "Cumplimiento normativo y auditoría",
    ],
    accent: "border-[#00a63e] bg-[#f0fdf4] text-[#00a63e]",
  },
  {
    tag: "Directivo",
    title: "Tablero de Control de Portafolio",
    body: "Vea la salud de 10 proyectos en 30 segundos con métricas EVM (CPI/SPI) estandarizadas.",
    bullets: [
      "Dashboard ejecutivo con indicadores CPI/SPI",
      "Vista consolidada de múltiples proyectos",
      "Alertas automáticas de proyectos en riesgo",
      "Gráficas de Curva S por portafolio",
    ],
    accent: "border-[#1c398e] bg-[#eff6ff] text-[#1c398e]",
  },
];

const trustCards = [
  {
    title: "Seguridad Empresarial",
    bullets: [
      "Encriptación AES-256 en reposo y tránsito",
      "Autenticación de dos factores (2FA)",
      "Controles de acceso basados en roles (RBAC)",
    ],
  },
  {
    title: "Backups & Redundancia",
    bullets: [
      "Backups automáticos cada 6 horas",
      "Réplicas en múltiples zonas geográficas",
      "Recuperación ante desastres < 4 horas",
    ],
  },
  {
    title: "Normativa & Cumplimiento",
    bullets: [
      "Cumplimiento ISO 27001",
      "Auditorías trimestrales de seguridad",
      "Monitoreo 24/7 con SLA empresarial",
    ],
  },
  {
    title: "Soporte & Acompañamiento",
    bullets: [
      "Onboarding guiado para equipos",
      "Soporte técnico 24/7",
      "Actualizaciones continuas y roadmap",
    ],
  },
];

export default function LandingPage() {
  return (
    <div className={`${arimo.className} bg-white text-[#0f172b]`}>
      <header className="border-b border-[#d1d5dc] bg-white">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6">
          <div className="text-2xl font-bold text-[#0f172b]">
            Budget<span className="text-[#1c398e]">Pro</span>
          </div>
          <nav className="hidden items-center gap-8 text-sm text-[#314158] md:flex">
            <a href="#problema" className="hover:text-[#1c398e]">El Problema</a>
            <a href="#solucion" className="hover:text-[#1c398e]">La Solución</a>
            <a href="#roles" className="hover:text-[#1c398e]">Por Rol</a>
            <a href="#seguridad" className="hover:text-[#1c398e]">Seguridad</a>
          </nav>
          <div className="hidden items-center gap-4 md:flex">
            <Button asChild variant="ghost" className="text-[#0a0a0a]">
              <Link href="/login">Iniciar Sesión</Link>
            </Button>
            <Button asChild className="bg-[#1c398e] hover:bg-[#162f73]">
              <Link href="/demo">Solicitar Demo Técnica</Link>
            </Button>
          </div>
        </div>
      </header>

      <section className="bg-gradient-to-b from-[#f8fafc] to-white">
        <div className="mx-auto max-w-6xl px-6 py-16 lg:py-24">
          <div className="grid gap-12 lg:grid-cols-[1.1fr_0.9fr] items-center">
            <div className="space-y-8">
              <div className="space-y-4">
                <h1 className="text-4xl font-bold leading-tight text-[#0f172b] md:text-5xl">
                  Deja de adivinar tu rentabilidad.
                  <span className="block text-[#1c398e]">Audita tu obra en tiempo real.</span>
                </h1>
                <p className="text-lg text-[#314158]">
                  <strong>BudgetPro</strong> desacopla el Gasto (AC) del Avance (EV). Detecta
                  desviaciones, robos e ineficiencias antes de que destruyan tu margen. El primer ERP
                  que certifica la producción física, no solo la financiera.
                </p>
              </div>
              <div className="flex flex-wrap gap-4">
              <Button asChild className="bg-[#1c398e] hover:bg-[#162f73]">
                <Link href="/demo">
                  Solicitar Demo Técnica
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
                <Button variant="outline" className="border-[#cad5e2] text-[#0f172b]">
                  Ver Motor de Auditoría →
                </Button>
              </div>
              <div className="flex flex-wrap gap-8 text-sm text-[#45556c]">
                <div>
                  <p className="text-2xl font-bold text-[#0f172b]">±2%</p>
                  <p>Precisión de Costo</p>
                </div>
                <div>
                  <p className="text-2xl font-bold text-[#0f172b]">100%</p>
                  <p>Trazabilidad</p>
                </div>
                <div>
                  <p className="text-2xl font-bold text-[#0f172b]">Real</p>
                  <p>Tiempo Real</p>
                </div>
              </div>
            </div>
            <div className="relative">
              <div className="overflow-hidden rounded-2xl border-4 border-[#e2e8f0] shadow-xl">
                <img className="w-full object-cover" src={heroImage} alt="Panel de control" />
              </div>
              <div className="absolute -bottom-6 -left-6 rounded-xl border-2 border-[#c10007] bg-[#e7000b] px-4 py-4 shadow-xl">
                <p className="text-sm font-bold text-white">Desvío Detectado</p>
                <p className="text-xs text-[#ffe2e2]">Gasto 90% | Avance 62%</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="problema" className="bg-[#0f172b] text-white">
        <div className="mx-auto max-w-6xl px-6 py-20">
          <div className="text-center space-y-4">
            <h2 className="text-3xl font-bold">
              “Gastar presupuesto NO significa avanzar en la obra.”
            </h2>
            <p className="text-[#cad5e2]">
              El problema fundamental de la industria de la construcción:
              <span className="block text-[#ff6467] font-bold">
                confundir erogación con producción.
              </span>
            </p>
          </div>
          <div className="mt-10 grid gap-6 lg:grid-cols-3">
            {problemCards.map((card) => (
              <div key={card.title} className="rounded-2xl border border-[#314158] bg-[#1d293d] p-6">
                <h3 className="text-lg font-bold">{card.title}</h3>
                <p className="mt-4 text-[#cad5e2]">{card.body}</p>
              </div>
            ))}
          </div>
          <div className="mt-10 rounded-xl border border-[#fb2c36] bg-[rgba(130,24,26,0.2)] px-6 py-4 text-center">
            <p className="text-lg font-bold text-[#ff6467]">
              Resultado: Márgenes que desaparecen sin explicación visible.
            </p>
          </div>
        </div>
      </section>

      <section id="solucion" className="bg-white">
        <div className="mx-auto max-w-6xl px-6 py-20">
          <div className="text-center space-y-3">
            <p className="text-xs font-bold uppercase tracking-[0.35px] text-[#1c398e]">Secret Sauce</p>
            <h2 className="text-3xl font-bold">Por qué BudgetPro es técnicamente superior</h2>
            <p className="text-[#45556c]">
              No es un software de contabilidad. Es un sistema de inteligencia de costos.
            </p>
          </div>
          <div className="mt-12 grid gap-6">
            {solutionCards.map((card) => (
              <div key={card.title} className="rounded-2xl border border-[#e2e8f0] bg-[#f8fafc] p-8">
                <div className="grid gap-6 lg:grid-cols-[1fr_1.4fr]">
                  <div className="space-y-3">
                    <h3 className="text-2xl font-bold">{card.title}</h3>
                    <p className="text-sm font-bold text-[#1c398e]">{card.subtitle}</p>
                  </div>
                  <div className="space-y-4 text-[#314158]">
                    <p>{card.body}</p>
                    <ul className="space-y-2 text-sm">
                      {card.bullets.map((bullet) => (
                        <li key={bullet} className="flex items-start gap-2">
                          <Check className="mt-1 h-4 w-4 text-[#1c398e]" />
                          <span>{bullet}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>
              </div>
            ))}
          </div>
          <div className="mt-12 overflow-hidden rounded-2xl border-4 border-[#e2e8f0] shadow-xl">
            <img className="w-full object-cover" src={constructionImage} alt="Trabajo en obra" />
          </div>
        </div>
      </section>

      <section id="roles" className="bg-white">
        <div className="mx-auto max-w-6xl px-6 py-20">
          <div className="text-center space-y-3">
            <p className="text-xs font-bold uppercase tracking-[0.35px] text-[#1c398e]">
              Hablemos el idioma de quien toma decisiones
            </p>
            <h2 className="text-3xl font-bold">Presión, control y visibilidad por rol</h2>
            <p className="text-[#45556c]">
              Porque cada actor necesita una respuesta inmediata y verificable.
            </p>
          </div>
          <div className="mt-12 grid gap-6">
            {roleCards.map((card) => (
              <div key={card.title} className={`rounded-2xl border-2 p-8 ${card.accent}`}>
                <div className="space-y-3">
                  <p className="text-xs font-bold uppercase tracking-[0.35px]">{card.tag}</p>
                  <h3 className="text-2xl font-bold text-[#0f172b]">{card.title}</h3>
                  <p className="text-[#314158]">{card.body}</p>
                </div>
                <ul className="mt-6 grid gap-3 text-sm text-[#314158] md:grid-cols-2">
                  {card.bullets.map((bullet) => (
                    <li key={bullet} className="flex items-start gap-2">
                      <Check className="mt-1 h-4 w-4" />
                      <span>{bullet}</span>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="seguridad" className="bg-white">
        <div className="mx-auto max-w-6xl px-6 py-20">
          <div className="text-center space-y-3">
            <p className="text-xs font-bold uppercase tracking-[0.35px] text-[#1c398e]">
              Seguridad & Confianza
            </p>
            <h2 className="text-3xl font-bold">
              Construido con la precisión que exige la Ingeniería Civil
            </h2>
            <p className="text-[#45556c]">
              Infraestructura de grado empresarial para proteger lo más importante: tus datos financieros.
            </p>
          </div>
          <div className="mt-12 grid gap-6 md:grid-cols-2 lg:grid-cols-4">
            {trustCards.map((card) => (
              <div key={card.title} className="rounded-2xl border border-[#e2e8f0] bg-[#f8fafc] p-6">
                <h3 className="text-lg font-bold">{card.title}</h3>
                <ul className="mt-4 space-y-2 text-sm text-[#314158]">
                  {card.bullets.map((bullet) => (
                    <li key={bullet} className="flex items-start gap-2">
                      <Check className="mt-1 h-4 w-4 text-[#1c398e]" />
                      <span>{bullet}</span>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
          <div className="mt-10 rounded-2xl border-l-4 border-[#1c398e] bg-[#eff6ff] p-6">
            <p className="text-[#314158] italic">
              &quot;Como ingeniero civil, necesitaba un sistema que hablara mi idioma: metros cúbicos,
              toneladas, jornadas reales. BudgetPro es el único ERP que entiende que la construcción no
              se mide en porcentajes, sino en producción física certificada.&quot;
            </p>
            <div className="mt-4 text-sm">
              <p className="font-bold text-[#0f172b]">Ing. Roberto Fernández</p>
              <p className="text-[#45556c]">Director de Proyectos, Constructora Andina</p>
            </div>
          </div>
        </div>
      </section>

      <section className="bg-gradient-to-r from-[#1c398e] to-[#0f172b] text-white">
        <div className="mx-auto max-w-5xl px-6 py-20 text-center space-y-6">
          <h2 className="text-3xl font-bold">¿Listo para recuperar el control de tus costos?</h2>
          <p className="text-[#cad5e2]">
            Deja de perder margen por falta de visibilidad. Audita tu obra en tiempo real con BudgetPro.
          </p>
          <div className="flex flex-wrap justify-center gap-4">
            <Button asChild className="bg-white text-[#1c398e] hover:bg-white/90">
              <Link href="/demo">
                Agendar Demostración
                <ArrowRight className="ml-2 h-4 w-4" />
              </Link>
            </Button>
            <Button variant="outline" className="border-white text-white hover:bg-white/10">
              Descargar Caso de Estudio
            </Button>
          </div>
          <p className="text-xs text-[#cad5e2]">
            Demo personalizada de 45 minutos • Sin compromiso • Para tomadores de decisión
          </p>
        </div>
      </section>

      <footer className="bg-[#0f172b] text-[#cad5e2]">
        <div className="mx-auto max-w-6xl px-6 py-12">
          <div className="grid gap-10 lg:grid-cols-[1.2fr_1fr]">
            <div className="space-y-6">
              <h3 className="text-2xl font-bold text-white">
                Budget<span className="text-[#51a2ff]">Pro</span>
              </h3>
              <p className="text-sm">
                La Verdad Financiera de tu Obra. ERP especializado en construcción.
              </p>
              <div className="grid gap-8 md:grid-cols-3">
                <div className="space-y-3 text-sm">
                  <p className="font-bold text-white">Producto</p>
                  <p>El Problema</p>
                  <p>La Solución</p>
                  <p>Por Rol</p>
                  <p>Seguridad</p>
                </div>
                <div className="space-y-3 text-sm">
                  <p className="font-bold text-white">Compañía</p>
                  <p>Sobre Nosotros</p>
                  <p>Cultura</p>
                  <p>Prensa</p>
                  <p>Contacto</p>
                </div>
                <div className="space-y-3 text-sm">
                  <p className="font-bold text-white">Recursos</p>
                  <p>Blog</p>
                  <p>Casos de Estudio</p>
                  <p>Centro de Ayuda</p>
                  <p>Actualizaciones</p>
                </div>
              </div>
            </div>
            <div className="lg:justify-self-end">
              <DemoForm />
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}
