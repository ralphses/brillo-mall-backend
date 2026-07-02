SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'brillo_booked_business_service'
              AND column_name = 'booking_status'
        ),
        'SELECT 1',
        'ALTER TABLE brillo_booked_business_service ADD COLUMN booking_status VARCHAR(50) NOT NULL DEFAULT ''PENDING'' AFTER scheduled_date'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
