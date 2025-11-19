package com.example.seatingsystem.repository;

import com.example.seatingsystem.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    /**
     * 查找某一老师管理的所有班级
     * @param teacherId 教师ID
     * @return 该教师的班级列表
     */
    List<Classroom> findByTeacherId(Long teacherId);
}