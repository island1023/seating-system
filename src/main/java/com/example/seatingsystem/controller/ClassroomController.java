package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.Classroom;
import com.example.seatingsystem.service.ClassroomService;
import com.example.seatingsystem.service.StudentService;
import com.example.seatingsystem.service.SeatingArrangementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.seatingsystem.model.SeatingResult;

import java.util.Optional;


@Controller
@RequestMapping("/class") // 设置顶级路径
public class ClassroomController {

    private final ClassroomService classroomService;
    private final StudentService studentService;
    private final ObjectMapper objectMapper;
    private final SeatingArrangementService seatingArrangementService;

    @Autowired
    public ClassroomController(ClassroomService classroomService,
                               StudentService studentService,
                               ObjectMapper objectMapper,
                               SeatingArrangementService seatingArrangementService) {
        this.classroomService = classroomService;
        this.studentService = studentService;
        this.objectMapper = objectMapper;
        this.seatingArrangementService = seatingArrangementService;
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

            // ❗ 新增：为新的间距配置字段设置默认值（空 JSON 字符串）
            if (newClassroom.getRowSpacingConfig() == null) {
                newClassroom.setRowSpacingConfig("{}");
            }
            if (newClassroom.getColSpacingConfig() == null) {
                newClassroom.setColSpacingConfig("{}");
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


        // 核心修正 2: 获取自定义间距配置，如果为空则设为默认空 JSON 字符串 "{}"
        String rowSpacingConfig = classroom.getRowSpacingConfig() != null && !classroom.getRowSpacingConfig().isEmpty()
                ? classroom.getRowSpacingConfig() : "{}";
        String colSpacingConfig = classroom.getColSpacingConfig() != null && !classroom.getColSpacingConfig().isEmpty()
                ? classroom.getColSpacingConfig() : "{}";

        // ❗ 新增：获取最新的排座结果
        Optional<SeatingResult> latestArrangement = seatingArrangementService.getLatestArrangement(classId);

        // ❗ 传递最新的排座结果 (如果存在)
        if (latestArrangement.isPresent()) {
            // 将 SeatingResult 转换为 JSON 字符串传递给前端，确保前端能直接使用
            try {
                String arrangementJson = objectMapper.writeValueAsString(latestArrangement.get());
                model.addAttribute("latestArrangementJson", arrangementJson); // 传递完整的布局数据
            } catch (Exception e) {
                model.addAttribute("errorMessage", "加载最近排座记录失败。");
                model.addAttribute("latestArrangementJson", "{}");
            }
        } else {
            model.addAttribute("latestArrangementJson", "{}"); // 传递空 JSON 字符串
        }

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
        // ❗ 传递新的间距配置 JSON
        model.addAttribute("rowSpacingConfig", rowSpacingConfig);
        model.addAttribute("colSpacingConfig", colSpacingConfig);


        return "class_detail"; // 返回班级详情模板
    }
    /**
     * 【新增】处理更新班级信息请求 (POST /class/update)
     */
    @PostMapping("/update")
    public String updateClassroom(@ModelAttribute Classroom classroom, HttpSession session, RedirectAttributes redirectAttributes) {
        Long teacherId = (Long) session.getAttribute("userId");
        if (teacherId == null) {
            return "redirect:/login";
        }

        try {
            // 确保用户有权修改（尽管 Service 层会通过 ID 校验）
            Optional<Classroom> existing = classroomService.findById(classroom.getId());
            if (existing.isEmpty() || !existing.get().getTeacherId().equals(teacherId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "无权修改此班级。");
                return "redirect:/home";
            }

            classroomService.updateClassroom(classroom);
            redirectAttributes.addFlashAttribute("successMessage", "班级 [" + classroom.getName() + "] 修改成功！");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "修改班级失败：" + e.getMessage());
        }

        return "redirect:/home";
    }


    /**
     * 【新增】处理删除班级请求 (GET /class/delete/{classId})
     */
    @GetMapping("/delete/{classId}")
    public String deleteClassroom(@PathVariable Long classId, HttpSession session, RedirectAttributes redirectAttributes) {
        Long teacherId = (Long) session.getAttribute("userId");
        if (teacherId == null) {
            return "redirect:/login";
        }

        try {
            // 权限校验
            Optional<Classroom> existing = classroomService.findById(classId);
            if (existing.isEmpty() || !existing.get().getTeacherId().equals(teacherId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "无权删除此班级或班级不存在。");
                return "redirect:/home";
            }

            classroomService.deleteClassroom(classId);
            redirectAttributes.addFlashAttribute("successMessage", "班级已删除！");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/home";
    }
}
