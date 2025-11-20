package com.example.seatingsystem.service;

import com.example.seatingsystem.entity.User;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface UserService {

    /**
     * 注册新用户（教师）
     * @param user 待注册的用户对象（包含username和未加密的password）
     * @return 注册成功的用户对象
     * @throws RuntimeException 如果用户名已存在
     */
    User register(User user);

    /**
     * 根据用户名查找用户（用于登录认证）
     * @param username 用户名
     * @return 包含用户对象的Optional，如果不存在则为空
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户ID查找用户
     * @param id 用户ID
     * @return 包含用户对象的Optional
     */
    Optional<User> findById(Long id);
    /**
     * 更新用户基本信息和密码
     */
    User updateUser(User updatedUser, String newPassword, String oldPassword, PasswordEncoder passwordEncoder);
}