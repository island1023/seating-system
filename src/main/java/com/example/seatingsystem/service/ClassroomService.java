package com.example.seatingsystem.service;

import com.example.seatingsystem.entity.Classroom;
import java.util.List;
import java.util.Optional;

public interface ClassroomService {

    /**
     * 根据教师ID查找该教师管理的所有班级
     * @param teacherId 教师ID
     * @return 班级列表
     */
    List<Classroom> findByTeacher(Long teacherId);

    /**
     * 创建或更新一个班级
     */
    Classroom save(Classroom classroom);

    /**
     * 根据ID查找班级
     */
    Optional<Classroom> findById(Long id);

    // 【新增】更新班级名称和描述
    Classroom updateClassroom(Classroom classroom);

    // 【新增】删除班级
    void deleteClassroom(Long classId);
}