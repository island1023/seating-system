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

    // --- 构造函数 ---
    public SeatingRecord() {}

    // --- 补充的 Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClassId() { // 解决 SeatingArrangementServiceImpl 中的 getClassId
        return classId;
    }

    public void setClassId(Long classId) { // 解决 SeatingArrangementServiceImpl 中的 setClassId
        this.classId = classId;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) { // 解决 SeatingArrangementServiceImpl 中的 setRecordName
        this.recordName = recordName;
    }

    public String getLayoutSnapshot() {
        return layoutSnapshot;
    }

    public void setLayoutSnapshot(String layoutSnapshot) { // 解决 SeatingArrangementServiceImpl 中的 setLayoutSnapshot
        this.layoutSnapshot = layoutSnapshot;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    // --- JPA 生命周期回调 ---
    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }
}