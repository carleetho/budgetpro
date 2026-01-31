import streamlit as st
import plotly.express as px
import pandas as pd
import json
import pathlib
from streamlit_plotly_events import plotly_events
import io
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors

# Configure the page
st.set_page_config(
    page_title="AXIOM Dashboard",
    page_icon="🛡️",
    layout="wide",
    initial_sidebar_state="expanded"
)

# File Paths
METRICS_FILE = pathlib.Path(".budgetpro/metrics.json")
LOG_FILE = pathlib.Path(".budgetpro/axiom.log")
DEFAULT_CONFIG_PATH = pathlib.Path(".budgetpro/axiom.config.yaml")
LEGACY_CONFIG_PATH = pathlib.Path("axiom.config.yaml")

@st.cache_data(ttl=10)
def load_metrics_data():
    """Loads and validates metrics from JSON file."""
    if not METRICS_FILE.exists():
        return None
    
    try:
        with open(METRICS_FILE, 'r') as f:
            data = json.load(f)
            
        # Basic validation
        if "history" not in data or "statistics" not in data:
            st.error("Metrics file format is invalid (missing 'history' or 'statistics').")
            return None
            
        return data
    except json.JSONDecodeError:
        st.error("Unable to parse metrics data. File may be corrupted.")
        return None
    except Exception as e:
        st.error(f"Error loading metrics: {e}")
        return None

@st.cache_data(ttl=10)
def load_validation_log():
    """Parses validation log for JSON entries."""
    if not LOG_FILE.exists():
        return []
        
    entries = []
    try:
        with open(LOG_FILE, 'r') as f:
            content = f.read()
            
        # Extract JSON blocks
        # Format: [JSON_ENTRY]...[/JSON_ENTRY]
        raw_entries = content.split("[JSON_ENTRY]")
        for entry in raw_entries[1:]: # Skip text before first entry
            if "[/JSON_ENTRY]" in entry:
                json_str = entry.split("[/JSON_ENTRY]")[0]
                try:
                    parsed = json.loads(json_str)
                    entries.append(parsed)
                except json.JSONDecodeError:
                    continue # Skip malformed entries
                    
        return entries
    except Exception as e:
        st.error(f"Error reading log file: {e}")
        return []

# Helpers
def calculate_health_score(pass_rate: float, avg_blocking: float, has_integrity_violation: bool = False) -> int:
    """Calculates health score (0-100)."""
    base = pass_rate
    penalty = avg_blocking * 10
    
    # Critical penalty for Integrity Violations (Semantic Lock)
    if has_integrity_violation:
        penalty += 20
        
    score = max(0, base - penalty)
    return int(min(100, score))

def determine_trend(history: list) -> str:
    """and determines trend (MEJORANDO, DEGRADANDO, ESTABLE)."""
    if len(history) < 2:
        return "ESTABLE →"
    
    # Simple recent vs previous comparison
    # Take up to last 10 runs
    recent_runs = history[-10:]
    midpoint = len(recent_runs) // 2
    
    if midpoint == 0:
         return "ESTABLE →"

    previous = recent_runs[:midpoint]
    recent = recent_runs[midpoint:]
    
    def get_blocking(runs):
        return sum(r.get('by_severity', {}).get('error', 0) for r in runs) / len(runs)
    
    avg_prev = get_blocking(previous)
    avg_recent = get_blocking(recent)
    
    current_blocking = history[-1].get('by_severity', {}).get('error', 0)
    
    if current_blocking > avg_prev and current_blocking > 0:
        return "DEGRADANDO ↓"
    
    if avg_prev == 0:
        if avg_recent > 0:
            return "DEGRADANDO ↓"
        else:
            return "ESTABLE →"

    change = (avg_recent - avg_prev) / avg_prev
    
    if change < -0.1:
        return "MEJORANDO ↑"
    elif change > 0.1:
        return "DEGRADANDO ↓"
    else:
        return "ESTABLE →"

def build_health_gauge(score):
    import plotly.graph_objects as go
    fig = go.Figure(go.Indicator(
        mode = "gauge+number",
        value = score,
        domain = {'x': [0, 1], 'y': [0, 1]},
        title = {'text': "Health Score (Latest Run)"},
        gauge = {
            'axis': {'range': [None, 100]},
            'bar': {'color': "white"},
            'steps' : [
                {'range': [0, 40], 'color': "red"},
                {'range': [40, 70], 'color': "orange"},
                {'range': [70, 100], 'color': "green"}],
            'threshold' : {
                'line': {'color': "white", 'width': 4},
                'thickness': 0.75,
                'value': 90}
        }
    ))
    fig.update_layout(template="plotly_dark", height=300)
    return fig

