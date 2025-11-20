package com.example.seatingsystem.service.impl;

import com.example.seatingsystem.entity.Student;
import com.example.seatingsystem.repository.StudentRepository;
import com.example.seatingsystem.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
import java.util.ArrayList;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // StudentServiceImpl.java (getActiveStudentsByClassId 方法)
    @Override
    public List<Student> getActiveStudentsByClassId(Long classId) {
        // 确保使用 Repository 中唯一且正确的方法名
        return studentRepository.findByClassIdAndIsActiveTrueOrderByStudentNoAsc(classId);
    }

    @Override
    @Transactional
    public Student addStudent(Student student) {
        // 校验：检查学号是否重复
        Optional<Student> existingStudent = studentRepository.findByClassIdAndStudentNo(
                student.getClassId(), student.getStudentNo()
        );
        if (existingStudent.isPresent()) {
            throw new RuntimeException("该班级中，学号 " + student.getStudentNo() + " 已存在。");
        }

        // 设置默认值
        student.setIsActive(true);
        return studentRepository.save(student);
    }

    @Override
    public Optional<Student> findById(Long studentId) {
        return studentRepository.findById(studentId);
    }

    @Override
    public Optional<Student> findByClassIdAndStudentNo(Long classId, String studentNo) {
        return studentRepository.findByClassIdAndStudentNo(classId, studentNo);
    }

    /**
     * 核心方法：从 Excel 文件批量导入学生数据 (使用 Apache POI)
     */
    @Override
    @Transactional
    public int importFromExcel(MultipartFile file, Long classId) throws Exception {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
            throw new IllegalArgumentException("请上传有效的 XLSX 格式文件。");
        }

        List<Student> studentsToSave = new ArrayList<>();
        int successCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // 默认读取第一个工作表

            // 假设第一行是标题行，从第二行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) continue;

                try {
                    // 1. 读取核心字段 (假设列顺序: 学号, 姓名, 性别)
                    String studentNo = getCellValue(row.getCell(0));
                    String name = getCellValue(row.getCell(1));
                    String gender = getCellValue(row.getCell(2));
                    String customInfo = getCellValue(row.getCell(3)); // 读取自定义信息

                    if (studentNo.isEmpty() || name.isEmpty()) {
                        throw new IllegalArgumentException("第 " + (i + 1) + " 行：学号和姓名不能为空。");
                    }

                    // 2. 校验并获取对象：实现覆盖逻辑
                    Optional<Student> existingStudent = studentRepository.findByClassIdAndStudentNo(classId, studentNo);

                    Student student;
                    if (existingStudent.isPresent()) {
                        // 覆盖/更新逻辑：如果存在，使用旧对象并更新其字段
                        student = existingStudent.get();

                        // 确保旧记录是活跃状态，如果是非活跃状态，则重新激活
                        if (!student.getIsActive()) {
                            student.setIsActive(true);
                        }
                    } else {
                        // 新增逻辑：如果不存在，创建新对象
                        student = new Student();
                        student.setClassId(classId); // 绑定班级ID
                        student.setIsActive(true);    // 设置为活跃
                    }

                    // 3. 更新/设置对象字段 (无论是新增还是更新，都用 Excel 数据覆盖)
                    student.setStudentNo(studentNo);
                    student.setName(name);
                    student.setGender(gender.toUpperCase().startsWith("男") ? "男" : "女");
                    student.setCustomInfo(customInfo);

                    studentsToSave.add(student);
                    successCount++;

                } catch (Exception rowEx) {
                    // 可以在日志中记录错误，但不中断整个事务
                    // 考虑到期末项目要求，这里选择抛出异常中断，让用户修正文件
                    throw rowEx;
                }
            }

            // 4. 批量保存
            studentRepository.saveAll(studentsToSave);

        } catch (RuntimeException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            // 捕获文件读取异常
            throw new Exception("文件解析失败，请检查文件格式或数据类型。", e);
        }

        return successCount;
    }

    /**
     * 辅助方法：获取单元格的值并转换为 String
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        // 设置单元格类型以避免格式错误，特别是数字学号
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    @Override
    @Transactional
    public Student updateStudent(Student student) {
        // 1. 检查学生是否存在
        Student existingStudent = studentRepository.findById(student.getId())
                .orElseThrow(() -> new RuntimeException("学生 ID:" + student.getId() + " 不存在，无法更新。"));

        // 2. 检查学号 (StudentNo) 是否被修改且与同班级其他学生冲突
        // 仅当学号发生变化时才进行校验
        if (!existingStudent.getStudentNo().equals(student.getStudentNo())) {
            Optional<Student> conflictStudent = studentRepository.findByClassIdAndStudentNo(
                    student.getClassId(), student.getStudentNo()
            );
            // 确保学号冲突的不是自己本身
            if (conflictStudent.isPresent() && !conflictStudent.get().getId().equals(student.getId())) {
                throw new RuntimeException("学号 " + student.getStudentNo() + " 已被同班级其他学生占用。");
            }
        }

        // 3. 更新字段 (只更新允许用户修改的字段)
        existingStudent.setStudentNo(student.getStudentNo());
        existingStudent.setName(student.getName());
        existingStudent.setGender(student.getGender());
        existingStudent.setCustomInfo(student.getCustomInfo());

        return studentRepository.save(existingStudent);
    }

    @Override
    @Transactional
    public void deleteStudentById(Long studentId) {
        // 1. 查找学生
        studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生 ID:" + studentId + " 不存在，无法删除。"));

        // 2. 硬删除（物理删除）
        studentRepository.deleteById(studentId);

    }
}