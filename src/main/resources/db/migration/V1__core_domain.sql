CREATE TABLE BRILLO_USER (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    email VARCHAR(150),
    phone_number VARCHAR(20),
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    invited_by VARCHAR(100),
    referred_by VARCHAR(100),
    oauth2_user BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT uk_brillo_user_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_user_email UNIQUE (email),
    CONSTRAINT uk_brillo_user_phone UNIQUE (phone_number),
    CONSTRAINT uk_brillo_user_username UNIQUE (username)
);

CREATE TABLE BRILLO_USER_ROLES (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_brillo_user_roles_user FOREIGN KEY (user_id) REFERENCES BRILLO_USER (id)
);

CREATE INDEX idx_user_roles_user_id ON BRILLO_USER_ROLES (user_id);
CREATE INDEX idx_user_roles_role ON BRILLO_USER_ROLES (role);

CREATE TABLE BRILLO_BUSINESS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    owner_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    customers CLOB,
    slug VARCHAR(120) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(100) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    email VARCHAR(150),
    phone_number VARCHAR(20),
    logo_url VARCHAR(255),
    state VARCHAR(100),
    city VARCHAR(100),
    address TEXT,
    whatsapp_type VARCHAR(50) NOT NULL DEFAULT 'SHARED',
    whatsapp_number VARCHAR(20),
    storefront_name VARCHAR(100) NOT NULL DEFAULT 'Default',
    storefront_active BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    setup_completed BOOLEAN DEFAULT FALSE,
    CONSTRAINT uk_brillo_business_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_business_slug UNIQUE (slug),
    CONSTRAINT uk_brillo_business_email UNIQUE (email),
    CONSTRAINT uk_brillo_business_phone UNIQUE (phone_number),
    CONSTRAINT fk_brillo_business_owner FOREIGN KEY (owner_id) REFERENCES BRILLO_USER (reference)
);

CREATE INDEX idx_brillo_business_owner ON BRILLO_BUSINESS (owner_id);
CREATE INDEX idx_brillo_business_slug ON BRILLO_BUSINESS (slug);
CREATE INDEX idx_brillo_business_status ON BRILLO_BUSINESS (status);

CREATE TABLE BRILLO_CUSTOMER (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    user_id VARCHAR(36),
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone_number VARCHAR(20),
    address TEXT,
    status VARCHAR(100) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_brillo_customer_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_customer_email UNIQUE (email),
    CONSTRAINT uk_brillo_customer_phone UNIQUE (phone_number)
);

CREATE INDEX idx_brillo_customer_user ON BRILLO_CUSTOMER (user_id);

CREATE TABLE customer_related_business_ids (
    customer_id BIGINT NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    CONSTRAINT pk_customer_related_business_ids PRIMARY KEY (customer_id, business_id),
    CONSTRAINT fk_customer_related_business_ids_customer FOREIGN KEY (customer_id) REFERENCES BRILLO_CUSTOMER (id),
    CONSTRAINT fk_customer_related_business_ids_business FOREIGN KEY (business_id) REFERENCES BRILLO_BUSINESS (reference)
);

CREATE INDEX idx_customer_related_business_ids_business ON customer_related_business_ids (business_id);

CREATE TABLE BRILLO_PRODUCT (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    discounted_price DECIMAL(10,2),
    sku VARCHAR(50),
    main_image_url VARCHAR(200),
    quantity INT,
    status VARCHAR(100) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_brillo_product_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_product_business_sku UNIQUE (business_id, sku),
    CONSTRAINT uk_brillo_product_business_name UNIQUE (business_id, name),
    CONSTRAINT fk_brillo_product_business FOREIGN KEY (business_id) REFERENCES BRILLO_BUSINESS (reference)
);

CREATE INDEX idx_brillo_product_business ON BRILLO_PRODUCT (business_id);
CREATE INDEX idx_brillo_product_status ON BRILLO_PRODUCT (status);

CREATE TABLE BRILLO_BUSINESS_SERVICE (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    pricing_type VARCHAR(50),
    base_price DECIMAL(19,2),
    duration_minutes INT,
    negotiable BOOLEAN NOT NULL DEFAULT FALSE,
    requires_schedule BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_brillo_business_service_reference UNIQUE (reference),
    CONSTRAINT uk_business_service_slug UNIQUE (business_id, slug),
    CONSTRAINT fk_brillo_business_service_business FOREIGN KEY (business_id) REFERENCES BRILLO_BUSINESS (reference)
);