def build_violations_by_validator_chart(history):
    if not history: return None
    latest = history[-1]
    data = latest.get('by_validator', {})
    if not data: return None
    
    df = pd.DataFrame(list(data.items()), columns=['Validator', 'Count'])
    fig = px.bar(df, x='Validator', y='Count', color='Validator', 
                 title="Violations by Validator (Latest Run)", template="plotly_dark")
    return fig

def build_violations_by_module_chart(history):
    if not history: return None
    latest = history[-1]
    data = latest.get('by_module', {})
    if not data: return None
    
    df = pd.DataFrame(list(data.items()), columns=['Module', 'Count'])
    fig = px.bar(df, x='Module', y='Count', color='Module', 
                 title="Violations by Module (Latest Run)", template="plotly_dark")
    return fig

def build_trend_chart(history):
    if not history: return None
    # Last 20 runs
    recent = history[-20:]
    
    data = []
    for run in recent:
        ts = run.get('timestamp', '')
        # Simplification: just formatting timestamp or use index if ts missing
        sev = run.get('by_severity', {})
        data.append({
            'Timestamp': ts,
            'Blocking': sev.get('error', 0) + sev.get('blocking', 0), # Support both error/blocking keys
            'Warning': sev.get('warning', 0)
        })
    
    df = pd.DataFrame(data)
    if df.empty: return None
    
    fig = px.line(df, x='Timestamp', y=['Blocking', 'Warning'], 
                  title="Violation Trend (Last 20 Runs)", template="plotly_dark",
                  color_discrete_map={"Blocking": "red", "Warning": "yellow"})
    return fig

def build_blast_radius_chart(history):
    if not history: return None
    recent = history[-20:]
    
    data = []
    for run in recent:
        ts = run.get('timestamp', '')
        # Try to get specific blast radius count if available
        impact = run.get('files_scanned', run.get('total_files', 0))
        
        data.append({
            'Timestamp': ts,
            'Files Impacted': impact 
        })
        
    df = pd.DataFrame(data)
    fig = px.bar(df, x='Timestamp', y='Files Impacted', 
                 title="Files Scanned/Impacted per Run", template="plotly_dark")
    return fig

def build_todo_distribution_chart(todos_by_module):
    if not todos_by_module: return None
    
    data = []
    for module, todo_list in todos_by_module.items():
        data.append({'Module': module, 'Count': len(todo_list)})
        
    df = pd.DataFrame(data)
    if df.empty: return None
    
    # Sort by count descending
    df = df.sort_values(by='Count', ascending=True) # Ascending for horizontal bar to show largest at top usually?, no in plotly horizontal y is category.
    # Actually usually better to sort descending for list, but for horizontal bar, the bottom is 0 index.
    
    fig = px.bar(df, x='Count', y='Module', orientation='h',
                 title="TODOs by Module", template="plotly_dark",
                 text='Count')
    fig.update_traces(textposition='outside')
    return fig

def build_hotspots_chart(history, log_entries):
    """Visualizes files with high entropy (frequency of changes)."""
    hotspots = {}
    for entry in log_entries:
        if "DOMAIN ENTROPY" in entry.get('message', ''):
            file_path = entry.get('file_path', 'unknown')
            hotspots[file_path] = hotspots.get(file_path, 0) + 1
            
    if not hotspots: return None
    
    df = pd.DataFrame(list(hotspots.items()), columns=['File', 'Alerts'])
    df = df.sort_values(by='Alerts', ascending=False).head(10)
    
    fig = px.bar(df, x='Alerts', y='File', orientation='h',
                 title="🔥 Domain Hotspots (Entropy Alerts)", template="plotly_dark",
                 color='Alerts', color_continuous_scale='Reds')
    return fig

def load_config_data():
    """Loads AXIOM configuration for display."""
    if DEFAULT_CONFIG_PATH.exists():
        path = DEFAULT_CONFIG_PATH
    elif LEGACY_CONFIG_PATH.exists():
        path = LEGACY_CONFIG_PATH
    else:
        return None, None
        
    try:
        import yaml
        with open(path, 'r') as f:
            config = yaml.safe_load(f)
        return config, str(path)
    except Exception as e:
        return None, str(path)

