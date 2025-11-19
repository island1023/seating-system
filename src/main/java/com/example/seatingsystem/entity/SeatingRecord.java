package com.example.seatingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seating_record")
public class SeatingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id", nullable = false)
    private Long classId; // 关联的班级ID

    @Column(name = "record_name", nullable = false, length = 100)
    private String recordName; // 排座记录名称

    @Column(name = "layout_snapshot", columnDefinition = "LONGTEXT", nullable = false)
    private String layoutSnapshot; // 座位布局快照（JSON 字符串）

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    // --- 构造函数, Getters and Setters (省略) ---
}