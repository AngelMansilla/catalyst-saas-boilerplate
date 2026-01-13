-- Catalyst User Service - Flyway Migration V1
-- Extends the existing auth schema with additional columns and tables
-- Note: The base auth.users and auth.sessions tables were created in init script

-- Add password_hash column for local authentication
ALTER TABLE auth.users 
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- Add role column with default USER
ALTER TABLE auth.users 
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER' NOT NULL;

-- Create constraint for valid roles
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_users_role'
    ) THEN
        ALTER TABLE auth.users 
            ADD CONSTRAINT chk_users_role 
            CHECK (role IN ('USER', 'ADMIN'));
    END IF;
END $$;

-- Create index on role column for efficient queries
CREATE INDEX IF NOT EXISTS idx_users_role ON auth.users(role);

-- Create password reset tokens table
CREATE TABLE IF NOT EXISTS auth.password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token VARCHAR(128) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN DEFAULT FALSE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ip_address INET
);

-- Create indexes for password reset tokens
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id 
    ON auth.password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token 
    ON auth.password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires_at 
    ON auth.password_reset_tokens(expires_at);

-- Create login history table for security audit
CREATE TABLE IF NOT EXISTS auth.login_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    login_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ip_address INET,
    user_agent TEXT,
    provider VARCHAR(50),
    success BOOLEAN DEFAULT TRUE NOT NULL,
    failure_reason VARCHAR(255)
);

-- Create indexes for login history
CREATE INDEX IF NOT EXISTS idx_login_history_user_id 
    ON auth.login_history(user_id);
CREATE INDEX IF NOT EXISTS idx_login_history_login_at 
    ON auth.login_history(login_at DESC);

-- Create function to clean expired password reset tokens
CREATE OR REPLACE FUNCTION auth.cleanup_expired_tokens()
RETURNS void AS $$
BEGIN
    DELETE FROM auth.password_reset_tokens 
    WHERE expires_at < CURRENT_TIMESTAMP 
       OR (used = TRUE AND used_at < CURRENT_TIMESTAMP - INTERVAL '7 days');
END;
$$ LANGUAGE plpgsql;

-- Add comment for documentation
COMMENT ON TABLE auth.password_reset_tokens IS 'Stores password reset tokens with expiration';
COMMENT ON TABLE auth.login_history IS 'Audit log of all login attempts';
COMMENT ON COLUMN auth.users.password_hash IS 'BCrypt hashed password for local authentication';
COMMENT ON COLUMN auth.users.role IS 'User role: USER or ADMIN';

-- Log migration
INSERT INTO audit.event_log (event_type, entity_type, entity_id, event_data)
VALUES (
    'MIGRATION_APPLIED', 
    'FLYWAY', 
    'V1__extend_auth_schema', 
    jsonb_build_object(
        'timestamp', CURRENT_TIMESTAMP,
        'service', 'user-service',
        'changes', ARRAY['password_hash column', 'role column', 'password_reset_tokens table', 'login_history table']
    )
);

