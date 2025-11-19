-- 插入测试座位数据
INSERT IGNORE INTO seat (seat_number, area, occupied, occupant, notes) VALUES
('A1', '一楼', FALSE, NULL, '靠窗座位'),
('A2', '一楼', TRUE, '张三', '长期占用'),
('B1', '二楼', FALSE, NULL, '靠过道'),
('B2', '二楼', FALSE, NULL, '投影仪附近'),
('C1', '三楼', TRUE, '李四', '临时占用');