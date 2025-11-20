package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.Classroom;
import com.example.seatingsystem.service.ClassroomService;
import com.example.seatingsystem.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper; // ❗ 引入 Jackson ObjectMapper
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/class") // 设置顶级路径
public class ClassroomController {

    private final ClassroomService classroomService;
    private final StudentService studentService;
    private final ObjectMapper objectMapper; // ❗ 注入 ObjectMapper

    @Autowired
    public ClassroomController(ClassroomService classroomService, StudentService studentService, ObjectMapper objectMapper) {
        this.classroomService = classroomService;
        this.studentService = studentService;
        this.objectMapper = objectMapper; // 初始化 ObjectMapper
    }

    /**
     * 处理创建班级请求 (POST /class/add)
     */
    @PostMapping("/add")
    public String addClassroom(@ModelAttribute Classroom newClassroom, HttpSession session, RedirectAttributes redirectAttributes) {
        Long teacherId = (Long) session.getAttribute("userId");
        if (teacherId == null) {
            return "redirect:/login";
        }

        try {
            newClassroom.setTeacherId(teacherId);

            // 默认设置一个空的座位布局，后续在排座功能中更新
            if (newClassroom.getSeatLayout() == null || newClassroom.getSeatLayout().isEmpty()) {
                newClassroom.setSeatLayout("{\"rows\": 0, \"cols\": 0}"); // 简化布局JSON
            }

            classroomService.save(newClassroom);

            redirectAttributes.addFlashAttribute("successMessage", "班级 [" + newClassroom.getName() + "] 创建成功！");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "创建班级失败：" + e.getMessage());
        }

        return "redirect:/home"; // 创建成功后重定向回主页
    }

    /**
     * 进入班级详情和座位管理页面 (GET /class/{classId})
     */
    @GetMapping("/{classId}")
    public String showClassDetail(@PathVariable Long classId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        Optional<Classroom> optionalClassroom = classroomService.findById(classId);

        if (optionalClassroom.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "班级不存在。");
            return "redirect:/home";
        }

        Classroom classroom = optionalClassroom.get();

        if (!classroom.getTeacherId().equals(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "无权访问此班级。");
            return "redirect:/home";
        }

        // 核心修复 1: 预处理 JSON 数据，避免 Thymeleaf SpEL 错误
        int layoutRows = 0;
        int layoutCols = 0;

        if (classroom.getSeatLayout() != null && !classroom.getSeatLayout().isEmpty()) {
            try {
                // 使用 ObjectMapper 安全地解析 JSON 字符串
                @SuppressWarnings("unchecked")
                java.util.Map<String, Integer> layoutMap = objectMapper.readValue(classroom.getSeatLayout(), java.util.Map.class);
                layoutRows = layoutMap.getOrDefault("rows", 0);
                layoutCols = layoutMap.getOrDefault("cols", 0);
            } catch (Exception e) {
                // 如果解析失败，可以在 Model 中添加错误信息
                model.addAttribute("errorMessage", "座位布局数据错误，请重新设置。");
            }
        }


        // 核心修复 2: 确保 RowSpacing 和 ColSpacing 字段非空
        if (classroom.getRowSpacing() == null) { classroom.setRowSpacing(15); }
        if (classroom.getColSpacing() == null) { classroom.setColSpacing(15); }

        // 获取该班级的学生数量
        int studentCount = 0;
        try {
            studentCount = studentService.getActiveStudentsByClassId(classId).size();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "获取学生人数失败，排座功能可能受到影响。错误详情: " + e.getMessage());
        }

        model.addAttribute("classroom", classroom);
        model.addAttribute("studentCount", studentCount);
        // ❗ 传递预先解析好的行/列数
        model.addAttribute("layoutRows", layoutRows);
        model.addAttribute("layoutCols", layoutCols);


        return "class_detail"; // 返回班级详情模板
    }
}