ALTER TABLE brillo_booked_business_service
    ADD COLUMN IF NOT EXISTS booking_status VARCHAR(50) NOT NULL DEFAULT 'PENDING';
