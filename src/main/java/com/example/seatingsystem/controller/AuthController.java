package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.User;
import com.example.seatingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest; // <-- 新增导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// ============== Spring Security & Java Utility Imports ==============
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository; // <-- 新增导入
import java.util.Collections;
import java.util.List;
import java.util.Optional;
// ====================================================================

@Controller
@RequestMapping("/")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 访问统一认证页面（包含登录和注册）
     */
    @GetMapping({"/login", "/register", "/"})
    public String showAuthForm(@RequestParam(required = false, defaultValue = "login") String activeTab,
                               @ModelAttribute("user") User userFlash,
                               @ModelAttribute("successMessage") String successMessage,
                               @ModelAttribute("errorMessage") String errorMessage,
                               Model model) {

        // 1. 检查 Model 中是否缺少 'user'，如果是初次加载，则手动添加
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", userFlash != null ? userFlash : new User());
        }

        // 2. 传递消息 (只有在 Flash Attribute 存在时才放入 Model)
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }

        // 3. 传递 Tab 信息
        model.addAttribute("activeTab", activeTab);
        return "auth"; // 返回统一的 auth.html 模板
    }

    /**
     * 处理登录请求
     * 新增 HttpServletRequest 参数用于显式保存 Security Context
     */
    @PostMapping("/login")
    public String processLogin(@ModelAttribute User loginUser, HttpSession session, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // 1. 根据用户名查找用户
        Optional<User> userOptional = userService.findByUsername(loginUser.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 2. 校验密码
            if (passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {

                // ==========================================================
                // 核心修正：通知 Spring Security 登录成功
                // ==========================================================

                // 1. 创建用户的权限列表 (Authority List)
                List<GrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority(user.getRole())
                );

                // 2. 创建认证令牌 (Authentication Token)
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user.getUsername(), // principal: 用户的登录名
                        null,             // credentials: 密码，认证成功后设为 null
                        authorities       // authorities: 用户的权限/角色
                );

                // 3. 将令牌设置到 Security Context 中
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 4. **核心修正：将 SecurityContext 显式保存到 Session**
                request.getSession().setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext()
                );

                // ==========================================================

                // 5. （可选）保留你的自定义 Session 属性，供业务逻辑使用
                session.setAttribute("currentUser", user);
                session.setAttribute("userId", user.getId());

                // 6. 跳转到主页
                return "redirect:/home";
            }
        }

        // 登录失败（用户不存在 或 密码错误）
        redirectAttributes.addFlashAttribute("errorMessage", "登录失败：用户名或密码不正确。");
        // 重定向回认证页，并激活登录 Tab
        redirectAttributes.addAttribute("activeTab", "login");
        return "redirect:/login";
    }

    /**
     * 处理注册请求
     */
    @PostMapping("/register")
    public String processRegistration(@ModelAttribute User newUser, RedirectAttributes redirectAttributes) {
        try {
            // 默认角色和时间戳在 Service 层设置
            userService.register(newUser);

            // 注册成功：重定向到登录 Tab，并显示成功提示
            redirectAttributes.addFlashAttribute("successMessage", "恭喜！注册成功，请登录。");
            redirectAttributes.addAttribute("activeTab", "login");
            return "redirect:/login";
        } catch (RuntimeException e) {
            // 注册失败：重定向到注册 Tab，并显示错误提示，同时保留用户输入的数据
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("user", newUser); // 保留用户输入
            redirectAttributes.addAttribute("activeTab", "register");
            return "redirect:/register";
        }
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 清除 Session

        // 额外的清理工作（可选，但推荐）
        SecurityContextHolder.clearContext();

        return "redirect:/login";
    }
}