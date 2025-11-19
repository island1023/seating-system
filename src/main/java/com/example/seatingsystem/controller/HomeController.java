package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.Classroom;
import com.example.seatingsystem.entity.User;
import com.example.seatingsystem.service.ClassroomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final ClassroomService classroomService; // 依赖班级服务

    @Autowired
    public HomeController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    /**
     * 系统主页：显示老师管理的班级列表
     */
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        // 1. 从 Session 中获取当前登录的用户ID
        Long userId = (Long) session.getAttribute("userId");

        // 确保用户已登录，实际项目中应由 Spring Security 处理，此处做简单检查
        if (userId == null) {
            return "redirect:/login"; // 未登录则重定向到登录页
        }

        // 2. 根据教师ID获取他管理的班级列表
        List<Classroom> classrooms = classroomService.findByTeacher(userId);

        // 3. 将数据放入 Model 供前端展示
        model.addAttribute("classrooms", classrooms);

        // 4. 将用户信息也传入前端，方便展示
        model.addAttribute("currentUser", (User) session.getAttribute("currentUser"));

        // 5. 返回 home.html 模板
        return "home";
    }
}