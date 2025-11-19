-- 座位表
CREATE TABLE IF NOT EXISTS seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '座位ID',
    seat_number VARCHAR(20) NOT NULL COMMENT '座位编号（如A1、B2）',
    area VARCHAR(50) NOT NULL COMMENT '区域（如一楼、二楼）',
    occupied BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否占用',
    occupant VARCHAR(100) COMMENT '占用者姓名',
    notes VARCHAR(255) COMMENT '备注信息',
    UNIQUE KEY uk_seat_number (seat_number) COMMENT '座位编号唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位信息表';