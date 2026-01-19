-- Migration: Add integrity hash columns to presupuesto table
-- Version: V7
-- Description: Adds cryptographic integrity hash fields for Swiss-Grade budget sealing
-- Dependencies: Task 28 (IntegrityHashService implementation)

-- Add integrity hash columns for Swiss-Grade budget sealing
-- All columns are nullable to support existing budgets that don't have hashes yet
-- Flyway ensures idempotency by tracking executed migrations, but we add IF NOT EXISTS for extra safety
DO $$
BEGIN
    -- Add integrity_hash_approval column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'presupuesto'
          AND column_name = 'integrity_hash_approval'
    ) THEN
        ALTER TABLE presupuesto ADD COLUMN integrity_hash_approval VARCHAR(64);
    END IF;

    -- Add integrity_hash_execution column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'presupuesto'
          AND column_name = 'integrity_hash_execution'
    ) THEN
        ALTER TABLE presupuesto ADD COLUMN integrity_hash_execution VARCHAR(64);
    END IF;

    -- Add integrity_hash_generated_at column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'presupuesto'
          AND column_name = 'integrity_hash_generated_at'
    ) THEN
        ALTER TABLE presupuesto ADD COLUMN integrity_hash_generated_at TIMESTAMP;
    END IF;

    -- Add integrity_hash_generated_by column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'presupuesto'
          AND column_name = 'integrity_hash_generated_by'
    ) THEN
        ALTER TABLE presupuesto ADD COLUMN integrity_hash_generated_by UUID;
    END IF;

    -- Add integrity_hash_algorithm column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'presupuesto'
          AND column_name = 'integrity_hash_algorithm'
    ) THEN
        ALTER TABLE presupuesto ADD COLUMN integrity_hash_algorithm VARCHAR(20);
    END IF;
END $$;

-- Add comments for documentation
COMMENT ON COLUMN presupuesto.integrity_hash_approval IS 
    'Immutable SHA-256 hash (64 hex chars) of budget structure at approval time. Once set, budget structure cannot be modified.';

COMMENT ON COLUMN presupuesto.integrity_hash_execution IS 
    'Dynamic SHA-256 hash (64 hex chars) of financial execution state. Updated after each financial transaction.';

COMMENT ON COLUMN presupuesto.integrity_hash_generated_at IS 
    'Timestamp when the integrity seal was created (when budget was approved).';

COMMENT ON COLUMN presupuesto.integrity_hash_generated_by IS 
    'UUID of the user who approved and sealed the budget.';

COMMENT ON COLUMN presupuesto.integrity_hash_algorithm IS 
    'Hash algorithm version identifier (e.g., SHA-256-v1). Enables future algorithm migration without breaking compatibility.';

-- Create partial index for integrity validation queries
-- Only indexes non-null approval hashes (budgets that have been sealed)
-- This improves query performance for integrity validation operations
CREATE INDEX IF NOT EXISTS idx_presupuesto_integrity 
    ON presupuesto(integrity_hash_approval) 
    WHERE integrity_hash_approval IS NOT NULL;

-- Verify columns added successfully
DO $$
DECLARE
    column_count INTEGER;
BEGIN
    -- Check that all 5 columns exist
    SELECT COUNT(*) INTO column_count
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'presupuesto'
      AND column_name IN (
          'integrity_hash_approval',
          'integrity_hash_execution',
          'integrity_hash_generated_at',
          'integrity_hash_generated_by',
          'integrity_hash_algorithm'
      );
    
    IF column_count < 5 THEN
        RAISE EXCEPTION 'Migration failed: Expected 5 integrity hash columns, found %', column_count;
    END IF;
    
    -- Check that index exists
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE schemaname = 'public'
          AND tablename = 'presupuesto'
          AND indexname = 'idx_presupuesto_integrity'
    ) THEN
        RAISE EXCEPTION 'Migration failed: Index idx_presupuesto_integrity not created';
    END IF;
    
    RAISE NOTICE 'Integrity hash columns and index added successfully to presupuesto table';
END $$;
