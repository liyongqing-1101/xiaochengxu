INSERT INTO exam_question (subject_id, type, stem, option_list, answer, explanation, status, created_at, updated_at) VALUES
(1, 1, '高等教育的本质特征是？', '[{"key":"A","value":"社会性"},{"key":"B","value":"阶级性"},{"key":"C","value":"生产性"},{"key":"D","value":"育人性"}]', 'D', '高等教育的本质特征是育人性，这是教育区别于其他社会现象的根本特征。', 1, NOW(), NOW()),
(1, 2, '高等教育的社会功能包括？', '[{"key":"A","value":"政治功能"},{"key":"B","value":"经济功能"},{"key":"C","value":"文化功能"},{"key":"D","value":"生态功能"}]', 'A,B,C', '高等教育的社会功能主要包括政治功能、经济功能、文化功能，不包括生态功能。', 1, NOW(), NOW()),
(1, 3, '高等教育是培养高级专门人才的社会活动。', NULL, 'true', '高等教育是在完成高级中等教育基础上实施的教育，其根本任务是培养高级专门人才。', 1, NOW(), NOW());
SELECT * FROM exam_question;
