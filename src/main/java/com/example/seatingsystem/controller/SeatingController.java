package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.SeatingRecord;
import com.example.seatingsystem.model.SeatingResult;
import com.example.seatingsystem.service.SeatingArrangementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequestMapping("/seating")
public class SeatingController {

    private final SeatingArrangementService seatingArrangementService;

    @Autowired
    public SeatingController(SeatingArrangementService seatingArrangementService) {
        this.seatingArrangementService = seatingArrangementService;
    }

    /**
     * 处理设置布局请求 (POST /seating/layout/update)
     * ❗ 核心修正：返回 JSON 数据，让前端 AJAX 渲染空座位图。
     */
    @PostMapping("/layout/update")
    @ResponseBody // ❗ 关键：返回 JSON 数据
    public SeatingResult updateLayout(@RequestParam Long classId,
                                      @RequestParam int rows,
                                      @RequestParam int cols,
                                      @RequestParam int rowSpacing,
                                      @RequestParam int colSpacing) {

        // 1. 调用 Service 逻辑，保存布局和间距信息 (Service 负责验证座位数是否足够)
        seatingArrangementService.updateLayout(classId, rows, cols, rowSpacing, colSpacing);

        // 2. 返回空的布局结构，让前端 AJAX 接收并渲染空白座位网格
        // 假设 SeatingArrangementService 中已新增 generateEmptyLayout 方法
        return seatingArrangementService.generateEmptyLayout(classId, rows, cols);
    }

    /**
     * 响应前端的随机排座请求，返回 JSON 数据
     */
    @GetMapping("/arrange/{classId}")
    @ResponseBody
    public SeatingResult getRandomArrangement(@PathVariable Long classId) {
        // 实际应用中应检查用户权限
        return seatingArrangementService.randomArrange(classId);
    }

    /**
     * 显示排座历史记录列表
     */
    @GetMapping("/records/{classId}")
    public String showSeatingRecords(@PathVariable Long classId, Model model) {
        List<SeatingRecord> records = seatingArrangementService.getRecordsByClassId(classId);
        model.addAttribute("records", records);
        model.addAttribute("classId", classId);
        return "seating_records"; // 历史记录页面模板
    }

    /**
     * 保存排座结果 (接收前端 JSON)
     */
    @PostMapping("/save/{classId}")
    @ResponseBody // 返回 JSON 确认保存成功
    public String saveArrangement(@PathVariable Long classId,
                                  @RequestParam String recordName,
                                  @RequestBody SeatingResult result) {
        try {
            seatingArrangementService.saveArrangement(classId, result, recordName);
            return "{\"success\": true}";
        } catch (RuntimeException e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }
}