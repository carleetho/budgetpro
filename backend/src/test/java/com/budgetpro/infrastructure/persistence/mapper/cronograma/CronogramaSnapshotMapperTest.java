package com.budgetpro.infrastructure.persistence.mapper.cronograma;

import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;
import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshotId;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.infrastructure.persistence.entity.cronograma.CronogramaSnapshotEntity;
import com.budgetpro.shared.validation.JsonSchemaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para CronogramaSnapshotMapper.
 * 
 * Verifica que el mapeo bidireccional entre CronogramaSnapshot (dominio) y
 * CronogramaSnapshotEntity (persistencia) funcione correctamente.
 * También verifica que la validación de esquemas JSON se ejecute antes de persistir.
 */
class CronogramaSnapshotMapperTest {

    private CronogramaSnapshotMapper mapper;
    private JsonSchemaValidator jsonSchemaValidator;
    private CronogramaSnapshotId snapshotId;
    private ProgramaObraId programaObraId;
    private PresupuestoId presupuestoId;
    private String fechasJson;
    private String duracionesJson;
    private String secuenciaJson;
    private String calendariosJson;

    @BeforeEach
    void setUp() {
        jsonSchemaValidator = mock(JsonSchemaValidator.class);
        mapper = new CronogramaSnapshotMapper(jsonSchemaValidator);
        snapshotId = CronogramaSnapshotId.nuevo();
        programaObraId = ProgramaObraId.nuevo();
        presupuestoId = PresupuestoId.nuevo();
        
        // JSON válidos según los esquemas
        fechasJson = "{\"programa\":{\"fechaInicio\":\"2024-01-01\",\"fechaFinEstimada\":\"2024-12-31\"},\"actividades\":[]}";
        duracionesJson = "{\"duracionTotalDias\":365,\"actividades\":[]}";
        secuenciaJson = "{\"actividades\":[]}";
        calendariosJson = "{\"calendarios\":[],\"diasFestivos\":[],\"restricciones\":[]}";
    }

    @Test
    void toEntity_debeMapearCorrectamenteDesdeDominio() {
        // Given: Un CronogramaSnapshot del dominio
        CronogramaSnapshot snapshot = CronogramaSnapshot.crear(
                snapshotId,
                programaObraId,
                presupuestoId,
                fechasJson,
                duracionesJson,
                secuenciaJson,
                calendariosJson
        );

        // When: Convertir a Entity
        CronogramaSnapshotEntity entity = mapper.toEntity(snapshot);

        // Then: Todos los campos deben mapearse correctamente
        assertNotNull(entity);
        assertEquals(snapshotId.getValue(), entity.getId());
        assertEquals(programaObraId.getValue(), entity.getProgramaObraId());
        assertEquals(presupuestoId.getValue(), entity.getPresupuestoId());
        assertEquals(fechasJson, entity.getFechasJson());
        assertEquals(duracionesJson, entity.getDuracionesJson());
        assertEquals(secuenciaJson, entity.getSecuenciaJson());
        assertEquals(calendariosJson, entity.getCalendariosJson());
        assertNotNull(entity.getSnapshotDate());
        assertEquals("TEMPORAL-SNAPSHOT-v1", entity.getSnapshotAlgorithm());
        
        // Verificar que se llamó a la validación
        verify(jsonSchemaValidator).validateFechasSnapshot(fechasJson);
        verify(jsonSchemaValidator).validateDuracionesSnapshot(duracionesJson);
        verify(jsonSchemaValidator).validateSecuenciaSnapshot(secuenciaJson);
        verify(jsonSchemaValidator).validateCalendariosSnapshot(calendariosJson);
    }
    
    @Test
    void toEntity_debeValidarEsquemasAntesDePersistir() {
        // Given: Un snapshot con JSON inválido
        String jsonInvalido = "{\"campo\":\"invalido\"}";
        CronogramaSnapshot snapshot = CronogramaSnapshot.crear(
                snapshotId,
                programaObraId,
                presupuestoId,
                jsonInvalido,
                duracionesJson,
                secuenciaJson,
                calendariosJson
        );
        
        // When/Then: Debe lanzar excepción al validar
        doThrow(new IllegalArgumentException("JSON inválido"))
            .when(jsonSchemaValidator).validateFechasSnapshot(jsonInvalido);
        
        assertThrows(IllegalArgumentException.class, () -> mapper.toEntity(snapshot));
    }

    @Test
    void toEntity_debeRetornarNullSiSnapshotEsNull() {
        // When: Convertir null
        CronogramaSnapshotEntity entity = mapper.toEntity(null);

        // Then: Debe retornar null
        assertNull(entity);
    }

