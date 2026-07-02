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
    CONSTRAINT uk_conversation_task_sessions_conversation UNIQUE (conversation_id)
);

CREATE INDEX IF NOT EXISTS idx_conversation_task_sessions_business
    ON conversation_task_sessions (business_id);

CREATE INDEX IF NOT EXISTS idx_conversation_task_sessions_customer
    ON conversation_task_sessions (customer_id);

CREATE INDEX IF NOT EXISTS idx_conversation_task_sessions_whatsapp_conversation
    ON conversation_task_sessions (whatsapp_conversation_id);

CREATE INDEX IF NOT EXISTS idx_conversation_task_sessions_task
    ON conversation_task_sessions (current_task_key);

CREATE INDEX IF NOT EXISTS idx_conversation_task_sessions_state
    ON conversation_task_sessions (current_state_key);

CREATE INDEX IF NOT EXISTS idx_conversation_task_sessions_status
    ON conversation_task_sessions (task_status);

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

CREATE INDEX IF NOT EXISTS idx_conversation_task_events_conversation
    ON conversation_task_events (conversation_id);

CREATE INDEX IF NOT EXISTS idx_conversation_task_events_business
    ON conversation_task_events (business_id);

CREATE INDEX IF NOT EXISTS idx_conversation_task_events_customer
    ON conversation_task_events (customer_id);

CREATE INDEX IF NOT EXISTS idx_conversation_task_events_whatsapp_conversation
    ON conversation_task_events (whatsapp_conversation_id);

CREATE INDEX IF NOT EXISTS idx_conversation_task_events_task
    ON conversation_task_events (task_key);

CREATE INDEX IF NOT EXISTS idx_conversation_task_events_state
    ON conversation_task_events (state_key);

CREATE INDEX IF NOT EXISTS idx_conversation_task_events_event_type
    ON conversation_task_events (event_type);
