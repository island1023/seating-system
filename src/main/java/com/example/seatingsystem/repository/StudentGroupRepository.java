package com.example.seatingsystem.repository;

import com.example.seatingsystem.entity.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {

    /**
     * 查找某一班级的所有学生分组
     * @param classId 班级ID
     * @return 分组列表
     */
    List<StudentGroup> findByClassId(Long classId);
}