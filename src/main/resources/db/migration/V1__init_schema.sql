CREATE TABLE users (
    id VARCHAR(36) NOT NULL,
    login VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (login)
) ENGINE=InnoDB;

CREATE TABLE items (
    id VARCHAR(36) NOT NULL,
    owner_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    version INTEGER NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_items_owner FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE item_permissions (
    id VARCHAR(36) NOT NULL,
    item_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role ENUM('VIEWER', 'EDITOR') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_perms_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_perms_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;