package com.college.controller;

import com.college.dto.ExamResultDTO;
import com.college.dto.Response;
import com.college.model.ExamResult;
import com.college.model.Student;
import com.college.model.Exam;
import com.college.model.Course;
import com.college.repository.StudentRepository;
import com.college.service.ExamResultService;
import com.college.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/grades")
@CrossOrigin(origins = "*")
public class GradeController {

    @Autowired
    private ExamResultService examResultService;

    @Autowired
    private ExamService examService;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<ExamResultDTO>>> getAllGrades() {
        List<ExamResult> examResults = examResultService.findAll();
        List<ExamResultDTO> examResultDTOs = examResults.stream()
                .map(examResultService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<ExamResultDTO>>success("获取成绩列表成功", examResultDTOs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isStudentOwner(authentication, #id)")
    public ResponseEntity<Response<ExamResultDTO>> getGradeById(@PathVariable Long id) {
        Optional<ExamResult> examResultOpt = examResultService.findById(id);
        if (examResultOpt.isPresent()) {
            ExamResultDTO examResultDTO = examResultService.convertToDTO(examResultOpt.get());
            return ResponseEntity.ok(Response.success("获取成绩成功", examResultDTO));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isStudentUser(authentication, #studentId)")
    public ResponseEntity<Response<List<ExamResultDTO>>> getGradesByStudent(@PathVariable Long studentId) {
        List<ExamResult> examResults = examResultService.findByStudentId(studentId);
        List<ExamResultDTO> examResultDTOs = examResults.stream()
                .map(examResultService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<ExamResultDTO>>success("获取学生成绩列表成功", examResultDTOs));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<ExamResultDTO>>> getGradesByCourse(@PathVariable Long courseId) {
        List<ExamResult> examResults = examResultService.findByCourseId(courseId);
        List<ExamResultDTO> examResultDTOs = examResults.stream()
                .map(examResultService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<ExamResultDTO>>success("获取课程成绩列表成功", examResultDTOs));
    }

    @GetMapping("/exam/{examId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<ExamResultDTO>>> getGradesByExam(@PathVariable Long examId) {
        List<ExamResult> examResults = examResultService.findByExamId(examId);
        List<ExamResultDTO> examResultDTOs = examResults.stream()
                .map(examResultService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<ExamResultDTO>>success("获取考试成绩列表成功", examResultDTOs));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<ExamResultDTO>> createGrade(@RequestBody ExamResultDTO examResultDTO, Authentication authentication) {
        try {
            // 检查是否已存在该学生该考试的成绩
            if (examResultService.findByStudentIdAndExamId(
                    examResultDTO.getStudentId(), examResultDTO.getExamId()).isPresent()) {
                return ResponseEntity.badRequest().body(Response.<ExamResultDTO>error("该学生已存在该考试的成绩", null));
            }
            
            ExamResult examResult = examResultService.convertToEntity(examResultDTO);
            // 设置录入人信息
            if (authentication != null && authentication.getName() != null) {
                examResult.setEntryBy(authentication.getName());
            }
            ExamResult savedExamResult = examResultService.save(examResult);
            ExamResultDTO savedDTO = examResultService.convertToDTO(savedExamResult);
            return ResponseEntity.ok(Response.<ExamResultDTO>success("创建成绩成功", savedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<ExamResultDTO>error("创建成绩失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<ExamResultDTO>> updateGrade(@PathVariable Long id, @RequestBody ExamResultDTO examResultDTO, Authentication authentication) {
        try {
            Optional<ExamResult> examResultOpt = examResultService.findById(id);
            if (examResultOpt.isPresent()) {
                ExamResult examResultToUpdate = examResultService.convertToEntity(examResultDTO);
                examResultToUpdate.setId(id);
                // 设置录入人信息
                if (authentication != null && authentication.getName() != null) {
                    examResultToUpdate.setEntryBy(authentication.getName());
                }
                ExamResult updatedExamResult = examResultService.save(examResultToUpdate);
                ExamResultDTO updatedDTO = examResultService.convertToDTO(updatedExamResult);
                return ResponseEntity.ok(Response.<ExamResultDTO>success("更新成绩成功", updatedDTO));
            }
            return ResponseEntity.<Response<ExamResultDTO>>notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<ExamResultDTO>error("更新成绩失败: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<Void>> deleteGrade(@PathVariable Long id) {
        if (examResultService.findById(id).isPresent()) {
            examResultService.deleteById(id);
            return ResponseEntity.ok(Response.<Void>success("删除成绩成功", null));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<ExamResultDTO>>> batchCreateGrades(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            System.out.println("收到批量创建成绩请求");
            // 安全地提取考试ID
            Object examIdObj = request.get("examId");
            Long examId = parseLongValue(examIdObj, "考试ID");
            System.out.println("解析到考试ID: " + examId);
            
            List<Map<String, Object>> students = (List<Map<String, Object>>) request.get("students");
            System.out.println("收到 " + students.size() + " 个学生数据");
            
            // 验证考试是否存在
            Optional<Exam> examOpt = examService.findById(examId);
            if (!examOpt.isPresent()) {
                System.out.println("考试不存在: " + examId);
                return ResponseEntity.badRequest().body(Response.<List<ExamResultDTO>>error("考试不存在", null));
            }
            Exam exam = examOpt.get();
            System.out.println("找到考试: " + exam.getTitle());
            
            // 验证考试是否有课程关联
            if (exam.getCourse() == null) {
                System.out.println("考试未关联课程: " + examId);
                return ResponseEntity.badRequest().body(Response.<List<ExamResultDTO>>error("考试未关联课程", null));
            }
            System.out.println("考试关联课程: " + exam.getCourse().getName());
            
            List<ExamResult> examResults = new ArrayList<>();
            
            // 首先收集所有的学生ID，一次性检查是否有重复
            Set<Long> studentIds = new HashSet<>();
            for (Map<String, Object> studentData : students) {
                Object studentIdObj = studentData.get("studentId");
                Long studentId = parseLongValue(studentIdObj, "学生ID");
                
                // 检查本次批量操作中是否已经有相同学生ID
                if (studentIds.contains(studentId)) {
                    System.out.println("发现重复学生ID: " + studentId);
                    return ResponseEntity.badRequest().body(Response.<List<ExamResultDTO>>error("学生ID " + studentId + " 在本次批量操作中重复", null));
                }
                studentIds.add(studentId);
            }
            System.out.println("验证通过，没有重复学生ID");
            
            for (Map<String, Object> studentData : students) {
                // 安全地提取学生ID
                Object studentIdObj = studentData.get("studentId");
                Long studentId = parseLongValue(studentIdObj, "学生ID");
                
                // 验证学生是否存在（按学号查找）
                Student student = studentRepository.findByStudentId(studentId.toString());
                if (student == null) {
                    System.out.println("学生不存在: 学号 " + studentId);
                    return ResponseEntity.badRequest().body(Response.<List<ExamResultDTO>>error("学号为 " + studentId + " 的学生不存在", null));
                }
                System.out.println("找到学生: " + student.getName() + " (ID: " + student.getId() + ")");
                
                // 检查数据库中是否已存在该学生该考试的成绩
                Optional<ExamResult> existingResult = examResultService.findByStudentIdAndExamId(student.getId(), examId);
                
                if (existingResult.isPresent()) {
                    System.out.println("学生已存在该考试的成绩: " + studentId);
                    return ResponseEntity.badRequest().body(Response.<List<ExamResultDTO>>error("学号为 " + studentId + " 的学生已存在该考试的成绩", null));
                }
                
                ExamResult examResult = new ExamResult();
                
                // 设置完整的关联对象
                examResult.setStudent(student);
                examResult.setExam(exam);
                examResult.setCourse(exam.getCourse());
                
                // 处理分数数据，可能是整数或浮点数
                Object scoreObj = studentData.get("score");
                Double score = parseDoubleValue(scoreObj);
                examResult.setScore(score);
                
                // 根据分数自动计算等级
                if (score != null) {
                    // 使用默认总分100来计算等级
                    examResult.setGrade(calculateGradeFromScore(score, 100.0));
                }
                
                if (studentData.containsKey("remark")) {
                    examResult.setFeedback((String) studentData.get("remark"));
                } else if (studentData.containsKey("feedback")) {
                    examResult.setFeedback((String) studentData.get("feedback"));
                }
                
                // 设置录入人信息
                if (authentication != null && authentication.getName() != null) {
                    examResult.setEntryBy(authentication.getName());
                }
                
                examResults.add(examResult);
            }
            System.out.println("准备保存 " + examResults.size() + " 条成绩记录");
            
            List<ExamResult> savedResults = examResultService.batchSave(examResults);
            System.out.println("成功保存 " + savedResults.size() + " 条成绩记录");
            
            List<ExamResultDTO> savedDTOs = savedResults.stream()
                    .map(examResultService::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Response.<List<ExamResultDTO>>success("批量创建成绩成功", savedDTOs));
        } catch (Exception e) {
            System.err.println("批量创建成绩失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Response.<List<ExamResultDTO>>error("批量创建成绩失败: " + e.getMessage(), null));
        }
    }
    
    // 辅助方法：安全地解析长整型值
    private Long parseLongValue(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        
        if (obj instanceof Integer) {
            return Long.valueOf((Integer) obj);
        } else if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof String) {
            return Long.parseLong((String) obj);
        } else if (obj instanceof Double) {
            return ((Double) obj).longValue();
        }
        
        throw new IllegalArgumentException(fieldName + "格式不正确: " + obj.toString());
    }
    
    // 辅助方法：安全地解析双精度浮点值
    private Double parseDoubleValue(Object obj) {
        if (obj == null) {
            return null;
        }
        
        if (obj instanceof Integer) {
            return ((Integer) obj).doubleValue();
        } else if (obj instanceof Long) {
            return ((Long) obj).doubleValue();
        } else if (obj instanceof Double) {
            return (Double) obj;
        } else if (obj instanceof Float) {
            return ((Float) obj).doubleValue();
        } else if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
    
    // 根据分数和总分计算等级
    private String calculateGradeFromScore(Double score, Double totalMarks) {
        if (score == null) {
            return null; // 如果分数为空，则等级也为null
        }
        
        double actualTotalMarks = (totalMarks == null || totalMarks <= 0) ? 100.0 : totalMarks;
        
        double percentage = (score / actualTotalMarks) * 100;
        
        if (percentage >= 90) {
            return "优秀";
        } else if (percentage >= 80) {
            return "良好";
        } else if (percentage >= 70) {
            return "中等";
        } else if (percentage >= 60) {
            return "及格";
        } else {
            return "不及格";
        }
    }
}