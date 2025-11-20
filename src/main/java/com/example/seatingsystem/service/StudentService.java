package com.example.seatingsystem.service;

import com.example.seatingsystem.entity.Student;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface StudentService {

    /**
     * 获取指定班级所有活跃的学生
     */
    List<Student> getActiveStudentsByClassId(Long classId);

    /**
     * 手动添加单个学生
     */
    Student addStudent(Student student);

    /**
     * 根据ID查找学生
     */
    Optional<Student> findById(Long studentId);

    /**
     * 根据班级ID和学号检查学生是否存在（校验重复）
     */
    Optional<Student> findByClassIdAndStudentNo(Long classId, String studentNo);

    /**
     * 从 Excel 文件批量导入学生数据
     */
    int importFromExcel(MultipartFile file, Long classId) throws Exception;

    // --- 新增：更新单个学生信息 ---
    /**
     * 更新单个学生的非核心信息（如姓名、性别、自定义信息）。
     * @param student 包含要更新信息的 Student 对象，ID必须存在。
     * @return 更新后的 Student 对象
     */
    Student updateStudent(Student student);

    // --- 新增：删除单个学生（软删除） ---
    /**
     * 根据 ID 物理删除单个学生（设置 isActive=false）。
     * @param studentId 要删除的学生的 ID
     */
    void deleteStudentById(Long studentId);
}