CREATE INDEX idx_service_business_id ON BRILLO_BUSINESS_SERVICE (business_id);
CREATE INDEX idx_service_slug ON BRILLO_BUSINESS_SERVICE (slug);
CREATE INDEX idx_service_status ON BRILLO_BUSINESS_SERVICE (status);

CREATE TABLE BRILLO_BUSINESS_SERVICE_REQUEST (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    whatsapp_conversation_id VARCHAR(100),
    business_service_id VARCHAR(36) NOT NULL,
    initial_price DECIMAL(19,2),
    last_offered_price DECIMAL(19,2),
    agreed_price DECIMAL(19,2),
    negotiation_attempts INT DEFAULT 0,
    human_takeover BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    request_status VARCHAR(50) NOT NULL DEFAULT 'NEGOTIATING',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_brillo_business_service_request_reference UNIQUE (reference),
    CONSTRAINT fk_brillo_business_service_request_customer FOREIGN KEY (customer_id) REFERENCES BRILLO_CUSTOMER (reference),
    CONSTRAINT fk_brillo_business_service_request_business FOREIGN KEY (business_id) REFERENCES BRILLO_BUSINESS (reference),
    CONSTRAINT fk_brillo_business_service_request_service FOREIGN KEY (business_service_id) REFERENCES BRILLO_BUSINESS_SERVICE (reference)
);

CREATE INDEX idx_brillo_business_service_request_business ON BRILLO_BUSINESS_SERVICE_REQUEST (business_id);
CREATE INDEX idx_brillo_business_service_request_service ON BRILLO_BUSINESS_SERVICE_REQUEST (business_service_id);
CREATE INDEX idx_brillo_business_service_request_user ON BRILLO_BUSINESS_SERVICE_REQUEST (user_id);
CREATE INDEX idx_brillo_business_service_request_conversation ON BRILLO_BUSINESS_SERVICE_REQUEST (whatsapp_conversation_id);

CREATE TABLE BRILLO_BOOKED_BUSINESS_SERVICE (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    business_service_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    service_request_id VARCHAR(36),
    agreed_price DECIMAL(19,2) NOT NULL,
    scheduled_date TIMESTAMP,
    booking_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_brillo_booked_business_service_reference UNIQUE (reference),
    CONSTRAINT fk_brillo_booked_business_service_business FOREIGN KEY (business_id) REFERENCES BRILLO_BUSINESS (reference),
    CONSTRAINT fk_brillo_booked_business_service_service FOREIGN KEY (business_service_id) REFERENCES BRILLO_BUSINESS_SERVICE (reference),
    CONSTRAINT fk_brillo_booked_business_service_customer FOREIGN KEY (customer_id) REFERENCES BRILLO_CUSTOMER (reference),
    CONSTRAINT fk_brillo_booked_business_service_request FOREIGN KEY (service_request_id) REFERENCES BRILLO_BUSINESS_SERVICE_REQUEST (reference)
);

CREATE INDEX idx_brillo_booked_business_service_business ON BRILLO_BOOKED_BUSINESS_SERVICE (business_id);
CREATE INDEX idx_brillo_booked_business_service_service ON BRILLO_BOOKED_BUSINESS_SERVICE (business_service_id);
CREATE INDEX idx_brillo_booked_business_service_user ON BRILLO_BOOKED_BUSINESS_SERVICE (user_id);
CREATE INDEX idx_brillo_booked_business_service_customer ON BRILLO_BOOKED_BUSINESS_SERVICE (customer_id);

CREATE TABLE BRILLO_ORDER (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    order_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(36),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    shipping_address TEXT,
    CONSTRAINT uk_brillo_order_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_order_order_id UNIQUE (order_id),
    CONSTRAINT fk_brillo_order_business FOREIGN KEY (business_id) REFERENCES BRILLO_BUSINESS (reference),
    CONSTRAINT fk_brillo_order_customer FOREIGN KEY (customer_id) REFERENCES BRILLO_CUSTOMER (reference)
);

