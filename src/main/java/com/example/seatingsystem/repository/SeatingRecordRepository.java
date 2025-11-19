package com.example.seatingsystem.repository;

import com.example.seatingsystem.entity.SeatingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatingRecordRepository extends JpaRepository<SeatingRecord, Long> {

    /**
     * 查找某一班级的所有历史排座记录
     * @param classId 班级ID
     * @return 排座记录列表
     */
    List<SeatingRecord> findByClassIdOrderByCreateTimeDesc(Long classId);
}