package com.example.seatingsystem.model;

import java.util.List;

// 用于表示整个班级的排座网格结果
public class SeatingResult {
    private int rows;
    private int cols;
    private List<SeatingPosition> layout; // 包含所有座位的列表

    // --- Getters and Setters ---
    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }
    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }
    public List<SeatingPosition> getLayout() { return layout; }
    public void setLayout(List<SeatingPosition> layout) { this.layout = layout; }
}