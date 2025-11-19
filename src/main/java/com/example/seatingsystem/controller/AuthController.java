package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.User;
import com.example.seatingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 确保 pom.xml 已添加 spring-boot-starter-security
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

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
     * - 处理 GET /login, /register, /
     * - @ModelAttribute("user") 确保了注册表单在失败返回时能正确绑定数据，也解决了初次访问时 '无法解析 user' 的报错。
     */

    @GetMapping({"/login", "/register", "/"})
    public String showAuthForm(@RequestParam(required = false, defaultValue = "login") String activeTab,
                               // 使用 @ModelAttribute 接收 Flash Attribute，
                               // 但我们将在方法内部保证 Model 的完整性
                               @ModelAttribute("user") User userFlash,
                               @ModelAttribute("successMessage") String successMessage,
                               @ModelAttribute("errorMessage") String errorMessage,
                               Model model) {

        // 1. **核心修正：** 检查 Model 中是否缺少 'user'，如果是初次加载，则手动添加
        // 这样可以避免在初次访问时，注册表单的 th:object="${user}" 找不到对象
        if (!model.containsAttribute("user")) {
            // 如果 flash attribute 传回了 user 对象（注册失败时），使用它。
            // 否则，创建一个新的 User 对象。
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
     */
    @PostMapping("/login")
    public String processLogin(@ModelAttribute User loginUser, HttpSession session, RedirectAttributes redirectAttributes) {
        // 1. 根据用户名查找用户
        Optional<User> userOptional = userService.findByUsername(loginUser.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 2. 校验密码
            if (passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
                // 3. 登录成功：存入 Session
                session.setAttribute("currentUser", user);
                session.setAttribute("userId", user.getId());

                // 4. 跳转到主页
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
        return "redirect:/login";
    }
}