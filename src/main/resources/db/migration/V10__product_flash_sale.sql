SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'brillo_product'
              AND column_name = 'flash_sale'
        ),
        'SELECT 1',
        'ALTER TABLE brillo_product ADD COLUMN flash_sale BOOLEAN NOT NULL DEFAULT FALSE'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
