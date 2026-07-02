ALTER TABLE IF EXISTS brillo_payment_log ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING';
ALTER TABLE IF EXISTS brillo_payment_log ADD COLUMN IF NOT EXISTS authorization_url VARCHAR(255);
ALTER TABLE IF EXISTS brillo_payment_log ADD COLUMN IF NOT EXISTS access_code VARCHAR(100);
ALTER TABLE IF EXISTS brillo_payment_log ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP NULL;
ALTER TABLE IF EXISTS brillo_payment_log ADD COLUMN IF NOT EXISTS reconciled_at TIMESTAMP NULL;
ALTER TABLE IF EXISTS brillo_payment_log ADD COLUMN IF NOT EXISTS gateway_message VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_brillo_payment_log_payment_status ON brillo_payment_log (payment_status);
CREATE UNIQUE INDEX IF NOT EXISTS uk_brillo_payment_log_payable ON brillo_payment_log (payable_type, payable_id);
