-- Add moneda column to billetera table
ALTER TABLE billetera 
ADD COLUMN moneda VARCHAR(3) NOT NULL DEFAULT 'PEN';

-- Add moneda column to movimiento_caja table
ALTER TABLE movimiento_caja 
ADD COLUMN moneda VARCHAR(3) NOT NULL DEFAULT 'PEN';

-- Update ensures data integrity even if default behavior varies across DBs (redundant but safe)
UPDATE billetera SET moneda = 'PEN' WHERE moneda IS NULL;
UPDATE movimiento_caja SET moneda = 'PEN' WHERE moneda IS NULL;