# 5. Interactive Features

def init_drill_down_state():
    if 'drill_down_filters' not in st.session_state:
        st.session_state['drill_down_filters'] = None
    if 'chart_key_suffix' not in st.session_state:
        st.session_state['chart_key_suffix'] = 0

def clear_drill_down():
    st.session_state['drill_down_filters'] = None
    st.session_state['chart_key_suffix'] += 1
    st.rerun()

def generate_pdf_report(metrics_data, history):
    """Generates a PDF report of the dashboard state."""
    buffer = io.BytesIO()
    c = canvas.Canvas(buffer, pagesize=letter)
    width, height = letter
    
    # Title
    c.setFont("Helvetica-Bold", 24)
    c.drawString(50, height - 50, "AXIOM Dashboard Report")
    
    # Timestamp
    import datetime
    dt = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    c.setFont("Helvetica", 12)
    c.drawString(50, height - 70, f"Generated: {dt}")
    
    # Summary Metrics
    stats = metrics_data.get('statistics', {})
    pass_rate = stats.get('pass_rate', 0)
    
    latest_run = history[-1] if history else {}
    latest_blocking = latest_run.get('by_severity', {}).get('error', 0) + latest_run.get('by_severity', {}).get('blocking', 0)
    total_runs = stats.get('total_runs', 0)
    
    y = height - 120
    c.setFont("Helvetica-Bold", 14)
    c.drawString(50, y, "Summary Metrics")
    c.setFont("Helvetica", 12)
    y -= 25
    c.drawString(50, y, f"Pass Rate: {pass_rate:.1f}%")
    c.drawString(250, y, f"Total Runs: {total_runs}")
    y -= 20
    c.drawString(50, y, f"Latest Blocking Violations: {latest_blocking}")
    
    # Validation Charts
    # Note: Generating static images from Plotly requires kaleido
    y -= 40
    c.setFont("Helvetica-Bold", 14)
    c.drawString(50, y, "Charts")
    y -= 300 # Space for chart
    
    # Ensure there is a chart to draw
    try:
        # We need to recreate the fig here or pass it in. 
        # Recreating is safer/easier than passing huge objects around if strictly needed,
        # but let's try to rebuild the trend chart simple for the report.
        from plotly import io as pio
        
        # Trend Chart
        fig_trend = build_trend_chart(history)
        if fig_trend:
            # Resize for PDF
            fig_trend.update_layout(width=500, height=250)
            img_bytes = fig_trend.to_image(format="png")
            
            # Save to temporary buffer or direct? Reportlab needs file-like or path
            # It accepts ImageReader wrapping BytesIO
            from reportlab.lib.utils import ImageReader
            img = ImageReader(io.BytesIO(img_bytes))
            c.drawImage(img, 50, y, width=500, height=250)
            
            c.drawString(50, y - 20, "Fig 1: Violation Trend")
            y -= 300
            
        # If space runs out, add page? For simplicity 1 page or just flow down
        if y < 100:
            c.showPage()
            y = height - 50
            
        # By Validator Chart
        fig_valid = build_violations_by_validator_chart(history)
        if fig_valid:
            fig_valid.update_layout(width=500, height=250)
            img_bytes = fig_valid.to_image(format="png")
            img = ImageReader(io.BytesIO(img_bytes))
            c.drawImage(img, 50, y, width=500, height=250)
            c.drawString(50, y - 20, "Fig 2: Violations by Validator")

    except Exception as e:
        c.drawString(50, y, f"Error generating charts: {str(e)}")
        
    c.save()
    buffer.seek(0)
    return buffer

def show_first_time_tutorial():
    if 'tutorial_complete' not in st.session_state:
        st.session_state['tutorial_complete'] = False
        
    if not st.session_state['tutorial_complete']:
        st.balloons()
        with st.container():
            st.info("""
            👋 **¡Bienvenido a AXIOM Dashboard!**
            
            Esta herramienta te ayudará a mantener la arquitectura de BudgetPro impecable.
            
            1. **Revisa el Estado**: El panel superior te dice si el sistema está saludable.
            2. **Analiza Gráficos**: Explora tendencias y hotspots en la pestaña "Análisis".
            3. **Gestiona Deuda**: Consulta la lista de TODOs pendientes.
            4. **Aprende**: Usa la barra lateral para documentación y ayuda.
            
            ¡Happy Coding! 🚀
            """)
            if st.button("¡Entendido, empecemos!"):
                st.session_state['tutorial_complete'] = True
                st.rerun()

