package com.example.seatingsystem.model;

import com.example.seatingsystem.entity.Student;

// 用于表示网格中的一个座位及其占据者
public class SeatingPosition {
    private int row;
    private int col;
    private Long studentId; // 使用 ID 简化 JSON 结构
    private String studentName; // 方便前端显示
    private String gender; // 性别信息

    // 1. 默认构造函数
    public SeatingPosition() {}

    // 2. 唯一的、修正后的构造函数 (包含性别提取)
    public SeatingPosition(int row, int col, Student student) {
        this.row = row;
        this.col = col;
        if (student != null) {
            this.studentId = student.getId();
            this.studentName = student.getName();
            // 确保 Student 实体中 getGender() 存在
            this.gender = student.getGender();
        }
    }

    // --- Getters and Setters ---
    public int getRow() {
        return row;
    }
    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }
    public void setCol(int col) {
        this.col = col;
    }

    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    // 修复后的 Gender Getter/Setter
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
}