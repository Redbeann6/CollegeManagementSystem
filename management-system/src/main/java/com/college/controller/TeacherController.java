package com.college.controller;

import com.college.dto.Response;
import com.college.dto.TeacherDTO;
import com.college.dto.CourseDTO;
import com.college.model.Department;
import com.college.model.Teacher;
import com.college.model.Course;
import com.college.repository.DepartmentRepository;
import com.college.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.college.service.CourseService;

@RestController
@RequestMapping("/teachers")
@CrossOrigin(origins = "*")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private com.college.service.SecurityService securityService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<List<TeacherDTO>>> getAllTeachers() {
        List<TeacherDTO> teachers = teacherService.findAll().stream()
                .map(teacherService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取教师列表成功", teachers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(authentication, #id)")
    public ResponseEntity<Response<TeacherDTO>> getTeacherById(@PathVariable Long id) {
        Optional<Teacher> teacherOpt = teacherService.findById(id);
        if (!teacherOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Response.error("教师不存在", null));
        }
        TeacherDTO teacherDTO = teacherService.convertToDTO(teacherOpt.get());
        return ResponseEntity.ok(Response.success("获取教师成功", teacherDTO));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<TeacherDTO>> createTeacher(@RequestBody TeacherDTO teacherDTO) {
        try {
            Teacher teacher = teacherService.convertToEntity(teacherDTO);
            // 在创建时设置默认密码（实际应用中应使用安全的默认密码或强制用户设置密码）
            teacher.setPassword("$2a$10$9S.vUvGbWbWHY3fRDqdj1u9qY9FXknsEKyORUrcVNlwGg6129/v1q"); // 默认密码：123456
            Teacher savedTeacher = teacherService.save(teacher);
            TeacherDTO savedTeacherDTO = teacherService.convertToDTO(savedTeacher);
            return ResponseEntity.ok(Response.success("添加教师成功", savedTeacherDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("添加教师失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<TeacherDTO>> updateTeacher(@PathVariable Long id, @RequestBody TeacherDTO teacherDTO) {
        try {
            Optional<Teacher> existingTeacherOpt = teacherService.findById(id);
            if (!existingTeacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师不存在", null));
            }

            // 获取现有的教师信息
            Teacher existingTeacher = existingTeacherOpt.get();
            
            // 只更新提供的字段，保留原有密码
            updateTeacherFromDTO(existingTeacher, teacherDTO);
            Teacher updatedTeacher = teacherService.save(existingTeacher);
            TeacherDTO updatedTeacherDTO = teacherService.convertToDTO(updatedTeacher);
            return ResponseEntity.ok(Response.success("更新教师成功", updatedTeacherDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("更新教师失败: " + e.getMessage(), null));
        }
    }

    // 辅助方法：根据DTO更新教师对象（不包括密码）
    private void updateTeacherFromDTO(Teacher teacher, TeacherDTO teacherDTO) {
        teacher.setTeacherId(teacherDTO.getTeacherId());
        teacher.setUsername(teacherDTO.getUsername());
        teacher.setName(teacherDTO.getName());
        teacher.setEmail(teacherDTO.getEmail());
        teacher.setPhone(teacherDTO.getPhone());
        teacher.setEnabled(teacherDTO.getEnabled() != null ? teacherDTO.getEnabled() : true);
        teacher.setGender(teacherDTO.getGender());
        teacher.setBirthDate(teacherDTO.getBirthDate());
        teacher.setJoinDate(teacherDTO.getJoinDate());
        teacher.setTitle(teacherDTO.getTitle());
        teacher.setEducation(teacherDTO.getEducation());
        teacher.setGraduateSchool(teacherDTO.getGraduateSchool());
        teacher.setMajor(teacherDTO.getMajor());
        teacher.setBiography(teacherDTO.getBiography());
        teacher.setOffice(teacherDTO.getOffice());
        teacher.setOfficePhone(teacherDTO.getOfficePhone());
        teacher.setMobilePhone(teacherDTO.getMobilePhone());
        teacher.setAddress(teacherDTO.getAddress());

        // 设置部门信息
        if (teacherDTO.getDepartmentId() != null) {
            Optional<Department> departmentOpt = departmentRepository.findById(teacherDTO.getDepartmentId());
            if (departmentOpt.isPresent()) {
                teacher.setDepartment(departmentOpt.get());
            } else {
                teacher.setDepartment(null); // 或者抛出异常
            }
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<String>> deleteTeacher(@PathVariable Long id) {
        try {
            Optional<Teacher> teacherOpt = teacherService.findById(id);
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师不存在", null));
            }

            teacherService.deleteById(id);
            return ResponseEntity.ok(Response.success("删除教师成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("删除教师失败: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<String>> deleteTeachersBatch(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                teacherService.deleteById(id);
            }
            return ResponseEntity.ok(Response.success("批量删除教师成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("批量删除教师失败: " + e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<TeacherDTO>> updateTeacherStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        try {
            Optional<Teacher> teacherOpt = teacherService.findById(id);
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师不存在", null));
            }

            Teacher teacher = teacherOpt.get();
            teacher.setEnabled(request.isEnabled());
            Teacher updatedTeacher = teacherService.save(teacher);
            TeacherDTO updatedTeacherDTO = teacherService.convertToDTO(updatedTeacher);
            return ResponseEntity.ok(Response.success("更新教师状态成功", updatedTeacherDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("更新教师状态失败: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}/courses")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isCurrentUser(authentication, #id)")
    public ResponseEntity<Response<List<CourseDTO>>> getCoursesByTeacherId(@PathVariable Long id) {
        try {
            List<Course> courses = courseService.findByTeacherId(id);
            List<CourseDTO> courseDTOs = courses.stream()
                    .map(courseService::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Response.success("获取教师课程成功", courseDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取教师课程失败: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Response<List<TeacherDTO>>> getTeachersByDepartment(@PathVariable Long departmentId) {
        List<Teacher> teachers = teacherService.findByDepartmentId(departmentId);
        List<TeacherDTO> teacherDTOs = teachers.stream()
                .map(teacherService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取系部教师成功", teacherDTOs));
    }
    
    @GetMapping("/courses")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<CourseDTO>>> getTeacherCoursesWithFilters(
            Authentication authentication,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) String keyword) {
        
        // 如果没有指定teacherId，默认使用当前登录教师的ID
        if (teacherId == null) {
            // 从Security上下文中获取当前教师ID
            teacherId = getCurrentTeacherId(authentication);
        }
        
        List<Course> courses;
        if (departmentId != null || semesterId != null || keyword != null) {
            // 使用筛选条件查询，但限制在当前教师的课程范围内
            courses = courseService.findCoursesByFilters(departmentId, teacherId, semesterId, keyword);
        } else {
            courses = courseService.findByTeacherId(teacherId);
        }
        
        List<CourseDTO> courseDTOs = courses.stream()
                .map(courseService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取教师课程成功", courseDTOs));
    }
    
    // 获取当前登录教师ID的辅助方法
    private Long getCurrentTeacherId(org.springframework.security.core.Authentication authentication) {
        if (authentication != null) {
            return securityService.getCurrentUserId(authentication);
        }
        return null; // 如果无法获取当前用户ID，则返回null
    }

    // 用于更新状态的内部请求类
    public static class StatusUpdateRequest {
        private Boolean enabled;

        public Boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }
}