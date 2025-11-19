package com.example.seatingsystem.service.impl;

import com.example.seatingsystem.entity.Classroom;
import com.example.seatingsystem.repository.ClassroomRepository;
import com.example.seatingsystem.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Classroom save(Classroom classroom) {
        // 可以在这里添加班级名称校验等业务逻辑
        return classroomRepository.save(classroom);
    }

    @Override
    public Optional<Classroom> findById(Long id) {
        return classroomRepository.findById(id);
    }
}