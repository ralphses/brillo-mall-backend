ALTER TABLE conversations ADD COLUMN IF NOT EXISTS channel_key VARCHAR(30);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS conversation_mode VARCHAR(30);

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
          AND b.whatsapp_number = c.channel_key
    ) THEN 'DEDICATED_BUSINESS'
    ELSE 'SHARED_BUSINESS'
END
WHERE c.conversation_mode IS NULL OR c.conversation_mode = '';

ALTER TABLE conversations ALTER COLUMN channel_key SET NOT NULL;
ALTER TABLE conversations ALTER COLUMN conversation_mode SET NOT NULL;

ALTER TABLE conversations DROP CONSTRAINT IF EXISTS uk_conversations_whatsapp_conversation_id;

CREATE UNIQUE INDEX IF NOT EXISTS uk_conversations_channel_context
    ON conversations (whatsapp_conversation_id, channel_key, conversation_mode);
CREATE INDEX IF NOT EXISTS idx_conversations_channel_mode
    ON conversations (channel_key, conversation_mode);
