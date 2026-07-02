CREATE TABLE IF NOT EXISTS brillo_notification_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message_medium VARCHAR(50) NOT NULL,
    whatsapp_message_type VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    recipient VARCHAR(150) NOT NULL,
    recipient_type VARCHAR(50),
    source_event_type VARCHAR(100),
    source_event_id VARCHAR(100),
    correlation_id VARCHAR(100) NOT NULL,
    business_id VARCHAR(100),
    customer_id VARCHAR(100),
    order_id VARCHAR(100),
    booking_id VARCHAR(100),
    payment_reference VARCHAR(100),
    service_request_id VARCHAR(100),
    conversation_id VARCHAR(100),
    template_name VARCHAR(100),
    business_initiated BOOLEAN NOT NULL DEFAULT TRUE,
    attempts INT NOT NULL DEFAULT 0,
    sent_at TIMESTAMP NULL,
    last_error TEXT,
    payload_json VARCHAR(4000),
    CONSTRAINT uk_brillo_notification_log_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_notification_log_correlation_recipient UNIQUE (correlation_id, recipient, message_medium)
);

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'brillo_notification_log'
              AND index_name = 'idx_brillo_notification_log_business'
        ),
        'SELECT 1',
        'CREATE INDEX idx_brillo_notification_log_business ON brillo_notification_log (business_id)'
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
              AND table_name = 'brillo_notification_log'
              AND index_name = 'idx_brillo_notification_log_customer'
        ),
        'SELECT 1',
        'CREATE INDEX idx_brillo_notification_log_customer ON brillo_notification_log (customer_id)'
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
              AND table_name = 'brillo_notification_log'
              AND index_name = 'idx_brillo_notification_log_order'
        ),
        'SELECT 1',
        'CREATE INDEX idx_brillo_notification_log_order ON brillo_notification_log (order_id)'
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
              AND table_name = 'brillo_notification_log'
              AND index_name = 'idx_brillo_notification_log_booking'
        ),
        'SELECT 1',
        'CREATE INDEX idx_brillo_notification_log_booking ON brillo_notification_log (booking_id)'
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
              AND table_name = 'brillo_notification_log'
              AND index_name = 'idx_brillo_notification_log_payment'
        ),
        'SELECT 1',
        'CREATE INDEX idx_brillo_notification_log_payment ON brillo_notification_log (payment_reference)'
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
              AND table_name = 'brillo_notification_log'
              AND index_name = 'idx_brillo_notification_log_request'
        ),
        'SELECT 1',
        'CREATE INDEX idx_brillo_notification_log_request ON brillo_notification_log (service_request_id)'
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
              AND table_name = 'brillo_notification_log'
              AND index_name = 'idx_brillo_notification_log_conversation'
        ),
        'SELECT 1',
        'CREATE INDEX idx_brillo_notification_log_conversation ON brillo_notification_log (conversation_id)'
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
              AND table_name = 'brillo_notification_log'
              AND index_name = 'idx_brillo_notification_log_status'
        ),
        'SELECT 1',
        'CREATE INDEX idx_brillo_notification_log_status ON brillo_notification_log (status)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
