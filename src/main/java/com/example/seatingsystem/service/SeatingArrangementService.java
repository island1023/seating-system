package com.example.seatingsystem.service;

import com.example.seatingsystem.entity.Classroom;
import com.example.seatingsystem.entity.SeatingRecord;
import com.example.seatingsystem.model.SeatingResult;

import java.io.ByteArrayOutputStream; // ❗ 新增导入

import java.util.List;
import java.util.Optional;

public interface SeatingArrangementService {

    /**
     * 初始化或更新班级的座位布局（行/列数）
     */
    Classroom updateLayout(Long classId, int rows, int cols, String rowSpacingConfigJson, String colSpacingConfigJson);
    /**
     * 核心算法：生成随机座位排布
     */
    SeatingResult randomArrange(Long classId);
    /**
     * 根据行数和列数，生成一个空的 SeatingResult 结构
     */
    SeatingResult generateEmptyLayout(Long classId, int rows, int cols);

    /**
     * 保存当前排座结果的快照
     */
    SeatingRecord saveArrangement(Long classId, SeatingResult result, String recordName);

    /**
     * 根据班级ID获取历史排座记录
     */
    List<SeatingRecord> getRecordsByClassId(Long classId);

    /**
     * 获取最新的排座记录（作为当前座位状态）
     */
    Optional<SeatingResult> getLatestArrangement(Long classId);

    /**
     * 新增：导出当前排座结果为 PDF
     * @param classId 班级ID
     * @return 包含 PDF 字节数据的 ByteArrayOutputStream
     */
    ByteArrayOutputStream exportSeatingToPdf(Long classId) throws Exception; // ❗ 新增方法
}