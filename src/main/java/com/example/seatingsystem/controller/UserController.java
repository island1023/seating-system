package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.User;
import com.example.seatingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 显示用户资料页面 (GET /user/profile)
     */
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        // 从数据库获取最新用户数据
        // 确保 userService.findById() 存在
        User currentUser = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户会话无效。请重新登录。"));

        model.addAttribute("user", currentUser);
        return "user_profile"; // 返回用户资料模板
    }

    /**
     * 处理更新用户资料请求 (POST /user/update)
     */
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User user,
                                @RequestParam(required = false) String oldPassword,
                                @RequestParam(required = false) String newPassword,
                                HttpSession session, RedirectAttributes redirectAttributes) {

        // 强制设置 ID 为当前登录用户，防止恶意修改他人资料
        user.setId((Long) session.getAttribute("userId"));

        try {
            // 确保 userService.updateUser() 存在且签名正确
            User updatedUser = userService.updateUser(user, newPassword, oldPassword, passwordEncoder);

            // 更新 Session 中的用户信息，以便导航栏显示最新姓名/用户名
            session.setAttribute("currentUser", updatedUser);

            redirectAttributes.addFlashAttribute("successMessage", "资料更新成功！");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/user/profile";
    }
}