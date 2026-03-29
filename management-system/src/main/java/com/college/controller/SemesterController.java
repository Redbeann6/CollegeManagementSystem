package com.college.controller;

import com.college.dto.Response;
import com.college.model.Semester;
import com.college.service.SemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/semesters")
@CrossOrigin(origins = "*")
public class SemesterController {

    @Autowired
    private SemesterService semesterService;

    @GetMapping
    public ResponseEntity<Response<List<Semester>>> getAllSemesters() {
        List<Semester> semesters = semesterService.getAllSemesters();
        return ResponseEntity.ok(new Response<>(true, "获取学期列表成功", semesters));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<Semester>> getSemesterById(@PathVariable Long id) {
        return semesterService.getSemesterById(id)
                .map(semester -> ResponseEntity.ok(Response.success("获取学期成功", semester)))
                .orElse(ResponseEntity.<Response<Semester>>notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Semester>> createSemester(@RequestBody Semester semester) {
        try {
            if (semesterService.semesterExists(semester.getName())) {
                return ResponseEntity.badRequest().body(Response.error("学期名称已存在", null));
            }
            Semester savedSemester = semesterService.saveSemester(semester);
            return ResponseEntity.ok(Response.success("创建学期成功", savedSemester));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("创建学期失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Semester>> updateSemester(@PathVariable Long id, @RequestBody Semester semester) {
        if (!semesterService.getSemesterById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            semester.setId(id); // 确保ID不变
            if (semesterService.semesterExists(semester.getName())) {
                Semester existing = semesterService.getSemesterById(id).orElse(null);
                if (existing != null && !existing.getName().equals(semester.getName())) {
                    return ResponseEntity.badRequest().body(Response.error("学期名称已存在", null));
                }
            }
            Semester updatedSemester = semesterService.updateSemester(id, semester);
            return ResponseEntity.ok(Response.success("更新学期成功", updatedSemester));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("更新学期失败: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> deleteSemester(@PathVariable Long id) {
        if (semesterService.getSemesterById(id).isPresent()) {
            semesterService.deleteSemester(id);
            return ResponseEntity.ok(Response.<Void>success("删除学期成功", null));
        }
        return ResponseEntity.<Response<Void>>notFound().build();
    }
}
