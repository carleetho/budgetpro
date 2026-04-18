-- Migration: Create presupuesto_integrity_audit table
-- Version: V8
-- Description: Creates immutable audit log table for budget integrity events (generation, validation, violations)
-- Dependencies: Task 29 (V7__add_integrity_hash_to_presupuesto.sql)

-- Create audit table for integrity events
-- This table provides complete audit trail for forensic analysis of budget integrity
CREATE TABLE IF NOT EXISTS presupuesto_integrity_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    presupuesto_id UUID NOT NULL REFERENCES presupuesto(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,  -- HASH_GENERATED, HASH_VALIDATED, HASH_VIOLATION
    hash_approval VARCHAR(64),          -- Full 64-character SHA-256 hex string
    hash_execution VARCHAR(64),         -- Full 64-character SHA-256 hex string
    validated_by UUID,                  -- User ID who performed validation
    validated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validation_result VARCHAR(20),      -- SUCCESS, FAILURE
    violation_details TEXT,              -- Detailed description for forensic analysis
    algorithm_version VARCHAR(20),      -- Algorithm version (e.g., "SHA-256-v1")
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add table and column comments for documentation
COMMENT ON TABLE presupuesto_integrity_audit IS 'Immutable audit log for budget integrity events. Records all hash generation, validation, and violation events for forensic analysis.';
COMMENT ON COLUMN presupuesto_integrity_audit.id IS 'Primary key UUID for audit entry';
COMMENT ON COLUMN presupuesto_integrity_audit.presupuesto_id IS 'Foreign key to presupuesto table. References the budget being audited.';
COMMENT ON COLUMN presupuesto_integrity_audit.event_type IS 'Type of integrity event: HASH_GENERATED (hash created on approval), HASH_VALIDATED (hash verified), HASH_VIOLATION (tampering detected)';
COMMENT ON COLUMN presupuesto_integrity_audit.hash_approval IS 'Full 64-character SHA-256 hex string of approval structure hash at time of event';
COMMENT ON COLUMN presupuesto_integrity_audit.hash_execution IS 'Full 64-character SHA-256 hex string of execution state hash at time of event';
COMMENT ON COLUMN presupuesto_integrity_audit.validated_by IS 'UUID of user who performed validation (null for HASH_GENERATED events)';
COMMENT ON COLUMN presupuesto_integrity_audit.validated_at IS 'Timestamp when validation or event occurred';
COMMENT ON COLUMN presupuesto_integrity_audit.validation_result IS 'Result of validation: SUCCESS (hash matches), FAILURE (hash mismatch or violation detected)';
COMMENT ON COLUMN presupuesto_integrity_audit.violation_details IS 'Detailed description of integrity violation for security team forensic analysis. Includes expected vs actual hash, violation type, and context.';
COMMENT ON COLUMN presupuesto_integrity_audit.algorithm_version IS 'Cryptographic algorithm version used (e.g., "SHA-256-v1"). Enables future algorithm migration.';
COMMENT ON COLUMN presupuesto_integrity_audit.created_at IS 'Timestamp when audit entry was created. Immutable record creation time.';

-- Create indexes for efficient querying by security team
-- Index for querying all events for a specific budget
CREATE INDEX IF NOT EXISTS idx_integrity_audit_presupuesto 
    ON presupuesto_integrity_audit(presupuesto_id);

-- Index for querying by event type (e.g., all violations across all budgets)
CREATE INDEX IF NOT EXISTS idx_integrity_audit_event 
    ON presupuesto_integrity_audit(event_type);

-- Index for querying by validation result (e.g., all failures)
CREATE INDEX IF NOT EXISTS idx_integrity_audit_result 
    ON presupuesto_integrity_audit(validation_result);

-- Index for time-based queries (most recent events first)
CREATE INDEX IF NOT EXISTS idx_integrity_audit_timestamp 
    ON presupuesto_integrity_audit(validated_at DESC);

-- Composite index for finding violations efficiently (critical for security alerts)
-- Partial index for HASH_VIOLATION events only
CREATE INDEX IF NOT EXISTS idx_integrity_audit_violations 
    ON presupuesto_integrity_audit(event_type, validation_result) 
    WHERE event_type = 'HASH_VIOLATION';

-- Composite index for querying validation events by result
CREATE INDEX IF NOT EXISTS idx_integrity_audit_validations 
    ON presupuesto_integrity_audit(event_type, validation_result) 
    WHERE event_type = 'HASH_VALIDATED';

-- Verification query to ensure migration completed successfully
DO $$
DECLARE
    table_exists BOOLEAN;
    index_count INTEGER;
    column_count INTEGER;
BEGIN
    -- Check that table exists
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'presupuesto_integrity_audit'
    ) INTO table_exists;

    IF NOT table_exists THEN
        RAISE EXCEPTION 'Migration failed: Table presupuesto_integrity_audit was not created';
    END IF;

    -- Check that all required columns exist
    SELECT COUNT(*) INTO column_count
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'presupuesto_integrity_audit'
      AND column_name IN (
          'id',
          'presupuesto_id',
          'event_type',
          'hash_approval',
          'hash_execution',
          'validated_by',
          'validated_at',
          'validation_result',
          'violation_details',
          'algorithm_version',
          'created_at'
      );

    IF column_count < 11 THEN
        RAISE EXCEPTION 'Migration failed: Expected 11 columns in presupuesto_integrity_audit, found %', column_count;
    END IF;

    -- Check that all required indexes exist
    SELECT COUNT(*) INTO index_count
    FROM pg_indexes
    WHERE schemaname = 'public'
      AND tablename = 'presupuesto_integrity_audit'
      AND indexname IN (
          'idx_integrity_audit_presupuesto',
          'idx_integrity_audit_event',
          'idx_integrity_audit_result',
          'idx_integrity_audit_timestamp',
          'idx_integrity_audit_violations',
          'idx_integrity_audit_validations'
      );

    IF index_count < 6 THEN
        RAISE EXCEPTION 'Migration failed: Expected 6 indexes on presupuesto_integrity_audit, found %', index_count;
    END IF;

    -- Check foreign key constraint
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu 
            ON tc.constraint_name = kcu.constraint_name
        WHERE tc.table_schema = 'public'
          AND tc.table_name = 'presupuesto_integrity_audit'
          AND tc.constraint_type = 'FOREIGN KEY'
          AND kcu.column_name = 'presupuesto_id'
    ) THEN
        RAISE EXCEPTION 'Migration failed: Foreign key constraint on presupuesto_id not found';
    END IF;

    RAISE NOTICE 'Migration V8 completed successfully: presupuesto_integrity_audit table created with % columns and % indexes', column_count, index_count;
END $$;
