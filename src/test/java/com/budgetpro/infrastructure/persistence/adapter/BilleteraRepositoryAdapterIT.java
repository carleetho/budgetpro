package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.billetera.Billetera;
import com.budgetpro.domain.finanzas.billetera.BilleteraId;
import com.budgetpro.domain.finanzas.billetera.Monto;
import com.budgetpro.domain.finanzas.billetera.Movimiento;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.entity.MovimientoEntity;
import com.budgetpro.infrastructure.persistence.repository.BilleteraJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.MovimientoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de Integración para BilleteraRepositoryAdapter.
 * 
 * Prueba el flujo completo de persistencia del agregado Billetera:
 * - Creación de billetera
 * - Ingreso de fondos
 * - Egreso de fondos
 * - Recuperación desde BD y verificación de estado
 * 
 * Extiende de AbstractIntegrationTest que proporciona:
 * - Base de datos PostgreSQL real en contenedor
 * - Migraciones de Flyway ejecutadas automáticamente
 * - Contexto Spring Boot completo con transacciones
 */
@Transactional
class BilleteraRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private BilleteraRepository billeteraRepository; // Puerto del dominio (implementado por adapter)

    @Autowired
    private BilleteraJpaRepository billeteraJpaRepository; // Para verificaciones directas en BD

    @Autowired
    private MovimientoJpaRepository movimientoJpaRepository; // Para verificaciones directas en BD

    private UUID proyectoId;

    @BeforeEach
    void setUp() {
        proyectoId = UUID.randomUUID();
        // Limpiar la base de datos antes de cada test
        movimientoJpaRepository.deleteAll();
        billeteraJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Flujo completo: Crear -> Ingresar -> Egresar -> Verificar saldo y movimientos")
    void testFlujoCompleto_CrearIngresarEgresar() {
        // ===== FASE 1: CREAR BILLETERA =====
        Billetera billetera = Billetera.crear(proyectoId);
        BilleteraId billeteraId = billetera.getId();
        
        // Persistir billetera inicial
        billeteraRepository.save(billetera);
        
        // Verificar que se guardó en BD
        Optional<BilleteraEntity> entityOpt = billeteraJpaRepository.findById(billeteraId.getValue());
        assertThat(entityOpt).isPresent();
        BilleteraEntity entityCreada = entityOpt.get();
        assertThat(entityCreada.getProyectoId()).isEqualTo(proyectoId);
        assertThat(entityCreada.getSaldoActual()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entityCreada.getVersion()).isEqualTo(1L); // Hibernate establece version = 1 tras insert

        // ===== FASE 2: INGRESAR FONDOS =====
        Monto montoIngreso = Monto.of(new BigDecimal("1000.5000"));
        String referenciaIngreso = "Pago inicial de proyecto";
        String evidenciaIngreso = "https://example.com/comprobante1.pdf";
        
        Movimiento movimientoIngreso = billetera.ingresar(montoIngreso, referenciaIngreso, evidenciaIngreso);
        assertThat(movimientoIngreso).isNotNull();
        assertThat(movimientoIngreso.getMonto()).isEqualTo(montoIngreso);
        assertThat(billetera.getSaldoActual().toBigDecimal()).isEqualByComparingTo(montoIngreso.toBigDecimal()); // Saldo = 1000.5000
        
        // Persistir el ingreso
        billeteraRepository.save(billetera);
        
        // Verificar que se guardó en BD
        Optional<BilleteraEntity> entityOpt2 = billeteraJpaRepository.findById(billeteraId.getValue());
        assertThat(entityOpt2).isPresent();
        BilleteraEntity entityConIngreso = entityOpt2.get();
        assertThat(entityConIngreso.getSaldoActual()).isEqualByComparingTo(new BigDecimal("1000.5000"));
        assertThat(entityConIngreso.getVersion()).isEqualTo(2L); // Version incrementada
        
        // Verificar que el movimiento se guardó
        List<MovimientoEntity> movimientosIngreso = movimientoJpaRepository.findByBilleteraIdOrderByFechaDesc(billeteraId.getValue());
        assertThat(movimientosIngreso).hasSize(1);
        MovimientoEntity movimientoEntity1 = movimientosIngreso.get(0);
        assertThat(movimientoEntity1.getMonto()).isEqualByComparingTo(new BigDecimal("1000.5000"));
        assertThat(movimientoEntity1.getReferencia()).isEqualTo(referenciaIngreso);
        assertThat(movimientoEntity1.getEvidenciaUrl()).isEqualTo(evidenciaIngreso);

        // ===== FASE 3: EGRESAR FONDOS =====
        Monto montoEgreso = Monto.of(new BigDecimal("350.2500"));
        String referenciaEgreso = "Compra de materiales";
        String evidenciaEgreso = "https://example.com/factura1.pdf";
        
        Movimiento movimientoEgreso = billetera.egresar(montoEgreso, referenciaEgreso, evidenciaEgreso);
        assertThat(movimientoEgreso).isNotNull();
        
        // Saldo esperado: 1000.5000 - 350.2500 = 650.2500
        BigDecimal saldoEsperado = new BigDecimal("650.2500");
        assertThat(billetera.getSaldoActual().toBigDecimal()).isEqualByComparingTo(saldoEsperado);
        
        // Persistir el egreso
        billeteraRepository.save(billetera);
        
        // ===== FASE 4: RECUPERAR DESDE BD Y VERIFICAR =====
        // Recuperar la billetera desde BD usando el repositorio del dominio
        Optional<Billetera> billeteraRecuperadaOpt = billeteraRepository.findByProyectoId(proyectoId);
        assertThat(billeteraRecuperadaOpt).isPresent();
        
        Billetera billeteraRecuperada = billeteraRecuperadaOpt.get();
        
        // Verificar ID
        assertThat(billeteraRecuperada.getId()).isEqualTo(billeteraId);
        assertThat(billeteraRecuperada.getProyectoId()).isEqualTo(proyectoId);
        
        // Verificar saldo final esperado (650.2500)
        assertThat(billeteraRecuperada.getSaldoActual().toBigDecimal()).isEqualByComparingTo(saldoEsperado);
        
        // Verificar version (debe ser 3 después de los 3 saves: crear, ingresar, egresar)
        assertThat(billeteraRecuperada.getVersion()).isEqualTo(3L);
        
        // Verificar que no hay movimientos nuevos pendientes (ya fueron persistidos)
        assertThat(billeteraRecuperada.getMovimientosNuevos()).isEmpty();
        
        // ===== VERIFICACIÓN FINAL: Consultar BD directamente =====
        Optional<BilleteraEntity> entityFinalOpt = billeteraJpaRepository.findByProyectoId(proyectoId);
        assertThat(entityFinalOpt).isPresent();
        BilleteraEntity entityFinal = entityFinalOpt.get();
        
        // Verificar saldo en BD
        assertThat(entityFinal.getSaldoActual()).isEqualByComparingTo(new BigDecimal("650.2500"));
        assertThat(entityFinal.getVersion()).isEqualTo(3L);
        
        // Verificar que existen 2 movimientos en BD
        List<MovimientoEntity> todosLosMovimientos = movimientoJpaRepository.findByBilleteraIdOrderByFechaDesc(billeteraId.getValue());
        assertThat(todosLosMovimientos).hasSize(2);
        
        // Verificar primer movimiento (EGRESO - más reciente, ordenado DESC)
        MovimientoEntity movimientoEgresoEntity = todosLosMovimientos.get(0);
        assertThat(movimientoEgresoEntity.getMonto()).isEqualByComparingTo(new BigDecimal("350.2500"));
        assertThat(movimientoEgresoEntity.getReferencia()).isEqualTo(referenciaEgreso);
        assertThat(movimientoEgresoEntity.getEvidenciaUrl()).isEqualTo(evidenciaEgreso);
        
        // Verificar segundo movimiento (INGRESO - más antiguo)
        MovimientoEntity movimientoIngresoEntity = todosLosMovimientos.get(1);
        assertThat(movimientoIngresoEntity.getMonto()).isEqualByComparingTo(new BigDecimal("1000.5000"));
        assertThat(movimientoIngresoEntity.getReferencia()).isEqualTo(referenciaIngreso);
        assertThat(movimientoIngresoEntity.getEvidenciaUrl()).isEqualTo(evidenciaIngreso);
    }

    @Test
    @DisplayName("Egresar sin saldo suficiente debe lanzar SaldoInsuficienteException")
    void testEgresar_SaldoInsuficiente() {
        // Given: Crear billetera con saldo 100
        Billetera billetera = Billetera.crear(proyectoId);
        billetera.ingresar(Monto.of(new BigDecimal("100.0000")), "Ingreso inicial", null);
        billeteraRepository.save(billetera);
        
        // Recargar desde BD para tener version actualizado
        Billetera billeteraRecargada = billeteraRepository.findByProyectoId(proyectoId).orElseThrow();
        
        // When/Then: Intentar egresar más de lo disponible (200)
        assertThatThrownBy(() -> 
            billeteraRecargada.egresar(
                Monto.of(new BigDecimal("200.0000")), 
                "Egreso excesivo", 
                null
            )
        )
        .isInstanceOf(com.budgetpro.domain.finanzas.billetera.exception.SaldoInsuficienteException.class);
        
        // Verificar que el saldo no cambió
        Optional<Billetera> billeteraFinal = billeteraRepository.findByProyectoId(proyectoId);
        assertThat(billeteraFinal).isPresent();
        assertThat(billeteraFinal.get().getSaldoActual().toBigDecimal()).isEqualByComparingTo(new BigDecimal("100.0000"));
    }

    @Test
    @DisplayName("findByProyectoId debe retornar Optional.empty() si no existe")
    void testFindByProyectoId_NoExiste() {
        // Given: Proyecto sin billetera
        UUID proyectoInexistente = UUID.randomUUID();
        
        // When
        Optional<Billetera> result = billeteraRepository.findByProyectoId(proyectoInexistente);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById debe retornar la billetera correcta")
    void testFindById() {
        // Given: Crear y persistir billetera
        Billetera billetera = Billetera.crear(proyectoId);
        BilleteraId billeteraId = billetera.getId();
        billeteraRepository.save(billetera);
        
        // When: Buscar por ID
        Optional<Billetera> result = billeteraRepository.findById(billeteraId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(billeteraId);
        assertThat(result.get().getProyectoId()).isEqualTo(proyectoId);
    }
}
