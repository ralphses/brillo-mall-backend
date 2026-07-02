SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'brillo_payment_log'
              AND column_name = 'payment_status'
        ),
        'SELECT 1',
        'ALTER TABLE brillo_payment_log ADD COLUMN payment_status VARCHAR(50) NOT NULL DEFAULT ''PENDING'''
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
              AND table_name = 'brillo_payment_log'
              AND column_name = 'authorization_url'
        ),
        'SELECT 1',
        'ALTER TABLE brillo_payment_log ADD COLUMN authorization_url VARCHAR(255) NULL'
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
              AND table_name = 'brillo_payment_log'
              AND column_name = 'access_code'
        ),
        'SELECT 1',
        'ALTER TABLE brillo_payment_log ADD COLUMN access_code VARCHAR(100) NULL'
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
              AND table_name = 'brillo_payment_log'
              AND column_name = 'verified_at'
        ),
        'SELECT 1',
        'ALTER TABLE brillo_payment_log ADD COLUMN verified_at TIMESTAMP NULL'
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
              AND table_name = 'brillo_payment_log'
              AND column_name = 'reconciled_at'
        ),
        'SELECT 1',
        'ALTER TABLE brillo_payment_log ADD COLUMN reconciled_at TIMESTAMP NULL'
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
              AND table_name = 'brillo_payment_log'
              AND column_name = 'gateway_message'
        ),
        'SELECT 1',
        'ALTER TABLE brillo_payment_log ADD COLUMN gateway_message VARCHAR(255) NULL'
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
              AND table_name = 'brillo_payment_log'
              AND index_name = 'idx_brillo_payment_log_payment_status'
        ),
        'SELECT 1',
        'CREATE INDEX idx_brillo_payment_log_payment_status ON brillo_payment_log (payment_status)'
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
              AND table_name = 'brillo_payment_log'
              AND index_name = 'uk_brillo_payment_log_payable'
        ),
        'SELECT 1',
        'CREATE UNIQUE INDEX uk_brillo_payment_log_payable ON brillo_payment_log (payable_type, payable_id)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
