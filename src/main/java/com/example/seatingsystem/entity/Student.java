package com.example.seatingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id", nullable = false)
    private Long classId; // 所属班级ID

    @Column(name = "student_no", nullable = false, length = 20)
    private String studentNo; // 学号

    @Column(nullable = false, length = 50)
    private String name; // 姓名

    @Column(length = 10)
    private String gender; // 性别

    @Column(name = "custom_info", columnDefinition = "TEXT")
    private String customInfo; // 学生自定义信息（JSON 字符串）

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 是否活跃

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    // --- 构造函数, Getters and Setters (省略，结构与 User.java 类似) ---
    // (请自行添加这些方法以确保代码完整性)
}