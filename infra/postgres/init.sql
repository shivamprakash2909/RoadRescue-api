-- RoadRescue PostgreSQL Schema Initialization Script
-- Executed on container startup to establish isolated schemas per microservice

CREATE SCHEMA IF NOT EXISTS auth_schema;
CREATE SCHEMA IF NOT EXISTS customer_schema;
CREATE SCHEMA IF NOT EXISTS provider_schema;
CREATE SCHEMA IF NOT EXISTS booking_schema;
CREATE SCHEMA IF NOT EXISTS payment_schema;
CREATE SCHEMA IF NOT EXISTS notification_schema;
CREATE SCHEMA IF NOT EXISTS rating_schema;

-- Grant permissions to default application user
GRANT ALL ON SCHEMA auth_schema TO CURRENT_USER;
GRANT ALL ON SCHEMA customer_schema TO CURRENT_USER;
GRANT ALL ON SCHEMA provider_schema TO CURRENT_USER;
GRANT ALL ON SCHEMA booking_schema TO CURRENT_USER;
GRANT ALL ON SCHEMA payment_schema TO CURRENT_USER;
GRANT ALL ON SCHEMA notification_schema TO CURRENT_USER;
GRANT ALL ON SCHEMA rating_schema TO CURRENT_USER;
