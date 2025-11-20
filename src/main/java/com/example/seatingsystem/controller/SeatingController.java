package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.SeatingRecord;
import com.example.seatingsystem.model.SeatingResult;
import com.example.seatingsystem.service.SeatingArrangementService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

    // 假设您在前端设置布局的接口是 /seating/layout/update
    // SeatingController.java (updateLayout 方法)

    @PostMapping("/layout/update")
    public String updateLayout(@RequestParam Long classId,
                               @RequestParam int rows,
                               @RequestParam int cols,
                               @RequestParam int rowSpacing, // ❗ 新增参数
                               @RequestParam int colSpacing, // ❗ 新增参数
                               RedirectAttributes redirectAttributes) {
        try {
            // ❗ 修正：调用 service 时传入所有参数
            seatingArrangementService.updateLayout(classId, rows, cols, rowSpacing, colSpacing);
            redirectAttributes.addFlashAttribute("successMessage", "座位布局设置成功！");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        // 修正：重定向回班级详情页 /class/{classId}
        return "redirect:/class/" + classId;
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