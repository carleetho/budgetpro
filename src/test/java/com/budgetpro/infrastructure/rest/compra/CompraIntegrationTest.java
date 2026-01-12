package com.budgetpro.infrastructure.rest.compra;

import com.budgetpro.application.compra.dto.RegistrarCompraDirectaResponse;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import com.budgetpro.domain.finanzas.presupuesto.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.logistica.inventario.InventarioItem;
import com.budgetpro.domain.logistica.inventario.InventarioId;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.recurso.model.Recurso;
import com.budgetpro.domain.recurso.model.RecursoId;
import com.budgetpro.domain.recurso.port.out.RecursoRepository;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.repository.BilleteraJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.InventarioJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.MovimientoCajaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.compra.CompraJpaRepository;
import com.budgetpro.infrastructure.rest.compra.dto.RegistrarCompraDirectaRequest;
import com.budgetpro.infrastructure.security.JwtTestHelper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.support.TransactionTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de Integración Crítico para el flujo completo de Compra Directa.
 * 
 * Según Directiva Maestra v2.0 (QA-02): Test Crítico que valida:
 * - Crear Partida
 * - Ingresar Saldo
 * - Comprar
 * - Verificar rebaja de Saldo y aumento de Stock
 * 
 * Extiende de AbstractIntegrationTest que proporciona:
 * - Base de datos PostgreSQL real en contenedor (Testcontainers)
 * - Migraciones de Flyway ejecutadas automáticamente
 * - Contexto Spring Boot completo
 */
class CompraIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private BilleteraRepository billeteraRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private PresupuestoJpaRepository presupuestoJpaRepository;

    @Autowired
    private BilleteraJpaRepository billeteraJpaRepository;

    @Autowired
    private RecursoJpaRepository recursoJpaRepository;

    @Autowired
    private InventarioJpaRepository inventarioJpaRepository;

    @Autowired
    private MovimientoCajaJpaRepository movimientoCajaJpaRepository;

    @Autowired
    private CompraJpaRepository compraJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @LocalServerPort
    private int port;

    private UUID proyectoId;
    private UUID presupuestoId;
    private UUID partidaId;
    private UUID recursoId1;
    private UUID recursoId2;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Configurar SecurityContext con usuario autenticado para auditoría
        UUID testUserId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                testUserId.toString(),
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Limpiar base de datos antes de cada test usando repositorios JPA
        movimientoCajaJpaRepository.deleteAll();
        compraJpaRepository.deleteAll(); // Limpiar compras para evitar OptimisticLockingFailureException
        inventarioJpaRepository.deleteAll();
        billeteraJpaRepository.deleteAll();
        presupuestoJpaRepository.deleteAll();
        recursoJpaRepository.deleteAll();
        
        // CRÍTICO: Flush y clear después de limpiar para asegurar que la limpieza se complete
        movimientoCajaJpaRepository.flush();
        compraJpaRepository.flush();
        inventarioJpaRepository.flush();
        billeteraJpaRepository.flush();
        presupuestoJpaRepository.flush();
        recursoJpaRepository.flush();
        entityManager.clear();

        // 1. Crear Proyecto directamente en BD (no hay entidad JPA)
        // Usar un proyectoId único para cada test para evitar conflictos
        proyectoId = UUID.randomUUID();
        // Usar TransactionTemplate para ejecutar queries nativas dentro de una
        // transacción explícita
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createNativeQuery(
                    "INSERT INTO proyecto (id, nombre, estado, created_at, updated_at) VALUES (?, ?, ?, now(), now())")
                    .setParameter(1, proyectoId)
                    .setParameter(2, "Proyecto Test")
                    .setParameter(3, "ACTIVO")
                    .executeUpdate();
        });

        // 2. Crear Presupuesto en dominio directamente (evita
        // LazyInitializationException)
        presupuestoId = UUID.randomUUID();
        PresupuestoId presupuestoIdVO = PresupuestoId.of(presupuestoId);
        Presupuesto presupuesto = Presupuesto.crear(presupuestoIdVO, proyectoId);

        // 3. Agregar Partida al Presupuesto
        com.budgetpro.domain.finanzas.partida.CodigoPartida codigo = com.budgetpro.domain.finanzas.partida.CodigoPartida
                .of("01.01.001");
        com.budgetpro.domain.finanzas.model.Monto montoPresupuestado = com.budgetpro.domain.finanzas.model.Monto
                .of(new BigDecimal("100000.00"));

        com.budgetpro.domain.finanzas.partida.Partida partida = presupuesto.agregarPartida(
                codigo,
                "Partida Test",
                com.budgetpro.domain.recurso.model.TipoRecurso.MATERIAL,
                montoPresupuestado);
        partidaId = partida.getId().getValue();

        // 4. Persistir Presupuesto con Partida
        presupuestoRepository.save(presupuesto);

        // 4. Crear Billetera e ingresar saldo
        BilleteraId billeteraId = BilleteraId.generate();
        Billetera billetera = Billetera.crear(billeteraId, proyectoId);
        billetera.ingresar(new BigDecimal("50000.00"), "Saldo inicial", null);
        billeteraRepository.save(billetera);

        // 5. Crear Recursos
        recursoId1 = UUID.randomUUID();
        Recurso recurso1 = Recurso.crear(
                RecursoId.of(recursoId1),
                "Cemento",
                com.budgetpro.domain.recurso.model.TipoRecurso.MATERIAL,
                "KG");
        recursoRepository.save(recurso1);

        recursoId2 = UUID.randomUUID();
        Recurso recurso2 = Recurso.crear(
                RecursoId.of(recursoId2),
                "Arena",
                com.budgetpro.domain.recurso.model.TipoRecurso.MATERIAL,
                "M3");
        recursoRepository.save(recurso2);

        // 6. Crear Inventarios para los recursos directamente en BD
        // NOTA: El dominio InventarioItem no tiene proyectoId, pero la tabla sí lo requiere
        // Por ahora, creamos las entidades directamente para el test
        com.budgetpro.infrastructure.persistence.entity.RecursoEntity recursoEntity1 = recursoJpaRepository
                .findById(recursoId1).orElseThrow();
        com.budgetpro.infrastructure.persistence.entity.RecursoEntity recursoEntity2 = recursoJpaRepository
                .findById(recursoId2).orElseThrow();

        // CREACIÓN DE INVENTARIOS (Corregido: version = null)
        com.budgetpro.infrastructure.persistence.entity.InventarioItemEntity inventarioEntity1 = new com.budgetpro.infrastructure.persistence.entity.InventarioItemEntity(
                UUID.randomUUID(),
                proyectoId,
                recursoEntity1,
                new BigDecimal("1000.00"), 
                new BigDecimal("50.00"), 
                null // <--- FIX: Version null
        );
        inventarioJpaRepository.save(inventarioEntity1);

        com.budgetpro.infrastructure.persistence.entity.InventarioItemEntity inventarioEntity2 = new com.budgetpro.infrastructure.persistence.entity.InventarioItemEntity(
                UUID.randomUUID(),
                proyectoId,
                recursoEntity2,
                new BigDecimal("1000.00"), 
                new BigDecimal("30.00"), 
                null // <--- FIX: Version null
        );
        inventarioJpaRepository.save(inventarioEntity2);

        // === LIMPIEZA DE CONTEXTO (CRÍTICO PARA EVITAR 409 CONFLICT) ===
        billeteraJpaRepository.flush();
        inventarioJpaRepository.flush();
        entityManager.clear(); // Obliga a leer de BD fresca en el test
        // ==============================================================

        // 7. Generar token JWT válido para autenticación (usar el mismo testUserId configurado arriba)
        jwtToken = JwtTestHelper.generateValidToken(testUserId);
    }

    @Test
    void testRegistrarCompraDirecta_FlujoCompleto() {
            // Given: Saldo inicial de 50000.00
            Optional<Billetera> billeteraAntes = billeteraRepository.findByProyectoId(proyectoId);
            assertThat(billeteraAntes).isPresent();
            BigDecimal saldoInicial = billeteraAntes.get().getSaldoActual();
            assertThat(saldoInicial).isEqualByComparingTo(new BigDecimal("50000.00"));

            // Given: Inventarios con stock inicial de 1000.00
            List<InventarioItem> inventariosAntes = inventarioRepository.findAllByProyectoIdAndRecursoIds(
                            proyectoId,
                            List.of(RecursoId.of(recursoId1), RecursoId.of(recursoId2))).values().stream().toList();
            assertThat(inventariosAntes).hasSize(2);
            BigDecimal stockInicial1 = inventariosAntes.stream()
                            .filter(i -> i.getRecursoId().getValue().equals(recursoId1))
                            .findFirst()
                            .map(InventarioItem::getStock)
                            .orElse(BigDecimal.ZERO);
            // Assert del stock inicial: 1000.00
            assertThat(stockInicial1).isEqualByComparingTo(new BigDecimal("1000.00"));

            // Given: Request de compra directa
            RegistrarCompraDirectaRequest request = new RegistrarCompraDirectaRequest(
                            proyectoId,
                            presupuestoId,
                            List.of(
                                            new RegistrarCompraDirectaRequest.DetalleCompraRequest(
                                                            recursoId1,
                                                            new BigDecimal("100.00"), // 100 KG de cemento
                                                            new BigDecimal("50.00") // $50 por KG
                                            ),
                                            new RegistrarCompraDirectaRequest.DetalleCompraRequest(
                                                            recursoId2,
                                                            new BigDecimal("10.00"), // 10 M3 de arena
                                                            new BigDecimal("30.00") // $30 por M3
                                            )));

            // Total esperado: (100 * 50) + (10 * 30) = 5000 + 300 = 5300

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwtToken); // Agregar token JWT
            HttpEntity<RegistrarCompraDirectaRequest> httpEntity = new HttpEntity<>(request, headers);

            // When: Registrar compra directa
            // Usar ResponseEntity<String> para capturar tanto éxito como error en una sola
            // llamada
            ResponseEntity<String> responseRaw = restTemplate.exchange(
                            "http://localhost:" + port + "/api/v1/compras/directa",
                            HttpMethod.POST,
                            httpEntity,
                            String.class);

            // Debug: Si el status no es 201, imprimir el cuerpo de la respuesta para
            // diagnóstico
            if (responseRaw.getStatusCode() != HttpStatus.CREATED) {
                    System.err.println("=== DEBUG: Error HTTP del servidor ===");
                    System.err.println("Status: " + responseRaw.getStatusCode());
                    System.err.println("Response Body: " + responseRaw.getBody());
            }

            // Then: Assert Status 201 CREATED
            assertThat(responseRaw.getStatusCode())
                            .as("El endpoint debe retornar 201 CREATED. Si retorna 409 CONFLICT, verifica saldo/stock suficiente.\nStatus actual: %s\nResponse Body: %s",
                                            responseRaw.getStatusCode(), responseRaw.getBody())
                            .isEqualTo(HttpStatus.CREATED);

            // Parsear el JSON del body a RegistrarCompraDirectaResponse
            ObjectMapper objectMapper = new ObjectMapper();
            RegistrarCompraDirectaResponse body;
            try {
                    body = objectMapper.readValue(responseRaw.getBody(), RegistrarCompraDirectaResponse.class);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new AssertionError("No se pudo parsear la respuesta del servidor: " + responseRaw.getBody(),
                                    e);
            }
            assertThat(body).isNotNull();
            assertThat(body.compraId()).isNotNull();
            assertThat(body.estado()).isEqualTo(com.budgetpro.domain.finanzas.compra.EstadoCompra.CONFIRMADA);
            
            // Then: Verificar que la respuesta incluye saldoActual
            assertThat(body.saldoActual()).isNotNull();
            BigDecimal saldoEsperado = saldoInicial.subtract(new BigDecimal("5300.00"));
            assertThat(body.saldoActual()).isEqualByComparingTo(saldoEsperado);
            
            // Then: Verificar que la respuesta incluye stockActualizado
            assertThat(body.stockActualizado()).isNotNull();
            assertThat(body.stockActualizado()).hasSize(2);
            
            // Verificar stock del recurso 1
            var stockRecurso1 = body.stockActualizado().stream()
                    .filter(s -> s.recursoId().equals(recursoId1))
                    .findFirst()
                    .orElseThrow();
            assertThat(stockRecurso1.stockAnterior()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(stockRecurso1.stockActual()).isEqualByComparingTo(new BigDecimal("1100.00"));
            
            // Verificar stock del recurso 2 (compra 10.00 unidades de 1000.00 inicial = 1010.00)
            var stockRecurso2 = body.stockActualizado().stream()
                    .filter(s -> s.recursoId().equals(recursoId2))
                    .findFirst()
                    .orElseThrow();
            assertThat(stockRecurso2.stockAnterior()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(stockRecurso2.stockActual()).isEqualByComparingTo(new BigDecimal("1010.00"));

            // Then: Verificar que el saldo se rebajó correctamente (verificación adicional desde repositorio)
            Optional<Billetera> billeteraDespues = billeteraRepository.findByProyectoId(proyectoId);
            assertThat(billeteraDespues).isPresent();
            BigDecimal saldoFinal = billeteraDespues.get().getSaldoActual();
            assertThat(saldoFinal).isEqualByComparingTo(saldoEsperado);

            // Then: Verificar que el stock aumentó correctamente
            List<InventarioItem> inventariosDespues = inventarioRepository.findAllByProyectoIdAndRecursoIds(
                            proyectoId,
                            List.of(RecursoId.of(recursoId1), RecursoId.of(recursoId2))).values().stream().toList();

            BigDecimal stockFinal1 = inventariosDespues.stream()
                            .filter(i -> i.getRecursoId().getValue().equals(recursoId1))
                            .findFirst()
                            .map(InventarioItem::getStock)
                            .orElse(BigDecimal.ZERO);
            // Stock final esperado: 1000.00 (inicial) + 100.00 (compra) = 1100.00
            assertThat(stockFinal1).isEqualByComparingTo(new BigDecimal("1100.00"));

            BigDecimal stockFinal2 = inventariosDespues.stream()
                            .filter(i -> i.getRecursoId().getValue().equals(recursoId2))
                            .findFirst()
                            .map(InventarioItem::getStock)
                            .orElse(BigDecimal.ZERO);
            // Stock final esperado: 1000.00 (inicial) + 10.00 (compra) = 1010.00
            assertThat(stockFinal2).isEqualByComparingTo(new BigDecimal("1010.00")); // 10 M3 agregados al stock inicial

            // ============================================================================
            // VALIDACIÓN CRÍTICA DE TRAZABILIDAD FINANCIERA (MovimientoCaja)
            // ============================================================================
            // Esta aserción BLINDA el fix de persistencia transaccional de MovimientoCaja.
            // Si esta validación falla, significa que la trazabilidad financiera se rompió.

            UUID billeteraId = billeteraDespues.get().getId().getValue();
            List<com.budgetpro.infrastructure.persistence.entity.MovimientoCajaEntity> movimientos = movimientoCajaJpaRepository
                            .findByBilleteraIdOrderByCreatedAtDesc(billeteraId);

            // ASERCIÓN 1: La lista de movimientos NO debe estar vacía
            // Esto garantiza que los movimientos se están persistiendo en la base de datos
            assertThat(movimientos)
                            .as("La lista de movimientos de caja NO debe estar vacía. Si está vacía, la persistencia de MovimientoCaja falló.")
                            .isNotEmpty();

            // ASERCIÓN 2: Debe haber al menos 1 movimiento de tipo EGRESO
            // Esto valida que el egreso de la compra se registró correctamente
            List<com.budgetpro.infrastructure.persistence.entity.MovimientoCajaEntity> movimientosEgreso = movimientos
                            .stream()
                            .filter(m -> "EGRESO".equals(m.getTipo()))
                            .toList();
            assertThat(movimientosEgreso)
                            .as("Debe haber al menos 1 movimiento de tipo EGRESO. Si no hay egresos, la compra no generó movimiento de caja.")
                            .isNotEmpty();

            // ASERCIÓN 3: El movimiento más reciente debe ser el egreso de la compra
            // Ordenados por fecha descendente, el primero es el más reciente
            com.budgetpro.infrastructure.persistence.entity.MovimientoCajaEntity ultimoMovimiento = movimientos.get(0);

            // ASERCIÓN 4: Verificar que el tipo sea EGRESO
            assertThat(ultimoMovimiento.getTipo())
                            .as("El movimiento más reciente debe ser de tipo EGRESO (gasto por compra). Tipo actual: %s",
                                            ultimoMovimiento.getTipo())
                            .isEqualTo("EGRESO");

            // ASERCIÓN 5: Verificar que el monto del movimiento coincida con el total de la
            // compra
            // Total esperado: (100 KG * $50) + (10 M3 * $30) = 5000 + 300 = 5300.00
            BigDecimal totalCompraEsperado = new BigDecimal("5300.00");
            assertThat(ultimoMovimiento.getMonto())
                            .as("El monto del movimiento EGRESO debe coincidir con el total de la compra. Esperado: %s, Actual: %s",
                                            totalCompraEsperado, ultimoMovimiento.getMonto())
                            .isEqualByComparingTo(totalCompraEsperado);

            // ASERCIÓN 6: Verificar que la referencia contenga información de la compra
            assertThat(ultimoMovimiento.getReferencia())
                            .as("La referencia del movimiento debe contener información de la compra. Referencia actual: %s",
                                            ultimoMovimiento.getReferencia())
                            .isNotNull()
                            .containsIgnoringCase("Compra");

            // ASERCIÓN 7: Verificar que el movimiento tiene la referencia correcta a la
            // billetera
            assertThat(ultimoMovimiento.getBilletera().getId())
                            .as("El movimiento debe estar asociado a la billetera correcta. Esperado: %s, Actual: %s",
                                            billeteraId, ultimoMovimiento.getBilletera().getId())
                            .isEqualTo(billeteraId);

            // ASERCIÓN 8: Verificar que el movimiento tiene un ID válido (persistido)
            assertThat(ultimoMovimiento.getId())
                            .as("El movimiento debe tener un ID válido (persistido en BD). Si es null, la persistencia falló.")
                            .isNotNull();
    }
}
