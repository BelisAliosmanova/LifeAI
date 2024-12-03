-- V1__create_questions_table.sql
CREATE TABLE questions (
    id BIGINT PRIMARY KEY,
    text VARCHAR(500) NOT NULL,
    depends_on BIGINT, -- ID of the triggering question
    depends_on_answer VARCHAR(10) -- Answer ("YES" or "NO") that triggers this question
);
