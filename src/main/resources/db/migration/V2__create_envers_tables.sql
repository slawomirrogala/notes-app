CREATE TABLE revinfo (
    rev INTEGER NOT NULL AUTO_INCREMENT,
    revtstmp BIGINT,
    PRIMARY KEY (rev)
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
    created_by VARCHAR(64) NOT NULL,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_items_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);