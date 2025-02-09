CREATE TABLE IF NOT EXISTS status_projection(
    pet_id BIGINT NOT NULL,
    status INTEGER NOT NULL,
    PRIMARY KEY(pet_id)
);

CREATE INDEX IF NOT EXISTS status_projection_status_idx ON status_projection(status);

