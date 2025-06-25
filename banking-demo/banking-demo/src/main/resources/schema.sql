CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS transactions;

CREATE TABLE transactions (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    amount      NUMERIC(14,2) NOT NULL,
    currency    CHAR(3)       NOT NULL,
    type        VARCHAR(20)   NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);