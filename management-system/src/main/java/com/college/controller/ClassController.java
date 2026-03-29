package com.college.controller;

import com.college.dto.ClazzDTO;
import com.college.dto.Response;
import com.college.model.Clazz;
import com.college.service.ClazzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/classes")
@CrossOrigin(origins = "*")
public class ClassController {

    @Autowired
    private ClazzService clazzService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Response<List<ClazzDTO>>> getAllClasses() {
        List<Clazz> classes = clazzService.findAll();
        List<ClazzDTO> clazzDTOs = classes.stream()
                .map(clazzService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取班级列表成功", clazzDTOs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Response<ClazzDTO>> getClazzById(@PathVariable Long id) {
        Clazz clazz = clazzService.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        ClazzDTO clazzDTO = clazzService.convertToDTO(clazz);
        return ResponseEntity.ok(Response.success("获取班级成功", clazzDTO));
    }

    @GetMapping("/major/{majorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Response<List<ClazzDTO>>> getClassesByMajor(@PathVariable Long majorId) {
        List<Clazz> classes = clazzService.findByMajorId(majorId);
        List<ClazzDTO> clazzDTOs = classes.stream()
                .map(clazzService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取班级列表成功", clazzDTOs));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Response<List<ClazzDTO>>> getClassesByDepartment(@PathVariable Long departmentId) {
        List<Clazz> classes = clazzService.findByDepartmentId(departmentId);
        List<ClazzDTO> clazzDTOs = classes.stream()
                .map(clazzService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取班级列表成功", clazzDTOs));
    }
}