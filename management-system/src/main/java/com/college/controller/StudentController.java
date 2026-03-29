package com.college.controller;

import com.college.dto.Response;
import com.college.dto.StudentDTO;
import com.college.model.Student;
import com.college.model.User;
import com.college.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<StudentDTO>>> getAllStudents() {
        List<StudentDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(Response.success("获取学生列表成功", students));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isCurrentUser(authentication, #id)")
    public ResponseEntity<Response<StudentDTO>> getStudentById(@PathVariable Long id) {
        StudentDTO student = studentService.getStudentById(id);
        if (student == null) {
            return ResponseEntity.badRequest().body(Response.error("学生不存在", null));
        }
        return ResponseEntity.ok(Response.success("获取学生成功", student));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<StudentDTO>> createStudent(@RequestBody StudentDTO studentDTO) {
        try {
            // 调用服务层创建学生
            StudentDTO createdStudent = studentService.createStudent(studentDTO);
            return ResponseEntity.ok(Response.success("创建学生成功", createdStudent));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("创建学生失败: " + e.getMessage(), null));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<StudentDTO>> updateStudent(@PathVariable Long id, @RequestBody StudentDTO studentDTO) {
        try {
            // 调用服务层更新学生
            StudentDTO updatedStudent = studentService.updateStudent(id, studentDTO);
            return ResponseEntity.ok(Response.success("更新学生成功", updatedStudent));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("更新学生失败: " + e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteById(id);
            return ResponseEntity.ok(Response.<Void>success("删除学生成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<Void>error("删除学生失败: " + e.getMessage(), null));
        }
    }
    
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> deleteStudents(@RequestBody List<Long> ids) {
        try {
            studentService.deleteByIds(ids);
            return ResponseEntity.ok(Response.<Void>success("批量删除学生成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<Void>error("批量删除学生失败: " + e.getMessage(), null));
        }
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<StudentDTO>> updateStudentStatus(@PathVariable Long id, @RequestBody UserStatusUpdateRequest statusRequest) {
        try {
            StudentDTO updatedStudent = studentService.updateStudentStatus(id, statusRequest.isEnabled());
            return ResponseEntity.ok(Response.success("更新学生状态成功", updatedStudent));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("更新学生状态失败: " + e.getMessage(), null));
        }
    }
}

// 添加UserStatusUpdateRequest类
class UserStatusUpdateRequest {
    private Boolean enabled;

    public Boolean isEnabled() {
        return enabled != null ? enabled : false;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}