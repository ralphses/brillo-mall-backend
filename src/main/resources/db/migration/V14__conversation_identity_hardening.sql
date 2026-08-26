SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'conversations'
              AND column_name = 'channel_key'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN channel_key VARCHAR(30) NULL'
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
              AND column_name = 'conversation_mode'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN conversation_mode VARCHAR(30) NULL'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE conversations
SET channel_key = COALESCE(NULLIF(whatsapp_business_number, ''), 'legacy')
WHERE channel_key IS NULL OR channel_key = '';

UPDATE conversations c
SET conversation_mode = CASE
    WHEN c.marketplace_mode = TRUE THEN 'SHARED_MARKETPLACE'
    WHEN EXISTS (
        SELECT 1
        FROM brillo_business b
        WHERE b.whatsapp_type = 'DEDICATED'
          AND b.whatsapp_number IS NOT NULL
          AND REPLACE(REPLACE(REPLACE(REPLACE(b.whatsapp_number, '+', ''), ' ', ''), '-', ''), '(', '') = c.channel_key
    ) THEN 'DEDICATED_BUSINESS'
    ELSE 'SHARED_BUSINESS'
END
WHERE c.conversation_mode IS NULL OR c.conversation_mode = '';

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'conversations'
              AND index_name = 'uk_conversations_whatsapp_conversation_id'
        ),
        'DROP INDEX uk_conversations_whatsapp_conversation_id ON conversations',
        'SELECT 1'
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
              AND column_name = 'channel_key'
              AND is_nullable = 'YES'
        ),
        'ALTER TABLE conversations MODIFY COLUMN channel_key VARCHAR(30) NOT NULL',
        'SELECT 1'
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
              AND column_name = 'conversation_mode'
              AND is_nullable = 'YES'
        ),
        'ALTER TABLE conversations MODIFY COLUMN conversation_mode VARCHAR(30) NOT NULL',
        'SELECT 1'
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
              AND index_name = 'uk_conversations_channel_context'
        ),
        'SELECT 1',
        'CREATE UNIQUE INDEX uk_conversations_channel_context ON conversations (whatsapp_conversation_id, channel_key, conversation_mode)'
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
              AND index_name = 'idx_conversations_channel_mode'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversations_channel_mode ON conversations (channel_key, conversation_mode)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
