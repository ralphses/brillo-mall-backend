ALTER TABLE brillo_booked_business_service
    ADD COLUMN IF NOT EXISTS location TEXT;

ALTER TABLE brillo_business_service_request
    ADD COLUMN IF NOT EXISTS request_status VARCHAR(50) NOT NULL DEFAULT 'NEGOTIATING';

CREATE INDEX IF NOT EXISTS idx_brillo_business_service_request_status
    ON brillo_business_service_request (request_status);

CREATE INDEX IF NOT EXISTS idx_brillo_booked_business_service_status
    ON brillo_booked_business_service (booking_status);

ALTER TABLE brillo_booked_business_service
    ADD CONSTRAINT IF NOT EXISTS uk_brillo_booked_business_service_request UNIQUE (service_request_id);
