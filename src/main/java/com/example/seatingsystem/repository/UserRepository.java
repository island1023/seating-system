package com.example.seatingsystem.repository;

import com.example.seatingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户（用于登录和注册校验）
     * @param username 登录用户名
     * @return 包含 User 对象的 Optional
     */
    Optional<User> findByUsername(String username);
}