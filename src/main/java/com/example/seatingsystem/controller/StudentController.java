package com.example.seatingsystem.controller;

import com.example.seatingsystem.entity.Classroom;
import com.example.seatingsystem.entity.Student;
import com.example.seatingsystem.service.ClassroomService;
import com.example.seatingsystem.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class StudentController {

    private final StudentService studentService;
    private final ClassroomService classroomService;

    @Autowired
    public StudentController(StudentService studentService, ClassroomService classroomService) {
        this.studentService = studentService;
        this.classroomService = classroomService;
    }

    /**
     * 显示班级学生列表和管理页面
     * 路径: /class/{classId}/students
     */
    @GetMapping("/class/{classId}/students")
    public String showStudentManagement(@PathVariable Long classId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        // 1. 检查班级和权限
        Classroom classroom = classroomService.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在"));

        if (!classroom.getTeacherId().equals(userId)) {
            model.addAttribute("error", "无权访问此班级。");
            return "redirect:/home";
        }

        // 2. 获取数据
        List<Student> students = studentService.getActiveStudentsByClassId(classId);

        // 3. 传递给前端
        model.addAttribute("classroom", classroom);
        model.addAttribute("students", students);
        model.addAttribute("newStudent", new Student()); // 用于手动添加表单绑定

        return "students_manage"; // 返回学生管理模板
    }

    /**
     * 处理手动添加单个学生请求
     */
    @PostMapping("/student/add")
    public String addStudent(@ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        try {
            studentService.addStudent(student);
            redirectAttributes.addFlashAttribute("successMessage", "学生 " + student.getName() + " 添加成功!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/class/" + student.getClassId() + "/students";
    }

    /**
     * 处理 Excel 批量导入请求
     * 路径: /student/import
     */
    @PostMapping("/student/import")
    public String importStudents(@RequestParam("file") MultipartFile file,
                                 @RequestParam("classId") Long classId,
                                 RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "文件不能为空。");
            return "redirect:/class/" + classId + "/students";
        }

        try {
            int count = studentService.importFromExcel(file, classId);
            redirectAttributes.addFlashAttribute("successMessage", "成功导入 " + count + " 条学生数据！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "导入失败: " + e.getMessage());
        }

        return "redirect:/class/" + classId + "/students";
    }

    /**
     * 处理删除学生请求
     */
    @GetMapping("/student/delete/{studentId}")
    public String deleteStudent(@PathVariable Long studentId, @RequestParam Long classId, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudentById(studentId);
            redirectAttributes.addFlashAttribute("successMessage", "学生信息删除成功 (已标记为不活跃)。");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/class/" + classId + "/students";
    }

    /**
     * 处理学生信息更新请求 (对应编辑模态框的提交)
     */
    @PostMapping("/student/update")
    public String updateStudent(@ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        // 确保学生 ID 存在
        if (student.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失败：缺少学生ID。");
            return "redirect:/class/" + student.getClassId() + "/students";
        }

        try {
            // ❗ 核心逻辑：调用 Service 层更新
            studentService.updateStudent(student);
            redirectAttributes.addFlashAttribute("successMessage", "学生 " + student.getName() + " 信息更新成功!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失败: " + e.getMessage());
        }

        // 重定向回学生管理列表
        return "redirect:/class/" + student.getClassId() + "/students";
    }

    // TODO: 补充 /student/update 逻辑
}