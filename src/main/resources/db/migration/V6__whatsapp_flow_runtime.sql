CREATE TABLE IF NOT EXISTS conversation_flow_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    conversation_id BIGINT NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    whatsapp_conversation_id VARCHAR(100) NOT NULL,
    flow_id VARCHAR(100),
    flow_name VARCHAR(100),
    flow_token VARCHAR(120),
    flow_cta VARCHAR(100),
    flow_mode VARCHAR(30),
    flow_action VARCHAR(100),
    flow_message_version VARCHAR(30),
    launch_task_key VARCHAR(100),
    launch_state_key VARCHAR(100),
    launch_intent_key VARCHAR(100),
    launch_presentation_type VARCHAR(50),
    correlation_reference VARCHAR(100),
    launch_payload TEXT,
    submission_payload TEXT,
    last_inbound_event_id VARCHAR(100),
    last_outbound_message_id VARCHAR(100),
    last_route_key VARCHAR(100),
    flow_status VARCHAR(30) NOT NULL DEFAULT 'LAUNCHED',
    launched_at TIMESTAMP NULL,
    submitted_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    CONSTRAINT uk_conversation_flow_sessions_reference UNIQUE (reference),
    CONSTRAINT uk_conversation_flow_sessions_conversation UNIQUE (conversation_id),
    CONSTRAINT fk_conversation_flow_sessions_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id),
    CONSTRAINT fk_conversation_flow_sessions_business FOREIGN KEY (business_id) REFERENCES brillo_business (reference),
    CONSTRAINT fk_conversation_flow_sessions_customer FOREIGN KEY (customer_id) REFERENCES brillo_customer (reference)
);

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'conversation_flow_sessions'
              AND index_name = 'idx_conversation_flow_sessions_business'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_flow_sessions_business ON conversation_flow_sessions (business_id)'
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
              AND table_name = 'conversation_flow_sessions'
              AND index_name = 'idx_conversation_flow_sessions_customer'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_flow_sessions_customer ON conversation_flow_sessions (customer_id)'
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
              AND table_name = 'conversation_flow_sessions'
              AND index_name = 'idx_conversation_flow_sessions_flow_status'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_flow_sessions_flow_status ON conversation_flow_sessions (flow_status)'
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
              AND table_name = 'conversation_flow_sessions'
              AND index_name = 'idx_conversation_flow_sessions_task'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_flow_sessions_task ON conversation_flow_sessions (launch_task_key)'
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
              AND table_name = 'conversation_flow_sessions'
              AND index_name = 'idx_conversation_flow_sessions_state'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_flow_sessions_state ON conversation_flow_sessions (launch_state_key)'
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
              AND table_name = 'conversation_flow_sessions'
              AND index_name = 'idx_conversation_flow_sessions_whatsapp_conversation'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_flow_sessions_whatsapp_conversation ON conversation_flow_sessions (whatsapp_conversation_id)'
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
              AND table_name = 'conversation_flow_sessions'
              AND index_name = 'idx_conversation_flow_sessions_flow_token'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_flow_sessions_flow_token ON conversation_flow_sessions (flow_token)'
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
              AND table_name = 'conversation_flow_sessions'
              AND index_name = 'idx_conversation_flow_sessions_correlation_reference'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_flow_sessions_correlation_reference ON conversation_flow_sessions (correlation_reference)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
