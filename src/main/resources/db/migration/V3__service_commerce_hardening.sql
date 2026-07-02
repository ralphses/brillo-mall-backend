SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'brillo_booked_business_service'
              AND column_name = 'location'
        ),
        'SELECT 1',
        'ALTER TABLE brillo_booked_business_service ADD COLUMN location TEXT NULL AFTER service_request_id'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'brillo_business_service_request'
              AND column_name = 'request_status'
        ),
        'SELECT 1',
        'ALTER TABLE brillo_business_service_request ADD COLUMN request_status VARCHAR(50) NOT NULL DEFAULT ''NEGOTIATING'''
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'brillo_business_service_request'
              AND index_name = 'idx_brillo_business_service_request_status'
        ),
        'SELECT 1',
        'CREATE INDEX idx_brillo_business_service_request_status ON brillo_business_service_request (request_status)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'brillo_booked_business_service'
              AND index_name = 'idx_brillo_booked_business_service_status'
        ),
        'SELECT 1',
        'CREATE INDEX idx_brillo_booked_business_service_status ON brillo_booked_business_service (booking_status)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.table_constraints
            WHERE table_schema = DATABASE()
              AND table_name = 'brillo_booked_business_service'
              AND constraint_name = 'uk_brillo_booked_business_service_request'
        ),
        'SELECT 1',
        'ALTER TABLE brillo_booked_business_service ADD CONSTRAINT uk_brillo_booked_business_service_request UNIQUE (service_request_id)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
