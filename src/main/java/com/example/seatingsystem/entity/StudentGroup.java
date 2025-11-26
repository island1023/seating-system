package com.example.seatingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_group")
public class StudentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id", nullable = false)
    private Long classId; // 关联的班级ID

    @Column(nullable = false, length = 100)
    private String name; // 分组名称

    @Column(name = "student_ids", columnDefinition = "TEXT")
    private String studentIds; // 该组包含的学生ID列表（JSON或逗号分隔）

    private String description; // 分组描述

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    // --- 构造函数 ---
    public StudentGroup() {}

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStudentIds() { return studentIds; }
    public void setStudentIds(String studentIds) { this.studentIds = studentIds; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    // --- JPA 生命周期回调 ---
    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }
}