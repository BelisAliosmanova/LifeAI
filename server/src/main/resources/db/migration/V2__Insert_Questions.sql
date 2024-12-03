-- V2__insert_questions.sql
INSERT INTO questions (id, text, depends_on, depends_on_answer) VALUES
(1, 'Do you have a diagnosis?', NULL, NULL),
(2, 'Have you undergone surgery?', 1, 'YES'),
(3, 'Are you undergoing therapy after surgery?', 2, 'YES'),
(4, 'Have you completed your therapy?', 3, 'YES'),
(5, 'Would you like further consultation?', 3, 'NO');
