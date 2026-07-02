CREATE TABLE IF NOT EXISTS conversation_task_sessions (
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
    current_intent VARCHAR(100),
    current_task_key VARCHAR(100),
    current_state_key VARCHAR(100),
    paused_task_key VARCHAR(100),
    paused_state_key VARCHAR(100),
    task_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    confidence DOUBLE NOT NULL DEFAULT 0,
    slots_json TEXT,
    last_normalized_input TEXT,
    last_ai_reason TEXT,
    last_ai_model VARCHAR(100),
    last_decision_source VARCHAR(30) NOT NULL DEFAULT 'RULE',
    last_reply_type VARCHAR(50),
    last_route_to VARCHAR(100),
    last_turn_at TIMESTAMP NULL,
    transition_count INT NOT NULL DEFAULT 0,
    human_takeover BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_conversation_task_sessions_reference UNIQUE (reference),
    CONSTRAINT uk_conversation_task_sessions_conversation UNIQUE (conversation_id),
    CONSTRAINT fk_conversation_task_sessions_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id),
    CONSTRAINT fk_conversation_task_sessions_business FOREIGN KEY (business_id) REFERENCES brillo_business (reference),
    CONSTRAINT fk_conversation_task_sessions_customer FOREIGN KEY (customer_id) REFERENCES brillo_customer (reference)
);

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'conversation_task_sessions'
              AND index_name = 'idx_conversation_task_sessions_business'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_sessions_business ON conversation_task_sessions (business_id)'
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
              AND table_name = 'conversation_task_sessions'
              AND index_name = 'idx_conversation_task_sessions_customer'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_sessions_customer ON conversation_task_sessions (customer_id)'
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
              AND table_name = 'conversation_task_sessions'
              AND index_name = 'idx_conversation_task_sessions_whatsapp_conversation'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_sessions_whatsapp_conversation ON conversation_task_sessions (whatsapp_conversation_id)'
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
              AND table_name = 'conversation_task_sessions'
              AND index_name = 'idx_conversation_task_sessions_task'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_sessions_task ON conversation_task_sessions (current_task_key)'
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
              AND table_name = 'conversation_task_sessions'
              AND index_name = 'idx_conversation_task_sessions_state'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_sessions_state ON conversation_task_sessions (current_state_key)'
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
              AND table_name = 'conversation_task_sessions'
              AND index_name = 'idx_conversation_task_sessions_status'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_sessions_status ON conversation_task_sessions (task_status)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS conversation_task_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    session_id BIGINT NOT NULL,
    conversation_id VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    whatsapp_conversation_id VARCHAR(100) NOT NULL,
    input_text TEXT,
    normalized_input TEXT,
    intent_key VARCHAR(100),
    route_to_key VARCHAR(100),
    task_key VARCHAR(100),
    state_key VARCHAR(100),
    decision_source VARCHAR(30),
    confidence DOUBLE,
    slot_snapshot TEXT,
    reply_type VARCHAR(50),
    reply_text TEXT,
    ai_model VARCHAR(100),
    ai_reason TEXT,
    event_type VARCHAR(30),
    raw_payload TEXT,
    CONSTRAINT uk_conversation_task_events_reference UNIQUE (reference),
    CONSTRAINT fk_conversation_task_events_session FOREIGN KEY (session_id) REFERENCES conversation_task_sessions (id)
);

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'conversation_task_events'
              AND index_name = 'idx_conversation_task_events_conversation'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_events_conversation ON conversation_task_events (conversation_id)'
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
              AND table_name = 'conversation_task_events'
              AND index_name = 'idx_conversation_task_events_business'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_events_business ON conversation_task_events (business_id)'
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
              AND table_name = 'conversation_task_events'
              AND index_name = 'idx_conversation_task_events_customer'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_events_customer ON conversation_task_events (customer_id)'
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
              AND table_name = 'conversation_task_events'
              AND index_name = 'idx_conversation_task_events_whatsapp_conversation'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_events_whatsapp_conversation ON conversation_task_events (whatsapp_conversation_id)'
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
              AND table_name = 'conversation_task_events'
              AND index_name = 'idx_conversation_task_events_task'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_events_task ON conversation_task_events (task_key)'
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
              AND table_name = 'conversation_task_events'
              AND index_name = 'idx_conversation_task_events_state'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_events_state ON conversation_task_events (state_key)'
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
              AND table_name = 'conversation_task_events'
              AND index_name = 'idx_conversation_task_events_event_type'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversation_task_events_event_type ON conversation_task_events (event_type)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