CREATE INDEX idx_brillo_order_business ON BRILLO_ORDER (business_id);
CREATE INDEX idx_brillo_order_customer ON BRILLO_ORDER (customer_id);
CREATE INDEX idx_brillo_order_user ON BRILLO_ORDER (user_id);
CREATE INDEX idx_brillo_order_status ON BRILLO_ORDER (status);

CREATE TABLE BRILLO_ORDER_ITEM (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    order_id BIGINT NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(10,2) NOT NULL,
    CONSTRAINT uk_brillo_order_item_reference UNIQUE (reference),
    CONSTRAINT fk_brillo_order_item_order FOREIGN KEY (order_id) REFERENCES BRILLO_ORDER (id),
    CONSTRAINT fk_brillo_order_item_product FOREIGN KEY (product_id) REFERENCES BRILLO_PRODUCT (reference)
);

CREATE INDEX idx_brillo_order_item_order ON BRILLO_ORDER_ITEM (order_id);
CREATE INDEX idx_brillo_order_item_product ON BRILLO_ORDER_ITEM (product_id);

CREATE TABLE CONVERSATIONS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    whatsapp_conversation_id VARCHAR(100),
    last_interaction_at TIMESTAMP,
    CONSTRAINT uk_conversations_reference UNIQUE (reference),
    CONSTRAINT uk_conversations_whatsapp_conversation_id UNIQUE (whatsapp_conversation_id),
    CONSTRAINT fk_conversations_business FOREIGN KEY (business_id) REFERENCES BRILLO_BUSINESS (reference),
    CONSTRAINT fk_conversations_customer FOREIGN KEY (customer_id) REFERENCES BRILLO_CUSTOMER (reference)
);

CREATE INDEX idx_conversations_business_id ON CONVERSATIONS (business_id);
CREATE INDEX idx_conversations_customer_id ON CONVERSATIONS (customer_id);
CREATE INDEX idx_conversations_whatsapp_conversation_id ON CONVERSATIONS (whatsapp_conversation_id);

CREATE TABLE MESSAGES (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    conversation_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    intent VARCHAR(100),
    CONSTRAINT uk_messages_reference UNIQUE (reference),
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES CONVERSATIONS (id)
);

CREATE INDEX idx_messages_conversation_id ON MESSAGES (conversation_id);

CREATE TABLE BRILLO_PAYMENT_LOG (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    payment_reference VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payable_type VARCHAR(50) NOT NULL,
    payable_id VARCHAR(36) NOT NULL,
    CONSTRAINT uk_brillo_payment_log_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_payment_log_payment_reference UNIQUE (payment_reference),
    CONSTRAINT fk_brillo_payment_log_business FOREIGN KEY (business_id) REFERENCES BRILLO_BUSINESS (reference)
);

CREATE INDEX idx_brillo_payment_log_reference ON BRILLO_PAYMENT_LOG (reference);
CREATE INDEX idx_brillo_payment_log_business ON BRILLO_PAYMENT_LOG (business_id);
CREATE INDEX idx_brillo_payment_log_payable ON BRILLO_PAYMENT_LOG (payable_id);

CREATE TABLE BRILLO_ALL_REQUEST (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    request_body CLOB NOT NULL,
    status VARCHAR(30) NOT NULL,
    response_body CLOB,
    origin VARCHAR(100),
    responded_at TIMESTAMP,
    CONSTRAINT uk_request_reference UNIQUE (reference)
);

CREATE INDEX idx_request_reference ON BRILLO_ALL_REQUEST (reference);
CREATE INDEX idx_request_status ON BRILLO_ALL_REQUEST (status);

CREATE TABLE media_asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    url VARCHAR(255),
    asset_id VARCHAR(255),
    name VARCHAR(255),
    CONSTRAINT uk_media_asset_reference UNIQUE (reference)
);

CREATE TABLE EVENT_PUBLICATION (
    id UUID PRIMARY KEY,
    publication_date TIMESTAMP NOT NULL,
    listener_id VARCHAR(255) NOT NULL,
    serialized_event VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    completion_date TIMESTAMP
);

CREATE TABLE EVENT_PUBLICATION_ARCHIVE (
    id UUID PRIMARY KEY,
    publication_date TIMESTAMP NOT NULL,
    listener_id VARCHAR(255) NOT NULL,
    serialized_event VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    completion_date TIMESTAMP
);
