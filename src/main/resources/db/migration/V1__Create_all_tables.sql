-- Migration to create all tables based on your entities

-- Create users table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_token VARCHAR(255),
    verification_token_expires TIMESTAMP NULL,
    password_reset_token VARCHAR(255),
    password_reset_token_expires TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Create persistent_logins table for Spring Security Remember Me
CREATE TABLE persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create global_field table
CREATE TABLE global_field (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    field_key VARCHAR(255),
    label VARCHAR(255),
    sort_order INT,
    deleted_at TIMESTAMP NULL
);

-- Create more_info table
CREATE TABLE more_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_order TEXT
);

-- Create text_section table
CREATE TABLE text_section (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    content TEXT,
    section_identifier VARCHAR(255),
    more_info_id BIGINT,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (more_info_id) REFERENCES more_info(id)
);

-- Create table_section table
CREATE TABLE table_section (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    section_identifier VARCHAR(255),
    more_info_id BIGINT,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (more_info_id) REFERENCES more_info(id)
);

-- Create mini_table_row table
CREATE TABLE mini_table_row (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    label VARCHAR(255),
    description TEXT,
    table_section_id BIGINT,
    FOREIGN KEY (table_section_id) REFERENCES table_section(id)
);

-- Create interest_rate table
CREATE TABLE interest_rate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    web_link VARCHAR(255),
    more_info_id BIGINT,
    FOREIGN KEY (more_info_id) REFERENCES more_info(id)
);

-- Create interest_rate_field_value table (this is the correct table name)
CREATE TABLE interest_rate_field_value (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    value VARCHAR(255),
    interest_rate_id BIGINT,
    global_field_id BIGINT,
    FOREIGN KEY (interest_rate_id) REFERENCES interest_rate(id),
    FOREIGN KEY (global_field_id) REFERENCES global_field(id)
);