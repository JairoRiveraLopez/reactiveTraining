DROP TABLE IF EXISTS "transactions";

CREATE TABLE "transactions" (
    id          UUID          PRIMARY KEY,
    amount      NUMERIC(14,2),
    currency    VARCHAR(3),
    status      VARCHAR(20),
    created_at  TIMESTAMP
);