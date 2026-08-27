SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'conversations'
              AND column_name = 'assigned_support_user_id'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN assigned_support_user_id VARCHAR(36) NULL'
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
              AND table_name = 'conversations'
              AND column_name = 'assigned_support_at'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN assigned_support_at TIMESTAMP NULL'
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
              AND table_name = 'conversations'
              AND column_name = 'assignment_status'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN assignment_status VARCHAR(30) NULL'
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
              AND table_name = 'conversations'
              AND column_name = 'last_support_action_at'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN last_support_action_at TIMESTAMP NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS support_conversation_notes (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NULL,
    updated_by VARCHAR(255) NULL,
    reference VARCHAR(255) NULL,
    conversation_id BIGINT NOT NULL,
    actor_user_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    CONSTRAINT fk_support_notes_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id)
);

CREATE TABLE IF NOT EXISTS support_conversation_events (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NULL,
    updated_by VARCHAR(255) NULL,
    reference VARCHAR(255) NULL,
    conversation_id BIGINT NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    actor_user_id VARCHAR(36) NOT NULL,
    reason TEXT NULL,
    from_assigned_support_user_id VARCHAR(36) NULL,
    to_assigned_support_user_id VARCHAR(36) NULL,
    from_active_business_id VARCHAR(36) NULL,
    to_active_business_id VARCHAR(36) NULL,
    metadata TEXT NULL,
    CONSTRAINT fk_support_events_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id)
);

CREATE INDEX idx_conversations_assigned_support_user_id ON conversations (assigned_support_user_id);
CREATE INDEX idx_support_conversation_notes_reference ON support_conversation_notes (conversation_id, created_at);
CREATE INDEX idx_support_conversation_events_reference ON support_conversation_events (conversation_id, created_at);
