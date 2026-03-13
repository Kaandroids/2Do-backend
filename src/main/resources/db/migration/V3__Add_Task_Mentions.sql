ALTER TABLE tasks ADD COLUMN is_private BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE task_mentions (
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, user_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);
