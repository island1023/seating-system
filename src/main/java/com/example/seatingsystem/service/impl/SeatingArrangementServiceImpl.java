package com.example.seatingsystem.service.impl;

import com.example.seatingsystem.entity.Classroom;
import com.example.seatingsystem.entity.SeatingRecord;
import com.example.seatingsystem.entity.Student;
import com.example.seatingsystem.model.SeatingPosition;
import com.example.seatingsystem.model.SeatingResult;
import com.example.seatingsystem.repository.ClassroomRepository;
import com.example.seatingsystem.repository.SeatingRecordRepository;
import com.example.seatingsystem.service.StudentService;
import com.example.seatingsystem.service.SeatingArrangementService;
import com.fasterxml.jackson.databind.ObjectMapper; // 用于对象转JSON
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class SeatingArrangementServiceImpl implements SeatingArrangementService {

    private final ClassroomRepository classroomRepository;
    private final SeatingRecordRepository seatingRecordRepository;
    private final StudentService studentService;
    private final ObjectMapper objectMapper; // 用于JSON序列化

    @Autowired
    public SeatingArrangementServiceImpl(ClassroomRepository classroomRepository,
                                         SeatingRecordRepository seatingRecordRepository,
                                         StudentService studentService,
                                         ObjectMapper objectMapper) {
        this.classroomRepository = classroomRepository;
        this.seatingRecordRepository = seatingRecordRepository;
        this.studentService = studentService;
        this.objectMapper = objectMapper;
    }

    // JSON 格式示例：{"rows":6, "cols":8, "layout": []}
    @Override
    @Transactional
    public Classroom updateLayout(Long classId, int rows, int cols, int rowSpacing, int colSpacing) { // ❗ 方法签名更改
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在。"));

        // ❗ 核心验证：座位数 < 人数时，不允许保存
        List<Student> students = studentService.getActiveStudentsByClassId(classId);
        if (rows * cols < students.size()) {
            throw new RuntimeException("总座位数 (" + (rows * cols) + ") 小于学生总人数 (" + students.size() + ")，请增加行/列数！");
        }

        try {
            // 构造新的布局信息（JSON 字符串）
            String layoutJson = String.format("{\"rows\":%d, \"cols\":%d}", rows, cols);
            classroom.setSeatLayout(layoutJson);

            // ❗ 保存间距配置
            classroom.setRowSpacing(rowSpacing);
            classroom.setColSpacing(colSpacing);

            return classroomRepository.save(classroom);
        } catch (RuntimeException e) {
            throw e; // 重新抛出自定义运行时异常
        } catch (Exception e) {
            throw new RuntimeException("更新座位布局失败: " + e.getMessage());
        }
    }


    /**
     * 核心算法：生成随机座位排布
     */
    @Override
    public SeatingResult randomArrange(Long classId) {
        // 1. 获取班级布局信息 (需要解析 JSON 字符串)
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在。"));

        int rows = 0;
        int cols = 0;

        try {
            String layoutJson = classroom.getSeatLayout();

            // ❗ 检查逻辑：如果座位布局为空，则抛出异常要求用户先设置布局
            if (layoutJson == null || layoutJson.isEmpty()) {
                throw new RuntimeException("请先设置班级的座位行数和列数。");
            }

            // ❗ 修正：使用 ObjectMapper 解析 JSON 结构 {"rows": R, "cols": C}
            @SuppressWarnings("unchecked")
            java.util.Map<String, Integer> layoutMap = objectMapper.readValue(layoutJson, java.util.Map.class);

            rows = layoutMap.getOrDefault("rows", 0);
            cols = layoutMap.getOrDefault("cols", 0);

            // 验证解析结果
            if (rows <= 0 || cols <= 0) {
                throw new RuntimeException("座位布局配置数据无效，请重新设置。");
            }

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // 捕获 JSON 解析错误
            throw new RuntimeException("座位布局配置解析错误，请联系管理员。", e);
        } catch (RuntimeException e) {
            throw e; // 重新抛出自定义运行时异常 (如“请先设置布局”)
        } catch (Exception e) {
            throw new RuntimeException("获取座位布局失败: " + e.getMessage());
        }


        // 2. 获取所有活跃学生
        List<Student> students = studentService.getActiveStudentsByClassId(classId);

        // 3. 核心：随机打乱学生顺序
        Collections.shuffle(students);

        // 4. 生成座位结果
        List<SeatingPosition> layout = new ArrayList<>();
        int studentIndex = 0;
        // int totalSeats = rows * cols; // 此变量在此处不再需要，但保留注释以供参考

        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                Student student = null;

                if (studentIndex < students.size()) {
                    // 只要有学生，就按顺序分配
                    student = students.get(studentIndex);
                    studentIndex++;
                }

                // 创建 SeatingPosition，未分配学生的座位 student=null
                layout.add(new SeatingPosition(r, c, student));
            }
        }

        // 5. 构造结果对象
        SeatingResult result = new SeatingResult();
        result.setRows(rows);
        result.setCols(cols);
        result.setLayout(layout);

        return result;
    }

    /**
     * 保存当前排座结果的快照
     */
    @Override
    @Transactional
    public SeatingRecord saveArrangement(Long classId, SeatingResult result, String recordName) {
        SeatingRecord record = new SeatingRecord();
        record.setClassId(classId);
        record.setRecordName(recordName);

        try {
            // 核心：将 SeatingResult 对象转换为 JSON 字符串存储
            String snapshotJson = objectMapper.writeValueAsString(result);
            record.setLayoutSnapshot(snapshotJson);
        } catch (Exception e) {
            throw new RuntimeException("保存排座记录失败，JSON 序列化错误。");
        }

        return seatingRecordRepository.save(record);
    }

    @Override
    public List<SeatingRecord> getRecordsByClassId(Long classId) {
        return seatingRecordRepository.findByClassIdOrderByCreateTimeDesc(classId);
    }
}