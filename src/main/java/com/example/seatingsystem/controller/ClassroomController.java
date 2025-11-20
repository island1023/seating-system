package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.Classroom;
import com.example.seatingsystem.service.ClassroomService;
import com.example.seatingsystem.service.StudentService; // 用于班级详情页统计学生
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
    private final StudentService studentService; // 用于获取班级学生数量

    @Autowired
    public ClassroomController(ClassroomService classroomService, StudentService studentService) {
        this.classroomService = classroomService;
        this.studentService = studentService;
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
            // 设置当前老师为班级所有者
            newClassroom.setTeacherId(teacherId);

            // 默认设置一个空的座位布局，后续在排座功能中更新
            if (newClassroom.getSeatLayout() == null || newClassroom.getSeatLayout().isEmpty()) {
                newClassroom.setSeatLayout("{\"rows\": 0, \"cols\": 0, \"layout\": []}");
            }

            classroomService.save(newClassroom); // 使用 save 方法保存或更新

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

        // 权限校验：确保该班级属于当前登录的老师
        if (!classroom.getTeacherId().equals(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "无权访问此班级。");
            return "redirect:/home";
        }

        // 获取该班级的学生数量（用于前端展示，尽管在 home.html 中实现更方便）
        int studentCount = studentService.getActiveStudentsByClassId(classId).size();

        model.addAttribute("classroom", classroom);
        model.addAttribute("studentCount", studentCount);

        return "class_detail"; // 返回班级详情模板
    }

    // TODO: 可以在这里添加 editClassroom 和 deleteClassroom 方法
}