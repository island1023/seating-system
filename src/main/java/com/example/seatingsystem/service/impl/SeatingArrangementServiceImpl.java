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
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

// ❗ iText 导入
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;


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
    public Classroom updateLayout(Long classId, int rows, int cols, String rowSpacingConfigJson, String colSpacingConfigJson) {
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

            // ❗ 保存间距配置 (直接保存传入的 JSON 字符串)
            classroom.setRowSpacingConfig(rowSpacingConfigJson);
            classroom.setColSpacingConfig(colSpacingConfigJson);

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


    // SeatingArrangementServiceImpl.java (新增 generateEmptyLayout 方法)

    @Override
    public SeatingResult generateEmptyLayout(Long classId, int rows, int cols) {
        List<SeatingPosition> layout = new ArrayList<>();

        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                // 创建空座位：student=null
                layout.add(new SeatingPosition(r, c, null));
            }
        }

        SeatingResult result = new SeatingResult();
        result.setRows(rows);
        result.setCols(cols);
        result.setLayout(layout);
        return result;
    }

    @Override
    public List<SeatingRecord> getRecordsByClassId(Long classId) {
        return seatingRecordRepository.findByClassIdOrderByCreateTimeDesc(classId);
    }

    /**
     * 实现：获取最新的排座记录（作为当前座位状态）
     */
    @Override
    public Optional<SeatingResult> getLatestArrangement(Long classId) {
        // 1. 获取最新的记录 (Repository 已经按时间倒序排序)
        List<SeatingRecord> records = seatingRecordRepository.findByClassIdOrderByCreateTimeDesc(classId);

        if (records.isEmpty()) {
            return Optional.empty();
        }

        SeatingRecord latestRecord = records.get(0);
        String snapshotJson = latestRecord.getLayoutSnapshot();

        try {
            // 2. 将 JSON 快照解析回 SeatingResult 对象
            SeatingResult result = objectMapper.readValue(snapshotJson, SeatingResult.class);
            return Optional.of(result);
        } catch (JsonProcessingException e) {
            // 如果 JSON 解析失败，返回空
            System.err.println("Error parsing seating snapshot for classId " + classId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 实现：导出当前排座结果为 PDF
     */
    @Override
    public ByteArrayOutputStream exportSeatingToPdf(Long classId) throws Exception {
        // 1. 获取班级信息和最新排座结果
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在。"));

        Optional<SeatingResult> resultOptional = getLatestArrangement(classId);

        if (resultOptional.isEmpty()) {
            throw new RuntimeException("班级 [" + classroom.getName() + "] 尚无保存的座位布局记录，无法导出 PDF。");
        }

        SeatingResult result = resultOptional.get();
        int rows = result.getRows();
        int cols = result.getCols();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate()); // 使用横向 A4 页面
        PdfWriter.getInstance(document, os);
        document.open();

        BaseFont baseFont;
        // 1. 使用 Spring ClassPathResource 确保在打包后也能找到资源
        ClassPathResource fontResource = new ClassPathResource("fonts/NotoSansCJK-Regular.otf");

        try (InputStream fontStream = fontResource.getInputStream()) {
            if (fontStream == null) {
                // 抛出更清晰的错误信息
                throw new RuntimeException("Font resource 'fonts/NotoSansCJK-Regular.otf' not found in classpath.");
            }

            // 2. 读取字体字节并创建 BaseFont
            byte[] fontBytes = fontStream.readAllBytes();

            // 3. 使用自定义名称和字节数组创建 BaseFont
            baseFont = BaseFont.createFont(
                    "NotoSansCJK-Regular.otf",     // 使用文件路径作为内部别名
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    BaseFont.CACHED,           // 解决 'is not recognized' 错误
                    fontBytes,
                    null
            );
        }

        Font titleFont = new Font(baseFont, 18, Font.BOLD);
        Font headerFont = new Font(baseFont, 10, Font.BOLD, BaseColor.WHITE);
        Font cellFont = new Font(baseFont, 10, Font.NORMAL);


        // 2. 写入标题
        Paragraph title = new Paragraph(classroom.getName() + " 座位安排表", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // 3. 绘制讲台
        PdfPTable deskTable = new PdfPTable(1);
        deskTable.setWidthPercentage(50);
        PdfPCell deskCell = new PdfPCell(new Phrase("讲台 / Blackboard", headerFont));
        deskCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        deskCell.setBackgroundColor(BaseColor.DARK_GRAY);
        deskCell.setPadding(8);
        deskTable.addCell(deskCell);
        document.add(deskTable);
        document.add(Chunk.NEWLINE);


        // 4. 创建座位表格
        PdfPTable seatingTable = new PdfPTable(cols);
        seatingTable.setWidthPercentage(100);
        seatingTable.getDefaultCell().setPadding(5);
        seatingTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        // 5. 填充表格
        for (int r = 1; r <= rows; r++) {
            // 解决 lambda 表达式的 final 问题：创建 final 副本
            final int currentRow = r;
            for (int c = 1; c <= cols; c++) {
                final int currentCol = c; // 解决 lambda 表达式的 final 问题：创建 final 副本

                PdfPCell cell = new PdfPCell();
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setMinimumHeight(40);
                cell.setPadding(5);

                SeatingPosition position = result.getLayout().stream()
                        // 引用 final 变量
                        .filter(p -> p.getRow() == currentRow && p.getCol() == currentCol)
                        .findFirst()
                        .orElse(null);

                String text;
                BaseColor color;
                if (position != null && position.getStudentId() != null) {
                    // 有人
                    text = position.getStudentName() + "\n(" + currentRow + "-" + currentCol + ")";
                    color = position.getGender() != null && position.getGender().equals("男") ? new BaseColor(91, 192, 222) : new BaseColor(255, 153, 204); // 蓝色/粉色
                    cell.setBackgroundColor(color);
                    cell.setBorderColor(BaseColor.BLACK); // 确保边框可见
                } else {
                    // ❗ 核心修改：使空座不可见 (透明)
                    text = "";
                    color = BaseColor.WHITE;
                    cell.setBorder(PdfPCell.NO_BORDER); // 移除边框
                    cell.setBackgroundColor(color);
                }

                cell.setPhrase(new Phrase(text, cellFont));
                seatingTable.addCell(cell);
            }
        }

        document.add(seatingTable);
        document.close();

        return os;
    }
}