package com.college.controller;

import com.college.dto.CourseDTO;
import com.college.dto.Response;
import com.college.model.Course;
import com.college.model.User;
import com.college.service.CourseService;
import com.college.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseService courseService;
    
    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<Response<List<CourseDTO>>> getAllCourses(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) String keyword) {
        
        List<Course> courses;
        if (departmentId != null || teacherId != null || semesterId != null || keyword != null) {
            courses = courseService.findCoursesByFilters(departmentId, teacherId, semesterId, keyword);
        } else {
            courses = courseService.findAll();
        }
        
        List<CourseDTO> courseDTOs = courses.stream()
                .map(course -> {
                    CourseDTO dto = courseService.convertToDTO(course);
                    // 添加已选人数信息
                    try {
                        int enrollmentCount = enrollmentService.countEnrollmentsByCourseId(course.getId());
                        dto.setEnrollmentCount(enrollmentCount);
                    } catch (Exception e) {
                        // 如果获取人数失败，设置默认值为0
                        dto.setEnrollmentCount(0);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(new Response<>(true, "获取课程列表成功", courseDTOs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<CourseDTO>> getCourseById(@PathVariable Long id) {
        return courseService.findById(id)
                .map(course -> {
                    CourseDTO courseDTO = courseService.convertToDTO(course);
                    return ResponseEntity.ok(Response.<CourseDTO>success("获取课程成功", courseDTO));
                })
                .orElse(ResponseEntity.<Response<CourseDTO>>notFound().build());
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<Response<List<CourseDTO>>> getCoursesByTeacher(@PathVariable Long teacherId) {
        List<Course> courses = courseService.findByTeacherId(teacherId);
        List<CourseDTO> courseDTOs = courses.stream()
                .map(courseService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new Response<>(true, "获取教师课程列表成功", courseDTOs));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Response<List<CourseDTO>>> getCoursesByStudent(@PathVariable Long studentId) {
        List<Course> courses = courseService.findByStudentId(studentId);
        List<CourseDTO> courseDTOs = courses.stream()
                .map(courseService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new Response<>(true, "获取学生课程列表成功", courseDTOs));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<CourseDTO>> createCourse(@RequestBody CourseDTO courseDTO) {
        try {
            // 检查课程代码是否已存在
            if (courseService.existsByCourseCode(courseDTO.getCourseCode())) {
                return ResponseEntity.badRequest().body(Response.<CourseDTO>error("课程代码已存在", null));
            }
            Course course = courseService.convertToEntity(courseDTO);
            Course savedCourse = courseService.save(course);
            CourseDTO savedDTO = courseService.convertToDTO(savedCourse);
            return ResponseEntity.ok(Response.<CourseDTO>success("创建课程成功", savedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<CourseDTO>error("创建课程失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<CourseDTO>> updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        return courseService.findById(id)
                .map(existingCourse -> {
                    Course courseToUpdate = courseService.convertToEntity(courseDTO);
                    courseToUpdate.setId(id); // 确保ID不变
                    
                    // 如果要更新课程代码，检查新代码是否已被其他课程使用
                    if (!existingCourse.getCourseCode().equals(courseDTO.getCourseCode()) && 
                            courseService.existsByCourseCode(courseDTO.getCourseCode())) {
                        return ResponseEntity.badRequest().body(Response.<CourseDTO>error("课程代码已存在", null));
                    }
                    
                    Course updatedCourse = courseService.save(courseToUpdate);
                    CourseDTO updatedDTO = courseService.convertToDTO(updatedCourse);
                    return ResponseEntity.ok(Response.<CourseDTO>success("更新课程成功", updatedDTO));
                })
                .orElse(ResponseEntity.<Response<CourseDTO>>notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> deleteCourse(@PathVariable Long id) {
        if (courseService.findById(id).isPresent()) {
            courseService.deleteById(id);
            return ResponseEntity.ok(Response.<Void>success("删除课程成功", null));
        }
        return ResponseEntity.<Response<Void>>notFound().build();
    }
    
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> batchDeleteCourses(@RequestBody List<Long> courseIds) {
        try {
            for (Long id : courseIds) {
                courseService.deleteById(id);
            }
            return ResponseEntity.ok(Response.<Void>success("批量删除课程成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<Void>error("批量删除课程失败: " + e.getMessage(), null));
        }
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<CourseDTO>> updateCourseStatus(@PathVariable Long id, @RequestBody CourseStatusUpdateRequest request) {
        return courseService.findById(id)
                .map(course -> {
                    course.setStatus(request.getStatus());
                    Course updatedCourse = courseService.save(course);
                    CourseDTO updatedDTO = courseService.convertToDTO(updatedCourse);
                    return ResponseEntity.ok(Response.<CourseDTO>success("更新课程状态成功", updatedDTO));
                })
                .orElse(ResponseEntity.<Response<CourseDTO>>notFound().build());
    }
    
    // 为CourseManagement.vue添加获取课程学生列表的API
    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<Object>>> getCourseStudents(@PathVariable Long id) {
        // 使用EnrollmentService来获取课程的学生列表
        try {
            // 这里需要通过Enrollment实体来获取选了这门课的学生
            List<Object> students = courseService.getCourseStudents(id);
            return ResponseEntity.ok(Response.<List<Object>>success("获取课程学生列表成功", students));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<List<Object>>error("获取课程学生列表失败: " + e.getMessage(), null));
        }
    }
    
    // 创建一个内部类用于处理状态更新请求
    public static class CourseStatusUpdateRequest {
        private Boolean status;
        
        public Boolean getStatus() {
            return status;
        }
        
        public void setStatus(Boolean status) {
            this.status = status;
        }
    }
    
    // 获取特定课程的注册人数
    @GetMapping("/{id}/enrollment-count")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'TEACHER')")
    public ResponseEntity<Response<Integer>> getCourseEnrollmentCount(@PathVariable Long id) {
        try {
            int enrollmentCount = enrollmentService.countEnrollmentsByCourseId(id);
            return ResponseEntity.ok(Response.<Integer>success("获取课程注册人数成功", enrollmentCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<Integer>error("获取课程注册人数失败: " + e.getMessage(), 0));
        }
    }
}