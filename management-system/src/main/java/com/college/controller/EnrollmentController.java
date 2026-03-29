package com.college.controller;

import com.college.dto.EnrollmentDTO;
import com.college.dto.Response;
import com.college.model.Enrollment;
import com.college.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    // 获取所有选课记录（管理员和教师）
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<EnrollmentDTO>>> getAllEnrollments(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId) {
        List<Enrollment> enrollments;
        
        // 根据参数筛选
        if (semesterId != null && courseId != null && studentId != null) {
            enrollments = enrollmentService.findBySemesterIdAndCourseIdAndStudentId(semesterId, courseId, studentId);
        } else if (semesterId != null && courseId != null) {
            enrollments = enrollmentService.findBySemesterIdAndCourseId(semesterId, courseId);
        } else if (semesterId != null && studentId != null) {
            enrollments = enrollmentService.findBySemesterIdAndStudentId(semesterId, studentId);
        } else if (courseId != null && studentId != null) {
            enrollments = enrollmentService.findByCourseIdAndStudentId(courseId, studentId);
        } else if (semesterId != null) {
            enrollments = enrollmentService.findBySemesterId(semesterId);
        } else if (courseId != null) {
            enrollments = enrollmentService.findByCourseId(courseId);
        } else if (studentId != null) {
            enrollments = enrollmentService.findByStudentId(studentId);
        } else {
            enrollments = enrollmentService.findAll();
        }
        
        List<EnrollmentDTO> enrollmentDTOs = enrollments.stream()
                .map(enrollmentService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<EnrollmentDTO>>success("获取选课记录列表成功", enrollmentDTOs));
    }

    // 根据ID获取选课记录
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isEnrollmentOwner(authentication, #id)")
    public ResponseEntity<Response<EnrollmentDTO>> getEnrollmentById(@PathVariable Long id) {
        return enrollmentService.findById(id)
                .map(enrollment -> {
                    EnrollmentDTO enrollmentDTO = enrollmentService.convertToDTO(enrollment);
                    return ResponseEntity.ok(Response.<EnrollmentDTO>success("获取选课记录成功", enrollmentDTO));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 根据学生ID获取选课记录
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isStudentUser(authentication, #studentId)")
    public ResponseEntity<Response<List<EnrollmentDTO>>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentService.findByStudentId(studentId);
        List<EnrollmentDTO> enrollmentDTOs = enrollments.stream()
                .map(enrollmentService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<EnrollmentDTO>>success("获取学生选课记录成功", enrollmentDTOs));
    }

    // 根据课程ID获取选课记录
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isCourseTeacher(authentication, #courseId)")
    public ResponseEntity<Response<List<EnrollmentDTO>>> getEnrollmentsByCourse(@PathVariable Long courseId) {
        List<Enrollment> enrollments = enrollmentService.findByCourseId(courseId);
        List<EnrollmentDTO> enrollmentDTOs = enrollments.stream()
                .map(enrollmentService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<EnrollmentDTO>>success("获取课程选课记录成功", enrollmentDTOs));
    }

    // 根据学期获取选课记录
    @GetMapping("/semester/{semester}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<EnrollmentDTO>>> getEnrollmentsBySemester(@PathVariable String semester) {
        List<Enrollment> enrollments = enrollmentService.findBySemester(semester);
        List<EnrollmentDTO> enrollmentDTOs = enrollments.stream()
                .map(enrollmentService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<EnrollmentDTO>>success("获取学期选课记录成功", enrollmentDTOs));
    }

    // 根据学生ID和课程ID获取选课记录
    @GetMapping("/student/{studentId}/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isStudentUser(authentication, #studentId)")
    public ResponseEntity<Response<EnrollmentDTO>> getEnrollmentByStudentAndCourse(
            @PathVariable Long studentId, @PathVariable Long courseId) {
        List<Enrollment> enrollments = enrollmentService.findByStudentIdAndCourseId(studentId, courseId);
        if (!enrollments.isEmpty()) {
            EnrollmentDTO enrollmentDTO = enrollmentService.convertToDTO(enrollments.get(0));
            return ResponseEntity.ok(Response.<EnrollmentDTO>success("获取选课记录成功", enrollmentDTO));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 学生选课
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Response<EnrollmentDTO>> enrollCourse(@RequestBody EnrollmentDTO enrollmentDTO) {
        try {
            // 检查是否已选该课程
            if (enrollmentService.existsByStudentIdAndCourseId(
                    enrollmentDTO.getStudentId(), enrollmentDTO.getCourseId())) {
                return ResponseEntity.badRequest().body(Response.<EnrollmentDTO>error("已选择该课程", null));
            }
            
            // 检查课程容量
            if (!enrollmentService.isCourseAvailable(enrollmentDTO.getCourseId())) {
                return ResponseEntity.badRequest().body(Response.<EnrollmentDTO>error("课程已满", null));
            }
            
            Enrollment enrollment = enrollmentService.convertToEntity(enrollmentDTO);
            Enrollment savedEnrollment = enrollmentService.save(enrollment);
            EnrollmentDTO savedDTO = enrollmentService.convertToDTO(savedEnrollment);
            return ResponseEntity.ok(Response.<EnrollmentDTO>success("选课成功", savedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<EnrollmentDTO>error("选课失败: " + e.getMessage(), null));
        }
    }

    // 更新选课记录（主要用于更新成绩和状态）
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<EnrollmentDTO>> updateEnrollment(
            @PathVariable Long id, @RequestBody EnrollmentDTO enrollmentDTO) {
        return enrollmentService.findById(id)
                .map(existingEnrollment -> {
                    Enrollment enrollmentToUpdate = enrollmentService.convertToEntity(enrollmentDTO);
                    enrollmentToUpdate.setId(id); // 确保ID不变
                    
                    Enrollment updatedEnrollment = enrollmentService.save(enrollmentToUpdate);
                    EnrollmentDTO updatedDTO = enrollmentService.convertToDTO(updatedEnrollment);
                    return ResponseEntity.ok(Response.<EnrollmentDTO>success("更新选课记录成功", updatedDTO));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 更新选课成绩
    @PutMapping("/{id}/score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isCourseTeacher(authentication, @enrollmentService.getCourseIdByEnrollmentId(#id))")
    public ResponseEntity<Response<EnrollmentDTO>> updateScore(
            @PathVariable Long id, @RequestBody ScoreUpdateRequest scoreRequest) {
        return enrollmentService.findById(id)
                .map(enrollment -> {
                    enrollment.setScore(scoreRequest.getScore());
                    Enrollment updatedEnrollment = enrollmentService.save(enrollment);
                    EnrollmentDTO updatedDTO = enrollmentService.convertToDTO(updatedEnrollment);
                    return ResponseEntity.ok(Response.<EnrollmentDTO>success("更新成绩成功", updatedDTO));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 学生退课
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isEnrollmentOwner(authentication, #id)")
    public ResponseEntity<Response<Void>> dropCourse(@PathVariable Long id) {
        if (enrollmentService.findById(id).isPresent()) {
            enrollmentService.deleteById(id);
            return ResponseEntity.ok(Response.<Void>success("退课成功", null));
        }
        return ResponseEntity.notFound().build();
    }

    // 批量选课
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<EnrollmentDTO>>> batchEnroll(
            @RequestBody List<EnrollmentDTO> enrollmentDTOs) {
        try {
            List<Enrollment> enrollments = enrollmentDTOs.stream()
                    .map(enrollmentService::convertToEntity)
                    .collect(Collectors.toList());
            
            List<Enrollment> savedEnrollments = enrollmentService.batchSave(enrollments);
            List<EnrollmentDTO> savedDTOs = savedEnrollments.stream()
                    .map(enrollmentService::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Response.<List<EnrollmentDTO>>success("批量选课成功", savedDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<List<EnrollmentDTO>>error("批量选课失败: " + e.getMessage(), null));
        }
    }

    // 批量退课
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<Void>> batchDrop(@RequestBody List<Long> ids) {
        try {
            enrollmentService.batchDelete(ids);
            return ResponseEntity.ok(Response.<Void>success("批量退课成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<Void>error("批量退课失败: " + e.getMessage(), null));
        }
    }

    // 批量导出选课数据
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportEnrollments(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId) {
        try {
            // 根据参数筛选数据
            List<Enrollment> enrollments;
            if (semesterId != null && courseId != null && studentId != null) {
                enrollments = enrollmentService.findBySemesterIdAndCourseIdAndStudentId(semesterId, courseId, studentId);
            } else if (semesterId != null && courseId != null) {
                enrollments = enrollmentService.findBySemesterIdAndCourseId(semesterId, courseId);
            } else if (semesterId != null && studentId != null) {
                enrollments = enrollmentService.findBySemesterIdAndStudentId(semesterId, studentId);
            } else if (courseId != null && studentId != null) {
                enrollments = enrollmentService.findByCourseIdAndStudentId(courseId, studentId);
            } else if (semesterId != null) {
                enrollments = enrollmentService.findBySemesterId(semesterId);
            } else if (courseId != null) {
                enrollments = enrollmentService.findByCourseId(courseId);
            } else if (studentId != null) {
                enrollments = enrollmentService.findByStudentId(studentId);
            } else {
                enrollments = enrollmentService.findAll();
            }
            
            // 转换为DTO列表
            List<EnrollmentDTO> enrollmentDTOs = enrollments.stream()
                    .map(enrollmentService::convertToDTO)
                    .collect(Collectors.toList());
            
            // 这里需要实现Excel导出逻辑
            byte[] excelBytes = exportToExcel(enrollmentDTOs);
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=enrollments.xlsx")
                    .body(excelBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 导出到Excel的辅助方法
    private byte[] exportToExcel(List<EnrollmentDTO> enrollmentDTOs) {
        // 这里应该实现真正的Excel导出逻辑
        // 为了简单起见，我们返回空数组
        // 实际项目中应该使用Apache POI等库来生成Excel文件
        return new byte[0];
    }
    
    // 成绩更新请求内部类
    static class ScoreUpdateRequest {
        private Double score;
        
        public Double getScore() {
            return score;
        }
        
        public void setScore(Double score) {
            this.score = score;
        }
    }
}