    @Test
    void toDomain_debeMapearCorrectamenteDesdeEntity() {
        // Given: Un CronogramaSnapshotEntity de persistencia
        UUID id = UUID.randomUUID();
        UUID programaObraUuid = UUID.randomUUID();
        UUID presupuestoUuid = UUID.randomUUID();
        LocalDateTime snapshotDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        String algoritmo = "TEMPORAL-SNAPSHOT-v2";

        CronogramaSnapshotEntity entity = new CronogramaSnapshotEntity(
                id,
                programaObraUuid,
                presupuestoUuid,
                fechasJson,
                duracionesJson,
                secuenciaJson,
                calendariosJson,
                snapshotDate,
                algoritmo
        );

        // When: Convertir a dominio
        CronogramaSnapshot snapshot = mapper.toDomain(entity);

        // Then: Todos los campos deben mapearse correctamente
        assertNotNull(snapshot);
        assertEquals(id, snapshot.getId().getValue());
        assertEquals(programaObraUuid, snapshot.getProgramaObraId().getValue());
        assertEquals(presupuestoUuid, snapshot.getPresupuestoId().getValue());
        assertEquals(fechasJson, snapshot.getFechasJson());
        assertEquals(duracionesJson, snapshot.getDuracionesJson());
        assertEquals(secuenciaJson, snapshot.getSecuenciaJson());
        assertEquals(calendariosJson, snapshot.getCalendariosJson());
        assertEquals(snapshotDate, snapshot.getSnapshotDate());
        assertEquals(algoritmo, snapshot.getSnapshotAlgorithm());
    }

    @Test
    void toDomain_debeRetornarNullSiEntityEsNull() {
        // When: Convertir null
        CronogramaSnapshot snapshot = mapper.toDomain(null);

        // Then: Debe retornar null
        assertNull(snapshot);
    }

    @Test
    void toEntity_y_toDomain_debenSerBidireccionales() {
        // Given: Un snapshot del dominio
        CronogramaSnapshot snapshotOriginal = CronogramaSnapshot.crear(
                snapshotId,
                programaObraId,
                presupuestoId,
                fechasJson,
                duracionesJson,
                secuenciaJson,
                calendariosJson
        );

        // When: Convertir a Entity y de vuelta a dominio
        CronogramaSnapshotEntity entity = mapper.toEntity(snapshotOriginal);
        CronogramaSnapshot snapshotRecuperado = mapper.toDomain(entity);

        // Then: El snapshot recuperado debe ser igual al original
        assertNotNull(snapshotRecuperado);
        assertEquals(snapshotOriginal.getId(), snapshotRecuperado.getId());
        assertEquals(snapshotOriginal.getProgramaObraId(), snapshotRecuperado.getProgramaObraId());
        assertEquals(snapshotOriginal.getPresupuestoId(), snapshotRecuperado.getPresupuestoId());
        assertEquals(snapshotOriginal.getFechasJson(), snapshotRecuperado.getFechasJson());
        assertEquals(snapshotOriginal.getDuracionesJson(), snapshotRecuperado.getDuracionesJson());
        assertEquals(snapshotOriginal.getSecuenciaJson(), snapshotRecuperado.getSecuenciaJson());
        assertEquals(snapshotOriginal.getCalendariosJson(), snapshotRecuperado.getCalendariosJson());
        assertEquals(snapshotOriginal.getSnapshotAlgorithm(), snapshotRecuperado.getSnapshotAlgorithm());
    }

    @Test
    void toEntity_debeMapearSnapshotConFechaYAlgoritmoEspecificos() {
        // Given: Un snapshot reconstruido con fecha y algoritmo específicos
        LocalDateTime fechaEspecifica = LocalDateTime.of(2024, 6, 15, 14, 30);
        String algoritmoEspecifico = "TEMPORAL-SNAPSHOT-v3";

        CronogramaSnapshot snapshot = CronogramaSnapshot.reconstruir(
                snapshotId,
                programaObraId,
                presupuestoId,
                fechasJson,
                duracionesJson,
                secuenciaJson,
                calendariosJson,
                fechaEspecifica,
                algoritmoEspecifico
        );

        // When: Convertir a Entity
        CronogramaSnapshotEntity entity = mapper.toEntity(snapshot);

        // Then: La fecha y algoritmo deben preservarse
        assertEquals(fechaEspecifica, entity.getSnapshotDate());
        assertEquals(algoritmoEspecifico, entity.getSnapshotAlgorithm());
    }

    @Test
    void toDomain_debeMapearJsonbComplejos() {
        // Given: Un Entity con JSONB complejos (válidos según esquema)
        String fechasJsonComplejo = """
                {
                    "programa": {
                        "fechaInicio": "2024-01-01",
                        "fechaFinEstimada": "2024-12-31"
                    },
                    "actividades": [
                        {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "partidaId": "550e8400-e29b-41d4-a716-446655440001",
                            "fechaInicio": "2024-01-01",
                            "fechaFin": "2024-02-01"
                        },
                        {
                            "id": "550e8400-e29b-41d4-a716-446655440002",
                            "partidaId": "550e8400-e29b-41d4-a716-446655440003",
                            "fechaInicio": "2024-02-01",
                            "fechaFin": "2024-03-01"
                        }
                    ]
                }
                """;

        CronogramaSnapshotEntity entity = new CronogramaSnapshotEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                fechasJsonComplejo,
                duracionesJson,
                secuenciaJson,
                calendariosJson,
                LocalDateTime.now(),
                "TEMPORAL-SNAPSHOT-v1"
        );

        // When: Convertir a dominio
        CronogramaSnapshot snapshot = mapper.toDomain(entity);

        // Then: Los JSONB complejos deben preservarse
        assertNotNull(snapshot);
        assertTrue(snapshot.getFechasJson().contains("550e8400-e29b-41d4-a716-446655440000"));
        assertTrue(snapshot.getFechasJson().contains("550e8400-e29b-41d4-a716-446655440002"));
    }
}
