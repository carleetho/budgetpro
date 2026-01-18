CURRENT_TASK: Implementación de Leyes Canónicas de Integridad Financiera
Estado: COMPLETADO
Fecha: 2026-01-18

Resumen de ejecución:
- VD-02 y CD-04 implementadas en dominio y servicio.
- Validación obligatoria y reserva por partida aplicada en compras.
- Tests unitarios e integración ejecutados (ver sección de pruebas).

Pruebas ejecutadas:
- ./mvnw test -Dtest=PartidaTest,BilleteraTest,MovimientoCajaTest,SaldoInsuficienteExceptionTest,ProcesarCompraServiceTest
- ./mvnw test -Dtest=PartidaEntityVersionTest
- ./mvnw test -Dtest=ProcesarCompraIntegrationTest
🎯 Objetivo

Eliminar la "nube de humo" lógica y técnica. Implementar el control presupuestario preventivo y el gobierno de evidencias estricto según el acuerdo de auditoría 2026.
📜 Leyes Canónicas a Aplicar

    Fórmula de Saldo Disponible (VD-02): Saldo_Disponible = Presupuesto_Asignado - (Gastos_Reales + Compromisos_Pendientes)

        Compromisos_Pendientes: Órdenes de compra aprobadas pero no liquidadas.

    Momento del Compromiso: El presupuesto se resta en el instante de la Aprobación de la Compra, no en el pago.

    Bloqueo de Evidencia (CD-04): No se permiten egresos si existen >3 movimientos en estado PENDIENTE_DE_EVIDENCIA.

🛠️ Acciones Requeridas
1. Modelos de Dominio (backend/src/main/java/com/budgetpro/domain/)

    finanzas/partida/model/Partida.java:

        Añadir campo/lógica para calcular saldoDisponible usando la fórmula canónica.

        Añadir método reservarSaldo(BigDecimal monto) que incremente los compromisos.

    finanzas/model/MovimientoBilletera.java:

        Añadir estado PENDIENTE_DE_EVIDENCIA al Enum de estados.

    finanzas/model/Billetera.java:

        Modificar egresar() para validar que contarMovimientosSinEvidencia() <= 3.

2. Servicios de Dominio

    logistica/compra/service/ProcesarCompraService.java:

        ELIMINAR "Opcional MVP".

        Implementar validación obligatoria: Si compra.total > partida.getSaldoDisponible(), lanzar SaldoInsuficienteException.

        Invocar partida.reservarSaldo() al aprobar la compra.

3. Documentación (docs/)

    Actualizar BUSINESS_MANIFESTO.md y FINANZAS_BILLETERA_SPECS.md con estas nuevas definiciones para mantener la sincronía entre código y verdad canónica.

⚠️ Restricciones (Leyes de Hierro de Cursor)

    NO HARDCODE: Prohibido escribir API Keys o credenciales reales. Usa ${RESEND_API_KEY} y ${DB_PASSWORD}.

    COMPILACIÓN: El código debe ser sintácticamente correcto. Usa ./mvnw clean compile para verificar.

    GIT: No realizar git commit. El usuario ejecutará ./secure-commit.sh manualmente.