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

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // --- 构造函数 ---
    public Student() {}

    // ---------------------------------------------
    // --- 完整的 Getters and Setters (修复缺失部分) ---
    // ---------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // 🎯 补充：Gender 的 Getter/Setter
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    // 🎯 补充：CustomInfo 的 Getter/Setter
    public String getCustomInfo() {
        return customInfo;
    }

    public void setCustomInfo(String customInfo) {
        this.customInfo = customInfo;
    }

    // 🎯 补充：IsActive 的 Getter/Setter (注意布尔类型的 Getter 通常是 getXxx 或 isXxx)
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // --- 时间戳 Getters/Setters ---

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    // --- JPA 生命周期回调 ---
    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}