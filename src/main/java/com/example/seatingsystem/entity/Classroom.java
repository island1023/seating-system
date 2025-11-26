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

    // ❗ 修改：移除旧的 Integer 间距字段，改为存储 JSON 配置
    @Column(name = "row_spacing_config", columnDefinition = "TEXT")
    private String rowSpacingConfig;
    @Column(name = "col_spacing_config", columnDefinition = "TEXT")
    private String colSpacingConfig;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // --- 构造函数 ---
    public Classroom() {}

    // ---------------------------------------------
    // --- 完整的 Getters and Setters (包含新增字段) ---
    // ---------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeatLayout() { return seatLayout; }
    public void setSeatLayout(String seatLayout) { this.seatLayout = seatLayout; }

    // ❗ 新增：间距配置 Getter/Setter
    public String getRowSpacingConfig() { return rowSpacingConfig; }
    public void setRowSpacingConfig(String rowSpacingConfig) { this.rowSpacingConfig = rowSpacingConfig; }
    public String getColSpacingConfig() { return colSpacingConfig; }
    public void setColSpacingConfig(String colSpacingConfig) { this.colSpacingConfig = colSpacingConfig; }

    // ❗ 兼容性方法 (返回默认值 15，以便在前端修改前不报错)
    public Integer getRowSpacing() { return 15; }
    public void setRowSpacing(Integer rowSpacing) { /* 忽略设置 */ }
    public Integer getColSpacing() { return 15; }
    public void setColSpacing(Integer colSpacing) { /* 忽略设置 */ }


    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

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