# bp-explore-delegate

El usuario necesita **exploración amplia** del codebase (solo lectura).

1. Lanza un subagente **explore** con un prompt que incluya: objetivo, módulos o palabras clave, y el formato de salida deseado (**rutas de archivo**, resumen por capas, lista de incógnitas).
2. Integra el resultado en el hilo: no repitas exploración ya hecha por el subagente salvo que falte evidencia.
3. Si a continuación se implementará negocio, el agente principal debe cumplir **Modo B** (canónicos) según `.cursor/rules/budgetpro-agent-governance.mdc`.
