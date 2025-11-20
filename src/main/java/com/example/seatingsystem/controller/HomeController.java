package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.Classroom;
import com.example.seatingsystem.entity.User;
import com.example.seatingsystem.service.ClassroomService;
import com.example.seatingsystem.service.StudentService; // ❗ 1. 新增 StudentService 依赖
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final ClassroomService classroomService;
    private final StudentService studentService; // ❗ 2. 注入 StudentService

    @Autowired
    public HomeController(ClassroomService classroomService, StudentService studentService) {
        this.classroomService = classroomService;
        this.studentService = studentService; // ❗ 3. 构造函数中初始化
    }

    /**
     * 系统主页：显示老师管理的班级列表
     */
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        // 1. 从 Session 中获取当前登录的用户ID
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login"; // 未登录则重定向到登录页
        }

        // 2. 根据教师ID获取他管理的班级列表
        List<Classroom> classrooms = classroomService.findByTeacher(userId);

        // ❗ 4. 核心逻辑：计算每个班级的学生数量
        Map<Long, Integer> studentCounts = new HashMap<>();

        for (Classroom classroom : classrooms) {
            // 调用 StudentService 获取活跃学生列表，并计算数量
            int count = studentService.getActiveStudentsByClassId(classroom.getId()).size();
            studentCounts.put(classroom.getId(), count);
        }

        // 5. 必须为模态框添加一个空的 Classroom 对象用于表单绑定 (防止 home.html 报错)
        model.addAttribute("newClassroom", new Classroom());

        // 6. 将数据传入前端
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("currentUser", (User) session.getAttribute("currentUser"));
        model.addAttribute("studentCounts", studentCounts); // ❗ 传递人数 Map

        return "home";
    }
}