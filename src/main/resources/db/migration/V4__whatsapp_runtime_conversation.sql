SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'conversations'
              AND column_name = 'status'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT ''ACTIVE'''
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
              AND column_name = 'last_intent'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN last_intent VARCHAR(100) NULL'
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
              AND column_name = 'active_task_key'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN active_task_key VARCHAR(100) NULL'
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
              AND column_name = 'human_takeover'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN human_takeover BOOLEAN NOT NULL DEFAULT FALSE'
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
              AND column_name = 'session_expires_at'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN session_expires_at TIMESTAMP NULL'
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
              AND column_name = 'whatsapp_business_number'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN whatsapp_business_number VARCHAR(30) NULL'
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
              AND table_name = 'conversations'
              AND index_name = 'idx_conversations_status'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversations_status ON conversations (status)'
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
              AND table_name = 'conversations'
              AND index_name = 'idx_conversations_session_expires_at'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversations_session_expires_at ON conversations (session_expires_at)'
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
              AND table_name = 'messages'
              AND column_name = 'whatsapp_message_id'
        ),
        'SELECT 1',
        'ALTER TABLE messages ADD COLUMN whatsapp_message_id VARCHAR(100) NULL'
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
              AND table_name = 'messages'
              AND column_name = 'transport_type'
        ),
        'SELECT 1',
        'ALTER TABLE messages ADD COLUMN transport_type VARCHAR(50) NULL'
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
              AND table_name = 'messages'
              AND column_name = 'source_event_id'
        ),
        'SELECT 1',
        'ALTER TABLE messages ADD COLUMN source_event_id VARCHAR(100) NULL'
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
              AND table_name = 'messages'
              AND column_name = 'metadata'
        ),
        'SELECT 1',
        'ALTER TABLE messages ADD COLUMN metadata TEXT NULL'
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
              AND table_name = 'messages'
              AND index_name = 'uk_messages_whatsapp_message_id'
        ),
        'SELECT 1',
        'CREATE UNIQUE INDEX uk_messages_whatsapp_message_id ON messages (whatsapp_message_id)'
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
              AND table_name = 'messages'
              AND index_name = 'idx_messages_source_event_id'
        ),
        'SELECT 1',
        'CREATE INDEX idx_messages_source_event_id ON messages (source_event_id)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
