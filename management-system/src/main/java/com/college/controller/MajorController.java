package com.college.controller;

import com.college.dto.MajorDTO;
import com.college.dto.Response;
import com.college.model.Major;
import com.college.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/majors")
@CrossOrigin(origins = "*")
public class MajorController {

    @Autowired
    private MajorService majorService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Response<List<MajorDTO>>> getAllMajors() {
        List<Major> majors = majorService.findAll();
        List<MajorDTO> majorDTOs = majors.stream()
                .map(majorService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取专业列表成功", majorDTOs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Response<MajorDTO>> getMajorById(@PathVariable Long id) {
        Major major = majorService.findById(id)
                .orElseThrow(() -> new RuntimeException("专业不存在"));
        MajorDTO majorDTO = majorService.convertToDTO(major);
        return ResponseEntity.ok(Response.success("获取专业成功", majorDTO));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Response<List<MajorDTO>>> getMajorsByDepartment(@PathVariable Long departmentId) {
        List<Major> majors = majorService.findByDepartmentId(departmentId);
        List<MajorDTO> majorDTOs = majors.stream()
                .map(majorService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.success("获取专业列表成功", majorDTOs));
    }
}