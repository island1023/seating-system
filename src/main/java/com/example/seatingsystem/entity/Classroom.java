package com.example.seatingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "classroom")
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId; // 关联的教师ID

    @Column(nullable = false, length = 100)
    private String name; // 班级名称

    private String description; // 班级描述

    @Column(name = "seat_layout", columnDefinition = "TEXT")
    private String seatLayout; // 座位布局配置（JSON 字符串）

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    // --- 构造函数, Getters and Setters (省略，结构与 User.java 类似) ---
    // (请自行添加这些方法以确保代码完整性)
}