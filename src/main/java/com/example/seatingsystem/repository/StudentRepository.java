package com.example.seatingsystem.repository;

import com.example.seatingsystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * 查找某一班级的所有学生
     * @param classId 班级ID
     * @return 学生列表
     */
    List<Student> findByClassIdAndIsActiveTrue(Long classId);

    /**
     * 检查某一班级下的学号是否已存在（用于导入和手动添加校验）
     */
    Optional<Student> findByClassIdAndStudentNo(Long classId, String studentNo);
}