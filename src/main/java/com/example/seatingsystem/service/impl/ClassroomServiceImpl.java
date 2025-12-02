package com.example.seatingsystem.service.impl;

import com.example.seatingsystem.entity.Classroom;
import com.example.seatingsystem.repository.ClassroomRepository;
import com.example.seatingsystem.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;

    @Autowired
    public ClassroomServiceImpl(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @Override
    public List<Classroom> findByTeacher(Long teacherId) {
        // 调用 Repository 层的方法
        return classroomRepository.findByTeacherId(teacherId);
    }

    @Override
    @Transactional
    public Classroom save(Classroom classroom) {
        // 可以在这里添加班级名称校验等业务逻辑
        return classroomRepository.save(classroom);
    }

    @Override
    public Optional<Classroom> findById(Long id) {
        return classroomRepository.findById(id);
    }

    // 【修正方法】更新班级信息 (修改名称和描述)
    @Override
    @Transactional
    public Classroom updateClassroom(Classroom updatedClassroom) {
        Classroom existingClassroom = classroomRepository.findById(updatedClassroom.getId())
                .orElseThrow(() -> new RuntimeException("班级不存在，无法更新。"));

        if (updatedClassroom.getName() == null || updatedClassroom.getName().trim().isEmpty()) {
            throw new RuntimeException("班级名称不能为空。");
        }

        existingClassroom.setName(updatedClassroom.getName());
        existingClassroom.setDescription(updatedClassroom.getDescription());

        return classroomRepository.save(existingClassroom);
    }

    // 【核心修正方法】删除班级，现在依赖数据库的级联删除
    @Override
    @Transactional
    public void deleteClassroom(Long classId) {
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在，无法删除。"));

        // ❗ 核心修正：由于 seating.sql 已添加 ON DELETE CASCADE，
        // 当删除 Classroom 记录时，所有关联的 Student, SeatingRecord, StudentGroup 记录将被自动删除。
        classroomRepository.delete(classroom);
        // 如果删除成功，则无需额外的 try-catch 块。
    }
}
