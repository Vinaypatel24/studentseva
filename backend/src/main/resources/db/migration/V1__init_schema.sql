-- ==============================================================================
-- StudentSeva - V1 Database Baseline Migration
-- Purpose: Enable PostGIS & pgcrypto, create core relational tables and GiST spatial indexes.
-- ==============================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- 2. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    name          VARCHAR(200) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('STUDENT', 'VENDOR', 'ADMIN')),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 3. OTP REQUESTS TABLE (Email OTP verification)
CREATE TABLE IF NOT EXISTS otp_requests (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) NOT NULL,
    otp_hash    VARCHAR(255) NOT NULL,
    purpose     VARCHAR(30)  NOT NULL CHECK (purpose IN ('REGISTER', 'LOGIN')),
    expires_at  TIMESTAMPTZ  NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_otp_requests_lookup ON otp_requests(email, used, expires_at);

-- 4. BUSINESSES TABLE (Vendor profile + PostGIS Spatial Location)
CREATE TABLE IF NOT EXISTS businesses (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id            UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name                VARCHAR(300) NOT NULL,
    category            VARCHAR(50)  NOT NULL CHECK (category IN (
                            'HOSTEL_PG', 'MESS_TIFFIN', 'PRINTING', 'BOOKS_STATIONERY', 'KIRANA', 'LIBRARY'
                        )),
    description         TEXT,
    address             TEXT NOT NULL,
    location            GEOGRAPHY(Point, 4326) NOT NULL,
    phone               VARCHAR(30),
    whatsapp            VARCHAR(30),
    status              VARCHAR(30) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED', 'TEMPORARILY_CLOSED')),
    verification_status VARCHAR(30) NOT NULL DEFAULT 'VERIFIED' CHECK (verification_status IN ('PENDING', 'VERIFIED', 'SUSPENDED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- CRITICAL: Spatial GiST Index for sub-15ms radius distance queries
CREATE INDEX IF NOT EXISTS idx_businesses_location ON businesses USING GIST(location);
CREATE INDEX IF NOT EXISTS idx_businesses_category ON businesses(category);
CREATE INDEX IF NOT EXISTS idx_businesses_owner    ON businesses(owner_id);

-- 5. BUSINESS PHOTOS TABLE
CREATE TABLE IF NOT EXISTS business_photos (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    storage_key VARCHAR(500) NOT NULL,
    public_url  VARCHAR(1000) NOT NULL,
    caption     VARCHAR(300),
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_business_photos_business ON business_photos(business_id);

-- 6. BUSINESS OPERATING HOURS
CREATE TABLE IF NOT EXISTS business_hours (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6), -- 0=Sunday, 6=Saturday
    opens_at    TIME,
    closes_at   TIME,
    is_closed   BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (business_id, day_of_week)
);

-- 7. OFFERINGS TABLE (Granular entity-level availability: Room, Meal, Print, Library Seat)
CREATE TABLE IF NOT EXISTS offerings (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id       UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    offering_type     VARCHAR(50) NOT NULL CHECK (offering_type IN (
                          'HOSTEL_ROOM', 'MESS_MENU', 'PRINT_SERVICE', 'PRODUCT_STOCK', 'LIBRARY_SEAT'
                      )),
    title             VARCHAR(300) NOT NULL,
    price             NUMERIC(10, 2) NOT NULL DEFAULT 0.00 CHECK (price >= 0),
    pricing_unit      VARCHAR(50), -- e.g., 'PER_MONTH', 'PER_PLATE', 'PER_PAGE'
    status            VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN (
                          'AVAILABLE', 'OCCUPIED', 'SOLD_OUT', 'OUT_OF_STOCK', 'UNAVAILABLE', 'UNCONFIRMED'
                      )),
    last_confirmed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    attributes        JSONB NOT NULL DEFAULT '{}', -- Category-specific flexible fields
    version           INT NOT NULL DEFAULT 0,      -- Optimistic locking to prevent concurrent overwrite
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_offerings_business ON offerings(business_id);
CREATE INDEX IF NOT EXISTS idx_offerings_type_status ON offerings(offering_type, status);
CREATE INDEX IF NOT EXISTS idx_offerings_freshness ON offerings(last_confirmed_at);

-- 8. VENDOR UPDATES FEED (The Follow-and-Updates Engine)
CREATE TABLE IF NOT EXISTS updates (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id   UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    offering_id   UUID REFERENCES offerings(id) ON DELETE SET NULL,
    update_type   VARCHAR(50) NOT NULL CHECK (update_type IN (
                      'MENU_UPDATE', 'HOURS_CHANGE', 'STOCK_ARRIVAL', 'PRICE_CHANGE', 'ANNOUNCEMENT'
                  )),
    title         VARCHAR(300) NOT NULL,
    content       TEXT NOT NULL,
    expires_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_updates_business ON updates(business_id, created_at DESC);

-- 9. FOLLOW RELATIONSHIPS TABLE
CREATE TABLE IF NOT EXISTS follows (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, business_id)
);
CREATE INDEX IF NOT EXISTS idx_follows_user ON follows(user_id);
CREATE INDEX IF NOT EXISTS idx_follows_business ON follows(business_id);

-- 10. LISTING REPORTS TABLE (Abuse & Inaccurate Listing Moderation)
CREATE TABLE IF NOT EXISTS listing_reports (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    reason      VARCHAR(100) NOT NULL,
    description TEXT,
    status      VARCHAR(30) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'RESOLVED', 'DISMISSED')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_reports_status ON listing_reports(status);
