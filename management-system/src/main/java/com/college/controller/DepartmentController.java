package com.college.controller;

import com.college.dto.Response;
import com.college.dto.TeacherDTO;
import com.college.model.Department;
import com.college.model.Teacher;
import com.college.service.DepartmentService;
import com.college.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private TeacherService teacherService;

    @GetMapping
    public ResponseEntity<Response<List<Department>>> getAllDepartments() {
        List<Department> departments = departmentService.findAll();
        return ResponseEntity.ok(new Response<>(true, "获取部门列表成功", departments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<Department>> getDepartmentById(@PathVariable Long id) {
        return departmentService.findById(id)
                .map(department -> ResponseEntity.ok(Response.success("获取部门成功", department)))
                .orElse(ResponseEntity.<Response<Department>>notFound().build());
    }
    
    @GetMapping("/search/{name}")
    public ResponseEntity<Response<List<Department>>> getDepartmentsByNameContaining(@PathVariable String name) {
        List<Department> departments = departmentService.findByNameContaining(name);
        return ResponseEntity.ok(new Response<>(true, "搜索部门成功", departments));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Department>> createDepartment(@RequestBody Map<String, Object> departmentData) {
        try {
            Department department = new Department();
            updateDepartmentFromMap(department, departmentData);
            Department savedDepartment = departmentService.save(department);
            return ResponseEntity.ok(Response.success("创建部门成功", savedDepartment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("创建部门失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Department>> updateDepartment(@PathVariable Long id, @RequestBody Map<String, Object> departmentData) {
        return departmentService.findById(id)
                .map(existingDepartment -> {
                    // 更新现有部门的信息
                    updateDepartmentFromMap(existingDepartment, departmentData);
                    existingDepartment.setId(id); // 确保ID不变
                    Department updatedDepartment = departmentService.save(existingDepartment);
                    return ResponseEntity.ok(Response.success("更新部门成功", updatedDepartment));
                })
                .orElse(ResponseEntity.<Response<Department>>notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> deleteDepartment(@PathVariable Long id) {
        if (departmentService.findById(id).isPresent()) {
            departmentService.deleteById(id);
            return ResponseEntity.ok(Response.<Void>success("删除部门成功", null));
        }
        return ResponseEntity.<Response<Void>>notFound().build();
    }
    
    private void updateDepartmentFromMap(Department department, Map<String, Object> departmentData) {
        if (departmentData.containsKey("name")) {
            department.setName((String) departmentData.get("name"));
        }
        if (departmentData.containsKey("abbreviation")) {
            department.setAbbreviation((String) departmentData.get("abbreviation"));
        }
        if (departmentData.containsKey("code")) {
            department.setCode((String) departmentData.get("code"));
        }
        if (departmentData.containsKey("dean")) {
            department.setDean((String) departmentData.get("dean"));
        }
        if (departmentData.containsKey("deputyDean")) {
            department.setDeputyDean((String) departmentData.get("deputyDean"));
        }
        if (departmentData.containsKey("description")) {
            department.setDescription((String) departmentData.get("description"));
        }
        if (departmentData.containsKey("email")) {
            department.setEmail((String) departmentData.get("email"));
        }
        if (departmentData.containsKey("establishDate")) {
            Object dateValue = departmentData.get("establishDate");
            if (dateValue != null) {
                if (dateValue instanceof String) {
                    String dateString = (String) dateValue;
                    // 处理可能的不同日期格式
                    if (dateString != null && !dateString.isEmpty()) {
                        try {
                            // 尝试解析日期字符串
                            department.setEstablishDate(java.time.LocalDate.parse(dateString));
                        } catch (Exception e) {
                            // 如果解析失败，可以尝试其他格式或记录错误
                            System.err.println("日期解析失败: " + dateString + ", 错误: " + e.getMessage());
                            // 可以选择保留原始值或设置为null
                            department.setEstablishDate(null);
                        }
                    }
                } else if (dateValue instanceof java.time.LocalDate) {
                    department.setEstablishDate((java.time.LocalDate) dateValue);
                } else if (dateValue instanceof java.util.Date) {
                    // 如果是java.util.Date类型，转换为LocalDate
                    department.setEstablishDate(new java.sql.Date(((java.util.Date) dateValue).getTime()).toLocalDate());
                } else if (dateValue instanceof Long) {
                    // 如果是时间戳，转换为LocalDate
                    java.util.Date date = new java.util.Date((Long) dateValue);
                    department.setEstablishDate(new java.sql.Date(date.getTime()).toLocalDate());
                }
            } else {
                department.setEstablishDate(null);
            }
        }
        if (departmentData.containsKey("history")) {
            department.setHistory((String) departmentData.get("history"));
        }
        if (departmentData.containsKey("location")) {
            department.setLocation((String) departmentData.get("location"));
        }
        if (departmentData.containsKey("phone")) {
            department.setPhone((String) departmentData.get("phone"));
        }
        if (departmentData.containsKey("status")) {
            department.setStatus((Boolean) departmentData.get("status"));
        }
        if (departmentData.containsKey("isActive")) {
            department.setIsActive((Boolean) departmentData.get("isActive"));
        }
        if (departmentData.containsKey("studentCount")) {
            Object countValue = departmentData.get("studentCount");
            if (countValue != null) {
                if (countValue instanceof Integer) {
                    department.setStudentCount((Integer) countValue);
                } else if (countValue instanceof Double) {
                    department.setStudentCount(((Double) countValue).intValue());
                }
            }
        }
        if (departmentData.containsKey("teacherCount")) {
            Object countValue = departmentData.get("teacherCount");
            if (countValue != null) {
                if (countValue instanceof Integer) {
                    department.setTeacherCount((Integer) countValue);
                } else if (countValue instanceof Double) {
                    department.setTeacherCount(((Double) countValue).intValue());
                }
            }
        }
    }
    
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<String>> deleteDepartmentsBatch(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                departmentService.deleteById(id);
            }
            return ResponseEntity.ok(Response.success("批量删除部门成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("批量删除部门失败: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/{id}/teachers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Response<List<TeacherDTO>>> getTeachersByDepartment(@PathVariable Long id) {
        try {
            // 检查部门是否存在
            Optional<Department> departmentOpt = departmentService.findById(id);
            if (!departmentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取该部门的教师列表
            List<Teacher> teachers = teacherService.findByDepartmentId(id);
            List<TeacherDTO> teacherDTOs = teachers.stream()
                    .map(teacherService::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Response.success("获取部门教师列表成功", teacherDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取部门教师列表失败: " + e.getMessage(), null));
        }
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        try {
            Optional<Department> departmentOpt = departmentService.findById(id);
            if (!departmentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Department department = departmentOpt.get();
            department.setStatus(request.getStatus());
            departmentService.save(department);
            
            return ResponseEntity.ok(Response.<Void>success("状态更新成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("状态更新失败: " + e.getMessage(), null));
        }
    }
    
    public static class StatusUpdateRequest {
        private Boolean status;
        
        public Boolean getStatus() {
            return status;
        }
        
        public void setStatus(Boolean status) {
            this.status = status;
        }
    }
}