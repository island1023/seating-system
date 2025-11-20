package com.example.seatingsystem.service;

import com.example.seatingsystem.entity.Classroom;
import com.example.seatingsystem.entity.SeatingRecord;
import com.example.seatingsystem.model.SeatingResult;

import java.util.List;

public interface SeatingArrangementService {

    /**
     * 初始化或更新班级的座位布局（行/列数）
     */
    Classroom updateLayout(Long classId, int rows, int cols, int rowSpacing, int colSpacing);
    /**
     * 核心算法：生成随机座位排布
     */
    SeatingResult randomArrange(Long classId);

    /**
     * 保存当前排座结果的快照
     */
    SeatingRecord saveArrangement(Long classId, SeatingResult result, String recordName);

    /**
     * 根据班级ID获取历史排座记录
     */
    List<SeatingRecord> getRecordsByClassId(Long classId);

    // TODO: 导出 PDF 功能留待最后
}