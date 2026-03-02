package com.budgetpro.application.finanzas.evm;

import com.budgetpro.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EVMTimeSeriesMigrationIntegrationTest extends AbstractIntegrationTest {

    private static final UUID PROYECTO_CON_SNAPSHOT = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
    private static final UUID PROYECTO_SIN_SNAPSHOT = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2");

    private static final String BACKFILL_DML = """
            INSERT INTO evm_time_series (
                id,
                proyecto_id,
                fecha_corte,
                periodo,
                moneda,
                pv,
                ev,
                ac,
                bac,
                bac_ajustado,
                cpi,
                spi,
                created_at,
                updated_at,
                created_by
            )
            SELECT
                gen_random_uuid() AS id,
                s.proyecto_id,
                CAST(s.fecha_corte AS DATE) AS fecha_corte,
                1 AS periodo,
                'USD' AS moneda,
                s.pv,
                s.ev,
                s.ac,
                s.bac,
                s.bac AS bac_ajustado,
                s.cpi,
                s.spi,
                NOW() AS created_at,
                NOW() AS updated_at,
                '00000000-0000-0000-0000-000000000001'::UUID AS created_by
            FROM (
                SELECT DISTINCT ON (proyecto_id)
                    proyecto_id,
                    fecha_corte,
                    pv,
                    ev,
                    ac,
                    bac,
                    cpi,
                    spi
                FROM evm_snapshot
                ORDER BY proyecto_id, fecha_corte DESC
            ) s
            WHERE NOT EXISTS (
                SELECT 1
                FROM evm_time_series ets
                WHERE ets.proyecto_id = s.proyecto_id
                  AND ets.fecha_corte = CAST(s.fecha_corte AS DATE)
            );
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql(statements = {
            "DELETE FROM evm_time_series WHERE proyecto_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1'",
            "DELETE FROM evm_snapshot WHERE proyecto_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1'",
            "DELETE FROM proyecto WHERE id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1'",
            "INSERT INTO proyecto (id, nombre, ubicacion, estado, moneda, presupuesto_total, version, created_by) " +
                    "VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'REQ61-MIGR-01', 'Test', 'ACTIVO', 'USD', 1000, 0, '00000000-0000-0000-0000-000000000001')",
            "INSERT INTO evm_snapshot (id, proyecto_id, fecha_corte, fecha_calculo, pv, ev, ac, bac, cv, sv, cpi, spi, eac, etc, vac, interpretacion, created_by) " +
                    "VALUES ('aaaaaaaa-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '2026-03-09 08:00:00', NOW(), 10, 8, 9, 100, -1, -2, 0.8889, 0.8000, 0, 0, 0, 'test', '00000000-0000-0000-0000-000000000001')",
            "INSERT INTO evm_snapshot (id, proyecto_id, fecha_corte, fecha_calculo, pv, ev, ac, bac, cv, sv, cpi, spi, eac, etc, vac, interpretacion, created_by) " +
                    "VALUES ('aaaaaaaa-0000-0000-0000-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '2026-03-10 08:00:00', NOW(), 15, 12, 11, 100, 1, -3, 1.0909, 0.8000, 0, 0, 0, 'test', '00000000-0000-0000-0000-000000000001')"
    })
    void testAC_MIGR_01_backfillInsertsRowForProjectWithSnapshot() {
        executeBackfill();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM evm_time_series WHERE proyecto_id = ?",
                Integer.class,
                PROYECTO_CON_SNAPSHOT);
        assertThat(count).isEqualTo(1);

        Integer periodo = jdbcTemplate.queryForObject(
                "SELECT periodo FROM evm_time_series WHERE proyecto_id = ?",
                Integer.class,
                PROYECTO_CON_SNAPSHOT);
        assertThat(periodo).isEqualTo(1);

        BigDecimal pv = jdbcTemplate.queryForObject(
                "SELECT pv FROM evm_time_series WHERE proyecto_id = ?",
                BigDecimal.class,
                PROYECTO_CON_SNAPSHOT);
        BigDecimal ev = jdbcTemplate.queryForObject(
                "SELECT ev FROM evm_time_series WHERE proyecto_id = ?",
                BigDecimal.class,
                PROYECTO_CON_SNAPSHOT);
        BigDecimal ac = jdbcTemplate.queryForObject(
                "SELECT ac FROM evm_time_series WHERE proyecto_id = ?",
                BigDecimal.class,
                PROYECTO_CON_SNAPSHOT);

        assertThat(pv).isEqualByComparingTo("15");
        assertThat(ev).isEqualByComparingTo("12");
        assertThat(ac).isEqualByComparingTo("11");
    }

    @Test
    @Sql(statements = {
            "DELETE FROM evm_time_series WHERE proyecto_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1'",
            "DELETE FROM evm_snapshot WHERE proyecto_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1'",
            "DELETE FROM proyecto WHERE id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1'",
            "INSERT INTO proyecto (id, nombre, ubicacion, estado, moneda, presupuesto_total, version, created_by) " +
                    "VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'REQ61-MIGR-02', 'Test', 'ACTIVO', 'USD', 1000, 0, '00000000-0000-0000-0000-000000000001')",
            "INSERT INTO evm_snapshot (id, proyecto_id, fecha_corte, fecha_calculo, pv, ev, ac, bac, cv, sv, cpi, spi, eac, etc, vac, interpretacion, created_by) " +
                    "VALUES ('aaaaaaaa-0000-0000-0000-000000000003', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '2026-03-11 08:00:00', NOW(), 20, 19, 18, 120, 1, -1, 1.0556, 0.9500, 0, 0, 0, 'test', '00000000-0000-0000-0000-000000000001')"
    })
    void testAC_MIGR_02_backfillIsIdempotent() {
        executeBackfill();
        executeBackfill();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM evm_time_series WHERE proyecto_id = ?",
                Integer.class,
                PROYECTO_CON_SNAPSHOT);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql(statements = {
            "DELETE FROM evm_time_series WHERE proyecto_id = 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2'",
            "DELETE FROM evm_snapshot WHERE proyecto_id = 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2'",
            "DELETE FROM proyecto WHERE id = 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2'",
            "INSERT INTO proyecto (id, nombre, ubicacion, estado, moneda, presupuesto_total, version, created_by) " +
                    "VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'REQ61-MIGR-03', 'Test', 'ACTIVO', 'USD', 800, 0, '00000000-0000-0000-0000-000000000001')"
    })
    void testAC_MIGR_03_projectWithoutSnapshotGetsNoRow() {
        executeBackfill();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM evm_time_series WHERE proyecto_id = ?",
                Integer.class,
                PROYECTO_SIN_SNAPSHOT);
        assertThat(count).isEqualTo(0);
    }

    private void executeBackfill() {
        jdbcTemplate.execute(BACKFILL_DML);
    }
}
