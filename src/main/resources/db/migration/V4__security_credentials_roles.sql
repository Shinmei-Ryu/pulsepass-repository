CREATE TABLE user_credentials (
                                  user_id             BIGINT       NOT NULL,
                                  password_hash       VARCHAR(100) NOT NULL,
                                  password_changed_at TIMESTAMP,

                                  CONSTRAINT PK_user_credentials_user_id PRIMARY KEY (user_id),
                                  CONSTRAINT FK_user_credentials_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_roles (
                            user_id BIGINT      NOT NULL,
                            role    VARCHAR(30) NOT NULL,

                            CONSTRAINT PK_user_roles PRIMARY KEY (user_id, role),
                            CONSTRAINT FK_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users(id),
                            CONSTRAINT CHK_user_roles_role CHECK (role IN ('USER', 'STAFF', 'ADMIN'))
);