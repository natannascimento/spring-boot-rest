CREATE TABLE IF NOT EXISTS job_post (
    post_id INTEGER PRIMARY KEY,
    post_profile VARCHAR(255) NOT NULL,
    post_desc VARCHAR(2000) NOT NULL,
    req_experience INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS job_post_tech_stack (
    post_id INTEGER NOT NULL,
    order_idx INTEGER NOT NULL,
    tech_stack VARCHAR(255) NOT NULL,
    PRIMARY KEY (post_id, order_idx),
    CONSTRAINT fk_job_post_tech_stack_job_post
        FOREIGN KEY (post_id)
        REFERENCES job_post (post_id)
        ON DELETE CASCADE
);