def show_improvement_recommendations(health_score, latest_blocking, todos_count):
    if health_score < 70:
        st.warning(f"⚠️ **Tu Health Score es {health_score}/100. Acciones recomendadas:**")
        
        recommendations = []
        if latest_blocking > 0:
            recommendations.append(f"🔴 **Prioridad Alta**: Corrige las {latest_blocking} violaciones bloqueantes.")
        if health_score < 50:
            recommendations.append("📉 **Calidad Crítica**: Detén el desarrollo de nuevas features y refactoriza.")
        if todos_count > 20:
            recommendations.append(f"🧹 **Limpieza**: Tienes {todos_count} TODOs. Dedica un sprint a reducirlos.")
        
        for rec in recommendations:
            st.markdown(f"- {rec}")
        st.markdown("---")

def build_validator_docs():
    docs = {
        "Base Policy (Blast Radius)": """
        **Propósito**: Evita cambios masivos y dispersos que rompen la estabilidad del sistema.
        
        **Límites actuales:**
        - **Zona Roja**: Máximo 1 archivo (Foco total en dominio).
        - **Zona Amarilla**: Máximo 3 archivos.
        - **Zona Verde**: Máximo 10 archivos.
        """,
        "SecurityValidator": """
        **Propósito**: Detecta secretos, claves API y protege la integridad semántica del Dominio.
        
        **Nivel Sentinel:**
        - **Semantic Lock**: Bloquea cambios en Enums, Classes o Interfaces en Zonas Rojas sin un override explícito.
        - **Secret Detection**: Busca claves API (AWS, Stripe, etc.) hardcodeadas.
        """,
        "LazyCodeValidator": """
        **Propósito**: Impide código incompleto o de depuración en el repositorio.
        
        **Patrones Críticos:**
        - `System.out.println`, `e.printStackTrace()`, `console.log()`.
        - Métodos vacíos o retornos nulos en persistencia.
        - TODOs/FIXMEs en módulos de dominio.
        """,
        "DependencyValidator": """
        **Propósito**: Garantiza la pureza técnica del Dominio (Hexagonal / DDD).
        
        **Restricciones**:
        - El dominio NO puede importar `infrastructure`.
        - El dominio NO puede importar `Spring` u otros frameworks.
        - El dominio NO puede importar `Persistence/JPA`.
        """
    }
    return docs

# --- CONFIGURATION & SETUP ---
# (Previous setup code remains, but session state init happens implicitly)

# Load Data
metrics_data = load_metrics_data()
log_entries = load_validation_log()
config_data, config_path = load_config_data()

# Header & Tutorial
st.title("🛡️ AXIOM Dashboard")
# Initialize Search/Filter State
init_drill_down_state()
show_first_time_tutorial()
st.markdown("### Centinela de Integridad Arquitectónica")

