package com.college.controller;

import com.college.dto.ExamResultDTO;
import com.college.dto.Response;
import com.college.model.ExamResult;
import com.college.model.User;
import com.college.service.ExamResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/exam-results")
@CrossOrigin(origins = "*")
public class ExamResultController {

    @Autowired
    private ExamResultService examResultService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<ExamResultDTO>>> getAllExamResults() {
        List<ExamResult> examResults = examResultService.findAll();
        List<ExamResultDTO> examResultDTOs = examResults.stream()
                .map(examResultService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<ExamResultDTO>>success("获取考试成绩列表成功", examResultDTOs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isStudentOwner(authentication, #id)")
    public ResponseEntity<Response<ExamResultDTO>> getExamResultById(@PathVariable Long id) {
        Optional<ExamResult> examResultOpt = examResultService.findById(id);
        if (examResultOpt.isPresent()) {
            ExamResultDTO examResultDTO = examResultService.convertToDTO(examResultOpt.get());
            return ResponseEntity.ok(Response.success("获取考试成绩成功", examResultDTO));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isStudentUser(authentication, #studentId)")
    public ResponseEntity<Response<List<ExamResultDTO>>> getExamResultsByStudent(@PathVariable Long studentId) {
        List<ExamResult> examResults = examResultService.findByStudentId(studentId);
        List<ExamResultDTO> examResultDTOs = examResults.stream()
                .map(examResultService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<ExamResultDTO>>success("获取学生考试成绩列表成功", examResultDTOs));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<ExamResultDTO>>> getExamResultsByCourse(@PathVariable Long courseId) {
        List<ExamResult> examResults = examResultService.findByCourseId(courseId);
        List<ExamResultDTO> examResultDTOs = examResults.stream()
                .map(examResultService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<ExamResultDTO>>success("获取课程考试成绩列表成功", examResultDTOs));
    }

    @GetMapping("/exam/{examId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<ExamResultDTO>>> getExamResultsByExam(@PathVariable Long examId) {
        List<ExamResult> examResults = examResultService.findByExamId(examId);
        List<ExamResultDTO> examResultDTOs = examResults.stream()
                .map(examResultService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<ExamResultDTO>>success("获取考试成绩列表成功", examResultDTOs));
    }

    @GetMapping("/student/{studentId}/exam/{examId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isStudentUser(authentication, #studentId)")
    public ResponseEntity<Response<ExamResultDTO>> getExamResultByStudentAndExam(@PathVariable Long studentId, @PathVariable Long examId) {
        Optional<ExamResult> examResultOpt = examResultService.findByStudentIdAndExamId(studentId, examId);
        if (examResultOpt.isPresent()) {
            ExamResultDTO examResultDTO = examResultService.convertToDTO(examResultOpt.get());
            return ResponseEntity.ok(Response.<ExamResultDTO>success("获取考试成绩成功", examResultDTO));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<ExamResultDTO>> createExamResult(@RequestBody ExamResultDTO examResultDTO) {
        try {
            // 检查是否已存在该学生该考试的成绩
            if (examResultService.findByStudentIdAndExamId(
                    examResultDTO.getStudentId(), examResultDTO.getExamId()).isPresent()) {
                return ResponseEntity.badRequest().body(Response.<ExamResultDTO>error("该学生已存在该考试的成绩", null));
            }
            
            ExamResult examResult = examResultService.convertToEntity(examResultDTO);
            ExamResult savedExamResult = examResultService.save(examResult);
            ExamResultDTO savedDTO = examResultService.convertToDTO(savedExamResult);
            return ResponseEntity.ok(Response.<ExamResultDTO>success("创建考试成绩成功", savedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<ExamResultDTO>error("创建考试成绩失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<ExamResultDTO>> updateExamResult(@PathVariable Long id, @RequestBody ExamResultDTO examResultDTO) {
        try {
            Optional<ExamResult> examResultOpt = examResultService.findById(id);
            if (examResultOpt.isPresent()) {
                ExamResult examResultToUpdate = examResultService.convertToEntity(examResultDTO);
                examResultToUpdate.setId(id);
                ExamResult updatedExamResult = examResultService.save(examResultToUpdate);
                ExamResultDTO updatedDTO = examResultService.convertToDTO(updatedExamResult);
                return ResponseEntity.ok(Response.<ExamResultDTO>success("更新考试成绩成功", updatedDTO));
            }
            return ResponseEntity.<Response<ExamResultDTO>>notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<ExamResultDTO>error("更新考试成绩失败: " + e.getMessage(), null));
        }
        
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<Void>> deleteExamResult(@PathVariable Long id) {
        if (examResultService.findById(id).isPresent()) {
            examResultService.deleteById(id);
            return ResponseEntity.ok(Response.<Void>success("删除考试成绩成功", null));
        }
        return ResponseEntity.notFound().build();
    }
}