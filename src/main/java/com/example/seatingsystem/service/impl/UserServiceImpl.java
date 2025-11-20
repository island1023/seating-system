package com.example.seatingsystem.service.impl;

import com.example.seatingsystem.entity.User;
import com.example.seatingsystem.repository.UserRepository;
import com.example.seatingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // ❗ 需要在配置类中配置PasswordEncoder Bean

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(User user) {
        // 1. 校验用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("用户名 [" + user.getUsername() + "] 已被注册。");
        }

        // 2. 加密密码
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 3. 设置默认角色和姓名
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("TEACHER"); // 默认注册为教师
        }
        if (user.getRealName() == null || user.getRealName().isEmpty()) {
            user.setRealName(user.getUsername());
        }

        // 4. 保存到数据库
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(User updatedUser, String newPassword, String oldPassword, PasswordEncoder passwordEncoder) {
        // 1. 获取数据库中已存在的用户
        User existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在。"));

        // 2. 校验旧密码 (如果用户尝试修改密码)
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (!passwordEncoder.matches(oldPassword, existingUser.getPassword())) {
                throw new RuntimeException("旧密码输入不正确。");
            }
            // 设置新密码并加密
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }

        // 3. 校验用户名是否冲突
        if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
            if (userRepository.findByUsername(updatedUser.getUsername()).isPresent()) {
                throw new RuntimeException("用户名 [" + updatedUser.getUsername() + "] 已被占用。");
            }
            existingUser.setUsername(updatedUser.getUsername());
        }

        // 4. 更新真实姓名
        existingUser.setRealName(updatedUser.getRealName());

        return userRepository.save(existingUser);
    }
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}