# Status Messages & Metrics
if metrics_data:
    stats = metrics_data.get('statistics', {})
    history = metrics_data.get('history', [])
    
    # 1. Status Indicator
    latest_run = history[-1] if history else {}
    latest_blocking = latest_run.get('by_severity', {}).get('error', 0)
    
    if latest_blocking == 0:
        st.success("✓ SISTEMA SALUDABLE - No se detectaron violaciones bloqueantes.")
    else:
         st.error(f"⚠ VIOLACIONES DETECTADAS - {latest_blocking} violaciones bloqueantes requieren atención.")

    # 2. Summary Metrics
    st.markdown("---")
    cols = st.columns(5)
    
    pass_rate = stats.get('pass_rate', 0)
    
    total_blocking = sum(r.get('by_severity', {}).get('error', 0) for r in history)
    avg_blocking = total_blocking / len(history) if history else 0
    
    avg_warnings = sum(r.get('by_severity', {}).get('warning', 0) for r in history) / len(history) if history else 0
    
    has_integrity_violation = any("SEMANTIC LOCK" in entry.get('message', '') for entry in log_entries)
    has_dependency_violation = any("FORBIDDEN IMPORT" in entry.get('message', '') for entry in log_entries)
    
    # Calculate health score based on LATEST run for the main indicator
    latest_pass_rate = 100 if latest_blocking == 0 else 0
    latest_health_score = calculate_health_score(latest_pass_rate, latest_blocking, has_integrity_violation or has_dependency_violation)
    
    trend = determine_trend(history)
    
    # Status Banner
    if latest_blocking > 0:
        st.error(f"🚫 **COMMIT BLOQUEADO**: Se detectaron {latest_blocking} fallos críticos en la última ejecución.")
    else:
        st.success("✅ **COMMIT APROBADO**: El código cumple con las políticas de AXIOM.")

    col_gauge, col_metrics = st.columns([1, 2])
    with col_gauge:
        st.plotly_chart(build_health_gauge(latest_health_score), use_container_width=True)
    
    with col_metrics:
        m_cols = st.columns(2)
        m_cols[0].metric("Latest Health", f"{latest_health_score}/100", trend)
        m_cols[1].metric("History Pass Rate", f"{pass_rate:.1f}%")
        
        m_cols2 = st.columns(2)
        m_cols2[0].metric("Avg Blocking (History)", f"{avg_blocking:.1f}")
        m_cols2[1].metric("Total Validations", stats.get('total_runs', 0))

    st.markdown("---")
    
    # NEW: Integrity & Architecture Highlights
    if has_integrity_violation or has_dependency_violation:
        if has_integrity_violation:
            st.error("🚨 **INTEGRITY BREACH**: Se intentó modificar el núcleo del dominio en una Zona Roja.")
        if has_dependency_violation:
            st.warning("📐 **ARCHITECTURAL DEBT**: Se detectaron importaciones prohibidas en el Dominio (Spring/Infrastructure).")
            
        with st.expander("Ver detalles de integridad y arquitectura", expanded=True):
            for entry in log_entries:
                if "SEMANTIC LOCK" in entry.get('message', '') or "FORBIDDEN IMPORT" in entry.get('message', ''):
                    st.warning(f"**Archivo**: `{entry.get('file_path')}`\n\n{entry.get('message')}")
        st.markdown("---")

    # NEW: Smart Recommendations
    todos_by_module = latest_run.get('todos_by_module', {})
    total_todos = sum(len(items) for items in todos_by_module.values())
    show_improvement_recommendations(latest_health_score, latest_blocking, total_todos)
    
    # 3. Charts & Analysis
    st.header("🔎 Análisis de Violaciones")
    
    col1, col2 = st.columns(2)
    
    with col1:
        fig_trend = build_trend_chart(history)
        if fig_trend: 
            st.caption("Tendencia Temporal (Click para filtrar)")
            sel_trend = plotly_events(fig_trend, click_event=True, hover_event=False, override_height=None, key=f"trend_{st.session_state.chart_key_suffix}")
            if sel_trend:
                # Format: [{'x': '2026-01-30T10:00:00', 'y': 5, ...}]
                ts = sel_trend[0].get('x')
                st.session_state['drill_down_filters'] = {'type': 'timestamp', 'value': ts}
                
        fig_valid = build_violations_by_validator_chart(history)
        if fig_valid:
            st.caption("Por Validador (Click para filtrar)")
            sel_valid = plotly_events(fig_valid, click_event=True, hover_event=False, override_height=None, key=f"valid_{st.session_state.chart_key_suffix}")
            if sel_valid:
                val = sel_valid[0].get('x')
                st.session_state['drill_down_filters'] = {'type': 'validator', 'value': val}
        
    with col2:
        # Blast radius / files scanned
        fig_blast = build_blast_radius_chart(history)
        if fig_blast: st.plotly_chart(fig_blast, use_container_width=True)
        
        fig_module = build_violations_by_module_chart(history)
        if fig_module:
            st.caption("Por Módulo (Click para filtrar)")
            sel_module = plotly_events(fig_module, click_event=True, hover_event=False, override_height=None, key=f"module_{st.session_state.chart_key_suffix}")
            if sel_module:
                mod = sel_module[0].get('x')
                st.session_state['drill_down_filters'] = {'type': 'module', 'value': mod}

    # 4. Domain Hotspots (NEW)
    fig_hotspots = build_hotspots_chart(history, log_entries)
    if fig_hotspots:
        st.markdown("---")
        st.subheader("🔥 Mapa de Calor de Entropía (Hotspots)")
        st.plotly_chart(fig_hotspots, use_container_width=True)
        st.info("💡 **Tip**: Archivos con alta entropía en la Zona Roja indican inestabilidad del dominio. Considera encapsular lógica o refactorizar.")

    # 5. Violation Details Table
    if log_entries:
        st.markdown("---")
        st.subheader("📋 Detalle de Violaciones")
        
        # Prepare DataFrame
        df_log = pd.DataFrame(log_entries)
        filtered_df = df_log.copy()
        
        # Apply Drill-Down Filters
        active_filter = st.session_state.get('drill_down_filters')
        if active_filter:
            f_type = active_filter['type']
            f_val = active_filter['value']
            
            st.info(f"🔍 Filtrando por **{f_type}**: `{f_val}`")
            if st.button("Limpiar Filtros Drill-Down"):
                clear_drill_down()
                
            if f_type == 'validator' and 'validator' in filtered_df.columns:
                filtered_df = filtered_df[filtered_df['validator'] == f_val]
            elif f_type == 'module' and 'module' in filtered_df.columns:
                filtered_df = filtered_df[filtered_df['module'] == f_val]
            elif f_type == 'timestamp':
                # Advanced: Try to filter by specific run timestamp if possible, or just ignore if log doesn't have it
                # Log entries might not have timestamp, they are aggregate? 
                # Task says "Click on trend chart... show violations from that specific run"
                # But validation.log is usually cumulative or just latest?
                # Code load_validation_log reads whole file. Entries structure check needed.
                # Assuming log entries might have timestamp or we match by simple heuristic if not available.
                # If log doesn't have timestamp, we can't filter.
                if 'timestamp' in filtered_df.columns:
                     filtered_df = filtered_df[filtered_df['timestamp'] == f_val]
                else:
                    st.warning("No se puede filtrar por timestamp en los logs actuales.")
        
        
        st.markdown("#### Filtros Adicionales")
        f_col1, f_col2, f_col3 = st.columns(3)
        
        # Filter Options (based on ALREADY filtered data or original? Usually original for context, but sticky)
        # Using original for options to avoid shrinking lists too much
        
        all_severities = sorted(df_log['severity'].unique().tolist()) if 'severity' in df_log.columns else []
        all_validators = sorted(df_log['validator'].unique().tolist()) if 'validator' in df_log.columns else []
        all_modules = sorted(df_log['module'].unique().tolist()) if 'module' in df_log.columns else []

        with f_col1:
            sel_severity = st.multiselect("Severidad", all_severities, default=[])
        with f_col2:
            sel_validator = st.multiselect("Validador", all_validators, default=[])
        with f_col3:
            sel_module = st.multiselect("Módulo", all_modules, default=[])
            
        # Apply Manual Filters
        if 'severity' in filtered_df.columns and sel_severity:
            filtered_df = filtered_df[filtered_df['severity'].isin(sel_severity)]
        if 'validator' in filtered_df.columns and sel_validator:
            filtered_df = filtered_df[filtered_df['validator'].isin(sel_validator)]
        if 'module' in filtered_df.columns and sel_module:
            filtered_df = filtered_df[filtered_df['module'].isin(sel_module)]
            
        st.dataframe(filtered_df, use_container_width=True)
        st.caption(f"Mostrando {len(filtered_df)} de {len(df_log)} entradas.")
    else:
        st.info("No hay datos detallados de logs disponibles para mostrar en la tabla.")
            
    # 5. TODO Tracking Section
    if todos_by_module:
        st.markdown("---")
        st.subheader("📝 Seguimiento de TODOs")
        
        st.metric("Total Pending TODOs", total_todos)
        
        col_todo_chart, col_todo_list = st.columns([1, 1])
        
        with col_todo_chart:
            fig_todo = build_todo_distribution_chart(todos_by_module)
            if fig_todo: st.plotly_chart(fig_todo, use_container_width=True)
            
        with col_todo_list:
            with st.expander("Ver Lista Detallada", expanded=False):
                for module, items in todos_by_module.items():
                    st.subheader(f"{module} ({len(items)})")
                    for item in items:
                        st.text(f"• {item}")
    else:
        st.markdown("---")
        st.info("No TODOs tracked. TODOs are only detected in configured modules.")
        
    # 6. Raw Log Viewer & Data Inspector
    st.markdown("---")
    col_log, col_raw = st.columns(2)
    with col_log:
        with st.expander("📜 Log de Validación Crudo"):
            if LOG_FILE.exists():
                with open(LOG_FILE, 'r') as f:
                    st.code(f.read(), language='text')
            else:
                st.warning("Log file not found.")
    
    with col_raw:
        with st.expander("📊 Datos de Métricas (JSON)"):
            if METRICS_FILE.exists():
                st.json(metrics_data)
            else:
                st.warning("Metrics file not found.")

