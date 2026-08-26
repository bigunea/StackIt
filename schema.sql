-- ====================================================================
-- StackIt – MySQL Schema
-- ====================================================================

CREATE DATABASE IF NOT EXISTS stackit
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE stackit;

-- Drop in reverse-FK order so we can re-run this script cleanly
DROP TABLE IF EXISTS budget_items;
DROP TABLE IF EXISTS budgets;
DROP TABLE IF EXISTS users;

-- --------------------------------------------------------------------
-- Table: users
-- --------------------------------------------------------------------
CREATE TABLE users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(120) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- --------------------------------------------------------------------
-- Table: budgets  (FK → users)
-- --------------------------------------------------------------------
CREATE TABLE budgets (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT          NOT NULL,
    name            VARCHAR(100) NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    total_income    DOUBLE       NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_budget_name UNIQUE (user_id, name)
);

-- --------------------------------------------------------------------
-- Table: budget_items  (FK → budgets, FK → users)
-- --------------------------------------------------------------------
CREATE TABLE budget_items (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    budget_id       INT          NOT NULL,
    user_id         INT          NOT NULL,
    category        VARCHAR(30)  NOT NULL,
    amount          DOUBLE       NOT NULL CHECK (amount > 0),
    description     VARCHAR(255),
    item_date       DATE         NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_budget FOREIGN KEY (budget_id)
        REFERENCES budgets(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_user   FOREIGN KEY (user_id)
        REFERENCES users(id)   ON DELETE CASCADE
);

SELECT 'Schema created successfully.' AS status;