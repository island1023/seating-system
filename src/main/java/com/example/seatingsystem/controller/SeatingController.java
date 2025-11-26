package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.SeatingRecord;
import com.example.seatingsystem.model.SeatingResult;
import com.example.seatingsystem.service.SeatingArrangementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse; // ❗ 修复：新增导入
import java.io.ByteArrayOutputStream; // ❗ 修复：新增导入
import java.io.OutputStream; // ❗ 修复：新增导入

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
     * ❗ 核心修正：使用 JSON 配置参数。
     */
    @PostMapping("/layout/update")
    @ResponseBody // ❗ 关键：返回 JSON 数据
    public SeatingResult updateLayout(@RequestParam Long classId,
                                      @RequestParam int rows,
                                      @RequestParam int cols,
                                      @RequestParam String rowSpacingConfigJson, // 修正为 JSON 字符串
                                      @RequestParam String colSpacingConfigJson) { // 修正为 JSON 字符串

        // 1. 调用 Service 逻辑，保存布局和间距信息
        seatingArrangementService.updateLayout(classId, rows, cols, rowSpacingConfigJson, colSpacingConfigJson);

        // 2. 返回空的布局结构
        return seatingArrangementService.generateEmptyLayout(classId, rows, cols);
    }

    /**
     * 响应前端的随机排座请求，返回 JSON 数据
     */
    @GetMapping("/arrange/{classId}")
    @ResponseBody
    public SeatingResult getRandomArrangement(@PathVariable Long classId) {
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

    /**
     * 新增：导出当前座位布局为 PDF
     * 允许前端通过 fileName 参数传递自定义文件名
     */
    @GetMapping("/exportPdf/{classId}")
    public void exportSeatingToPdf(@PathVariable Long classId,
                                   @RequestParam(required = false) String fileName, // 接收可选文件名
                                   HttpServletResponse response) {
        try {
            // 1. 调用 Service 生成 PDF 数据
            ByteArrayOutputStream os = seatingArrangementService.exportSeatingToPdf(classId);

            // 2. 确定文件名
            String finalFilename = (fileName != null && !fileName.isEmpty() ? fileName : "SeatingArrangement_" + classId) + ".pdf";

            // 3. 配置 HTTP 响应头
            response.setContentType("application/pdf");
            // 使用 attachment 确保浏览器触发下载而不是预览
            response.setHeader("Content-Disposition", "attachment; filename=\"" + new String(finalFilename.getBytes("UTF-8"), "ISO8859-1") + "\"");
            response.setContentLength(os.size());

            // 4. 将 PDF 数据写入响应流
            OutputStream out = response.getOutputStream();
            os.writeTo(out);
            out.flush();
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                response.getWriter().write("Error: " + e.getMessage());
            } catch (Exception ignored) { /* Ignored */ }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("Internal Server Error: PDF generation failed. Details: " + e.getMessage());
            } catch (Exception ignored) { /* Ignored */ }
        }
    }
}