elif metrics_data is None:
    st.warning("Data files not found or invalid. Run AXIOM validation first to generate metrics.")
    st.info("Run: `tools/axiom/install.sh` then `./axiom.sh`")

if not log_entries and LOG_FILE.exists():
     st.warning("Validation log exists but no JSON entries found.")

# --- SIDEBAR DOCUMENTATION ---
st.sidebar.title("📚 Ayuda AXIOM")

with st.sidebar.expander("🎯 ¿Qué es AXIOM?"):
    st.markdown("""
    **AXIOM** (Architectural X-Ray & Integrity Observation Module) es el guardián de calidad de BudgetPro.
    
    **Funciones Principales:**
    - 🛡️ **Validación Arquitectónica**: Previene violaciones de capas (Hexagonal/DDD).
    - 🔒 **Seguridad**: Detecta secretos y vulnerabilidades.
    - 📝 **Gestión de Deuda**: Rastrea TODOs en el código.
    """)

# NEW: Validator Documentation
with st.sidebar.expander("📘 Documentación de Validadores"):
    docs = build_validator_docs()
    selected_doc = st.selectbox("Selecciona Validador", list(docs.keys()))
    if selected_doc:
         st.markdown(docs[selected_doc])

with st.sidebar.expander("📊 Glosario de Métricas"):
    st.markdown("""
    - **Health Score**: Puntuación 0-100 basada en tasa de éxito y penalizaciones por violaciones.
    - **Pass Rate**: % de ejecuciones sin errores bloqueantes.
    - **Avg Blocking**: Promedio de errores críticos por ejecución.
    """)

