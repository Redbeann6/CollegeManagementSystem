package com.college.controller;

import com.college.dto.ExamDTO;
import com.college.dto.Response;
import com.college.model.Exam;
import com.college.model.Teacher;
import com.college.model.User;
import com.college.service.ExamService;
import com.college.service.TeacherService;
import com.college.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teacher/exams")
@CrossOrigin(origins = "*")
public class TeacherExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    // 获取当前教师的考试列表
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<ExamDTO>>> getTeacherExams(Authentication authentication) {
        try {
            // 从认证信息中获取当前用户的用户名
            String username = authentication.getName();

            // 通过用户名获取用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }

            // 获取教师信息
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }

            Teacher teacher = teacherOpt.get();

            // 获取该教师创建的考试
            List<Exam> exams = examService.findByCreatorId(teacher.getId());
            List<ExamDTO> examDTOs = exams.stream()
                    .map(examService::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Response.success("获取教师考试列表成功", examDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取教师考试列表失败: " + e.getMessage(), null));
        }
    }

    // 获取特定考试信息
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<ExamDTO>> getExamById(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }

            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }

            Teacher teacher = teacherOpt.get();

            // 获取考试信息
            Optional<Exam> examOpt = examService.findById(id);
            if (!examOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("考试不存在", null));
            }

            Exam exam = examOpt.get();

            // 验证考试是否属于当前教师
            if (exam.getCreator() == null || !exam.getCreator().getId().equals(teacher.getId())) {
                return ResponseEntity.status(403).body(Response.error("无权访问该考试", null));
            }

            ExamDTO examDTO = examService.convertToDTO(exam);
            return ResponseEntity.ok(Response.success("获取考试成功", examDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取考试失败: " + e.getMessage(), null));
        }
    }

    // 创建考试
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<ExamDTO>> createExam(@RequestBody ExamDTO examDTO, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }

            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }

            Teacher teacher = teacherOpt.get();

            // 设置创建者ID
            examDTO.setCreatorId(teacher.getId());

            // 验证课程是否属于当前教师
            if (examDTO.getCourseId() != null) {
                // 这里可以添加额外的验证逻辑
            }

            Exam exam = examService.convertToEntity(examDTO);
            Exam savedExam = examService.save(exam);
            ExamDTO savedDTO = examService.convertToDTO(savedExam);

            return ResponseEntity.ok(Response.success("创建考试成功", savedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("创建考试失败: " + e.getMessage(), null));
        }
    }

    // 更新考试
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<ExamDTO>> updateExam(@PathVariable Long id, @RequestBody ExamDTO examDTO, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }

            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }

            Teacher teacher = teacherOpt.get();

            // 验证考试是否属于当前教师
            Optional<Exam> existingExamOpt = examService.findById(id);
            if (!existingExamOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("考试不存在", null));
            }

            Exam existingExam = existingExamOpt.get();
            if (existingExam.getCreator() == null || !existingExam.getCreator().getId().equals(teacher.getId())) {
                return ResponseEntity.status(403).body(Response.error("无权修改该考试", null));
            }

            // 设置ID和创建者ID
            examDTO.setId(id);
            examDTO.setCreatorId(teacher.getId());

            Exam exam = examService.convertToEntity(examDTO);
            Exam updatedExam = examService.save(exam);
            ExamDTO updatedDTO = examService.convertToDTO(updatedExam);

            return ResponseEntity.ok(Response.success("更新考试成功", updatedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("更新考试失败: " + e.getMessage(), null));
        }
    }

    // 删除考试
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<Void>> deleteExam(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }

            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }

            Teacher teacher = teacherOpt.get();

            // 验证考试是否属于当前教师
            Optional<Exam> existingExamOpt = examService.findById(id);
            if (!existingExamOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("考试不存在", null));
            }

            Exam existingExam = existingExamOpt.get();
            if (existingExam.getCreator() == null || !existingExam.getCreator().getId().equals(teacher.getId())) {
                return ResponseEntity.status(403).body(Response.error("无权删除该考试", null));
            }

            examService.deleteById(id);

            return ResponseEntity.ok(Response.success("删除考试成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("删除考试失败: " + e.getMessage(), null));
        }
    }
}