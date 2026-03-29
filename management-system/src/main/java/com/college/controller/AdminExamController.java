package com.college.controller;

import com.college.dto.ExamDTO;
import com.college.dto.Response;
import com.college.model.Exam;
import com.college.model.User;
import com.college.model.Teacher;
import com.college.service.ExamService;
import com.college.repository.TeacherRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/exams")
@CrossOrigin(origins = "*")
public class AdminExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private TeacherRepository teacherRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<List<ExamDTO>>> getAllExams() {
        // 在获取考试列表前更新所有考试的状态
        examService.updateAllExamStatuses();
        
        List<Exam> exams = examService.findAll();
        List<ExamDTO> examDTOs = exams.stream()
                .map(exam -> {
                    ExamDTO dto = examService.convertToDTO(exam);
                    // 设置正确的参考人数（选课学生总数）
                    int enrolledCount = examService.getTotalEnrolledStudentsCount(exam.getId());
                    dto.setStudentCount(enrolledCount);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取考试列表成功", examDTOs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<ExamDTO>> getExamById(@PathVariable Long id) {
        // 在获取单个考试前更新所有考试的状态
        examService.updateAllExamStatuses();
        
        return examService.findById(id)
                .map(exam -> {
                    ExamDTO examDTO = examService.convertToDTO(exam);
                    // 设置正确的参考人数（选课学生总数）
                    int enrolledCount = examService.getTotalEnrolledStudentsCount(exam.getId());
                    examDTO.setStudentCount(enrolledCount);
                    return ResponseEntity.ok(Response.success("获取考试成功", examDTO));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<ExamDTO>> createExam(
            @RequestBody ExamDTO examDTO, 
            @AuthenticationPrincipal User currentUser) {
        try {
            // 验证必要字段
            if (examDTO.getCourseId() == null || examDTO.getCourseId() <= 0) {
                return ResponseEntity.badRequest().body(Response.error("必须选择一个有效的课程"));
            }
            
            String title = examDTO.getName() != null ? examDTO.getName() : examDTO.getTitle();
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Response.error("考试标题不能为空"));
            }
            
            // 设置创建者ID
            if (currentUser != null) {
                if (examDTO.getCreatorId() == null || examDTO.getCreatorId() <= 0) {
                    // 如果前端没有指定创建者，或创建者ID无效，使用当前用户相关信息
                    Optional<Teacher> teacherOpt = teacherRepository.findByUsername(currentUser.getUsername());
                    if (teacherOpt.isPresent()) {
                        examDTO.setCreatorId(teacherOpt.get().getId());
                    } else {
                        // 对于管理员，如果找不到对应的教师记录，可以不设置creatorId或使用其他逻辑
                        // 但我们仍需要确保creatorId被设置，所以这里使用传入的teacherId作为creatorId
                        if (examDTO.getTeacherId() != null && examDTO.getTeacherId() > 0) {
                            examDTO.setCreatorId(examDTO.getTeacherId());
                        } else {
                            return ResponseEntity.badRequest().body(Response.error("必须指定有效的教师"));
                        }
                    }
                }
                // 如果前端指定了有效的creatorId，则保留该ID（允许管理员为其他教师创建考试）
            }
            
            Exam exam = examService.convertToEntity(examDTO);
            Exam savedExam = examService.save(exam);
            ExamDTO savedDTO = examService.convertToDTO(savedExam);
            return ResponseEntity.ok(Response.success("创建考试成功", savedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("创建考试失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<ExamDTO>> updateExam(@PathVariable Long id, @RequestBody ExamDTO examDTO, @AuthenticationPrincipal User currentUser) {
        Optional<Exam> existingExamOpt = examService.findById(id);
        if (existingExamOpt.isPresent()) {
            // 验证必要字段
            if (examDTO.getCourseId() == null || examDTO.getCourseId() <= 0) {
                return ResponseEntity.badRequest().body(Response.error("必须选择一个有效的课程"));
            }
            
            String title = examDTO.getName() != null ? examDTO.getName() : examDTO.getTitle();
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Response.error("考试标题不能为空"));
            }
            
            // 设置创建者ID
            if (currentUser != null) {
                if (examDTO.getCreatorId() == null || examDTO.getCreatorId() <= 0) {
                    // 如果前端没有指定创建者，或创建者ID无效，使用当前用户相关信息
                    Optional<Teacher> teacherOpt = teacherRepository.findByUsername(currentUser.getUsername());
                    if (teacherOpt.isPresent()) {
                        examDTO.setCreatorId(teacherOpt.get().getId());
                    } else {
                        return ResponseEntity.badRequest().body(Response.error("当前用户不是教师，无法更新考试"));
                    }
                }
                // 如果前端指定了有效的creatorId，则保留该ID（允许管理员为其他教师创建考试）
            }
            
            Exam examToUpdate = examService.convertToEntity(examDTO);
            examToUpdate.setId(id); // 确保ID不变
            
            Exam updatedExam = examService.save(examToUpdate);
            
            // 重新从数据库获取完整的考试信息以确保关联关系正确加载
            Optional<Exam> savedExamOpt = examService.findById(updatedExam.getId());
            if (savedExamOpt.isPresent()) {
                ExamDTO updatedDTO = examService.convertToDTO(savedExamOpt.get());
                return ResponseEntity.ok(Response.success("更新考试成功", updatedDTO));
            } else {
                return ResponseEntity.badRequest().body(Response.error("更新后的考试未找到"));
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> deleteExam(@PathVariable Long id) {
        if (examService.findById(id).isPresent()) {
            examService.deleteById(id);
            return ResponseEntity.ok(Response.success("删除考试成功", null));
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> batchDeleteExams(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> rawIds = (List<Object>) request.get("examIds");
            List<Long> ids = rawIds.stream()
                    .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
                    .collect(Collectors.toList());
            for (Long id : ids) {
                examService.deleteById(id);
            }
            return ResponseEntity.ok(Response.success("批量删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("批量删除失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<List<ExamDTO>>> getExamsByCourse(@PathVariable Long courseId) {
        List<Exam> exams = examService.findByCourseId(courseId);
        List<ExamDTO> examDTOs = exams.stream()
                .map(exam -> {
                    ExamDTO dto = examService.convertToDTO(exam);
                    // 设置正确的参考人数（选课学生总数）
                    int enrolledCount = examService.getTotalEnrolledStudentsCount(exam.getId());
                    dto.setStudentCount(enrolledCount);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取课程考试列表成功", examDTOs));
    }
    
    @GetMapping("/creator/{creatorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<List<ExamDTO>>> getExamsByCreator(@PathVariable Long creatorId) {
        List<Exam> exams = examService.findByCreatorId(creatorId);
        List<ExamDTO> examDTOs = exams.stream()
                .map(exam -> {
                    ExamDTO dto = examService.convertToDTO(exam);
                    // 设置正确的参考人数（选课学生总数）
                    int enrolledCount = examService.getTotalEnrolledStudentsCount(exam.getId());
                    dto.setStudentCount(enrolledCount);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取创建者考试列表成功", examDTOs));
    }
    
    @GetMapping("/time-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<List<ExamDTO>>> getExamsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Exam> exams = examService.findByExamTimeBetween(start, end);
        List<ExamDTO> examDTOs = exams.stream()
                .map(examService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取时间范围内考试列表成功", examDTOs));
    }
    
    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<List<Map<String, Object>>>> getExamStudents(@PathVariable Long id) {
        try {
            List<Map<String, Object>> students = examService.getExamStudents(id);
            return ResponseEntity.ok(Response.success("获取考试学生列表成功", students));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取考试学生列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Map<String, Object>>> getExamStats(@PathVariable Long id) {
        try {
            Map<String, Object> stats = examService.getExamStatistics(id);
            return ResponseEntity.ok(Response.success("获取考试统计信息成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取考试统计信息失败: " + e.getMessage()));
        }
    }
}