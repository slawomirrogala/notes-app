CREATE TABLE revinfo (
    id INTEGER NOT NULL AUTO_INCREMENT,
    revtstmp BIGINT,
    changed_by VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE items_aud (
    id VARCHAR(36) NOT NULL,
    rev INTEGER NOT NULL,
    revtype TINYINT,
    title VARCHAR(255),
    content TEXT,
    version INTEGER,
    owner_id VARCHAR(36) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_items_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (id)
);