with st.sidebar.expander("⚙️ Configuración Actual"):
    if config_data:
        st.caption(f"Archivo: `{config_path}`")
        validators = config_data.get('validators', {})
        
        st.markdown("**Validadores Activos:**")
        for name, settings in validators.items():
            if settings.get('enabled', True):
                st.markdown(f"✅ **{name}**")
            else:
                 st.markdown(f"❌ *{name} (Desactivado)*")
        
        # Simple changelog based on file mod time
        import os, datetime
        if os.path.exists(config_path):
            mod_time = os.path.getmtime(config_path)
            dt = datetime.datetime.fromtimestamp(mod_time).strftime('%Y-%m-%d %H:%M:%S')
            st.caption(f"Última modificación: {dt}")

    else:
        st.warning("No se encontró archivo de configuración.")

with st.sidebar.expander("🛠️ Comandos Útiles"):
    st.markdown("""
    **Validación Manual:** `./axiom.sh`
    **Instalar Hook:** `tools/axiom/install.sh`
    """)

# NEW: Export Report
st.sidebar.markdown("---")
st.sidebar.subheader("📄 Exportar Reporte")
if st.sidebar.button("Generar PDF"):
    try:
        pdf_buffer = generate_pdf_report(metrics_data or {},  metrics_data.get('history', []) if metrics_data else [])
        st.sidebar.download_button(
            label="⬇️ Descargar PDF",
            data=pdf_buffer,
            file_name="axiom_dashboard_report.pdf",
            mime="application/pdf"
        )
        st.sidebar.success("PDF Generado!")
    except Exception as e:
        st.sidebar.error(f"Error al generar PDF: {e}")

# NEW: Searchable FAQ
with st.sidebar.expander("❓ FAQ", expanded=True):
    faq_db = {
        "¿Por qué falló mi commit?": "AXIOM detectó una violación bloqueante.",
        "¿Cómo desactivo una regla?": "Edita `.budgetpro/axiom.config.yaml`.",
        "¿Qué es una violación bloqueante?": "Un error crítico que impide el merge.",
        "¿Dónde veo los logs?": "En la sección inferior de este dashboard."
    }
    
    faq_search = st.text_input("🔍 Buscar en FAQ:")
    
    for q, a in faq_db.items():
        if not faq_search or faq_search.lower() in q.lower() or faq_search.lower() in a.lower():
            st.markdown(f"**{q}**\n{a}")
            st.markdown("---")

# Interactive element proof-of-concept
st.sidebar.divider()
st.sidebar.caption("Dashboard v1.0.0")
if metrics_data:
    st.sidebar.text(f"Last Run: {metrics_data['history'][-1]['timestamp']}")
