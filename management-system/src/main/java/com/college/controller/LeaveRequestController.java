package com.college.controller;

import com.college.dto.LeaveRequestDTO;
import com.college.dto.Response;
import com.college.model.LeaveRequest;
import com.college.model.Teacher;
import com.college.model.User;
import com.college.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/leave-requests")
@CrossOrigin(origins = "*")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<LeaveRequestDTO>>> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = leaveRequestService.findAll();
        List<LeaveRequestDTO> leaveRequestDTOs = leaveRequests.stream()
                .map(leaveRequestService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<LeaveRequestDTO>>success("获取请假申请列表成功", leaveRequestDTOs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isStudentOwner(authentication, #id)")
    public ResponseEntity<Response<LeaveRequestDTO>> getLeaveRequestById(@PathVariable Long id) {
        return leaveRequestService.findById(id)
                .map(leaveRequest -> {
                    LeaveRequestDTO leaveRequestDTO = leaveRequestService.convertToDTO(leaveRequest);
                    return ResponseEntity.ok(Response.<LeaveRequestDTO>success("获取请假申请成功", leaveRequestDTO));
                })
                .orElse(ResponseEntity.<Response<LeaveRequestDTO>>notFound().build());
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isStudentUser(authentication, #studentId)")
    public ResponseEntity<Response<List<LeaveRequestDTO>>> getLeaveRequestsByStudent(@PathVariable Long studentId) {
        List<LeaveRequest> leaveRequests = leaveRequestService.findByStudentId(studentId);
        List<LeaveRequestDTO> leaveRequestDTOs = leaveRequests.stream()
                .map(leaveRequestService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<LeaveRequestDTO>>success("获取学生请假申请列表成功", leaveRequestDTOs));
    }

    @GetMapping("/handler/{handlerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<LeaveRequestDTO>>> getLeaveRequestsByHandler(@PathVariable Long handlerId) {
        List<LeaveRequest> leaveRequests = leaveRequestService.findByHandlerId(handlerId);
        List<LeaveRequestDTO> leaveRequestDTOs = leaveRequests.stream()
                .map(leaveRequestService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<LeaveRequestDTO>>success("获取待处理请假申请列表成功", leaveRequestDTOs));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<LeaveRequestDTO>>> getLeaveRequestsByStatus(@PathVariable String status) {
        List<LeaveRequest> leaveRequests = leaveRequestService.findByStatus(LeaveRequest.Status.valueOf(status));
        List<LeaveRequestDTO> leaveRequestDTOs = leaveRequests.stream()
                .map(leaveRequestService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<LeaveRequestDTO>>success("获取指定状态请假申请列表成功", leaveRequestDTOs));
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<LeaveRequestDTO>> createLeaveRequest(
            @RequestBody LeaveRequestDTO leaveRequestDTO, 
            @AuthenticationPrincipal User currentUser) {
        try {
            // 设置学生ID，让service层处理实体转换
            if (currentUser != null) {
                leaveRequestDTO.setStudentId(currentUser.getId());
            }
            LeaveRequest leaveRequest = leaveRequestService.convertToEntity(leaveRequestDTO);
            // 设置初始状态为PENDING
            leaveRequest.setStatus(LeaveRequest.Status.PENDING);
            
            LeaveRequest savedLeaveRequest = leaveRequestService.save(leaveRequest);
            LeaveRequestDTO savedDTO = leaveRequestService.convertToDTO(savedLeaveRequest);
            return ResponseEntity.ok(Response.<LeaveRequestDTO>success("创建请假申请成功", savedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<LeaveRequestDTO>error("创建请假申请失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @securityService.isStudentOwner(authentication, #id)")
    public ResponseEntity<Response<LeaveRequestDTO>> updateLeaveRequest(
            @PathVariable Long id, 
            @RequestBody LeaveRequestDTO leaveRequestDTO,
            @AuthenticationPrincipal User currentUser) {
        return leaveRequestService.findById(id)
                .map(existingLeaveRequest -> {
                    // 只有管理员、教师或请假申请人才能更新
                    if (currentUser.getRole().equals("STUDENT") && 
                            !existingLeaveRequest.getStudent().getId().equals(currentUser.getId())) {
                        return ResponseEntity.badRequest().body(Response.<LeaveRequestDTO>error("无权更新该请假申请", null));
                    }
                    
                    // 已处理的请假申请不允许更新
                    if (existingLeaveRequest.getStatus().name().equals("APPROVED") || 
                            existingLeaveRequest.getStatus().name().equals("REJECTED")) {
                        return ResponseEntity.badRequest().body(Response.<LeaveRequestDTO>error("已处理的请假申请不允许更新", null));
                    }
                    
                    LeaveRequest leaveRequestToUpdate = leaveRequestService.convertToEntity(leaveRequestDTO);
                    leaveRequestToUpdate.setId(id); // 确保ID不变
                    leaveRequestToUpdate.setStudent(existingLeaveRequest.getStudent()); // 保持申请人不变
                    
                    LeaveRequest updatedLeaveRequest = leaveRequestService.save(leaveRequestToUpdate);
                    LeaveRequestDTO updatedDTO = leaveRequestService.convertToDTO(updatedLeaveRequest);
                    return ResponseEntity.ok(Response.<LeaveRequestDTO>success("更新请假申请成功", updatedDTO));
                })
                .orElse(ResponseEntity.<Response<LeaveRequestDTO>>notFound().build());
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<LeaveRequestDTO>> processLeaveRequest(
            @PathVariable Long id, 
            @RequestParam String status,
            @AuthenticationPrincipal User currentUser) {
        return leaveRequestService.findById(id)
                .map(existingLeaveRequest -> {
                    // 只能处理待处理状态的请假申请
                    if (!existingLeaveRequest.getStatus().name().equals("PENDING")) {
                        return ResponseEntity.badRequest().body(Response.<LeaveRequestDTO>error("只能处理待处理状态的请假申请", null));
                    }
                    
                    // 设置处理状态和处理人
                    existingLeaveRequest.setStatus(LeaveRequest.Status.valueOf(status.toUpperCase()));
                    // 将User转换为Teacher类型（假设currentUser是Teacher实例）
                    if (currentUser instanceof Teacher) {
                        existingLeaveRequest.setHandler((Teacher) currentUser);
                    } else {
                        return ResponseEntity.badRequest().body(Response.<LeaveRequestDTO>error("只有教师可以处理请假申请", null));
                    }
                    
                    LeaveRequest processedLeaveRequest = leaveRequestService.save(existingLeaveRequest);
                    LeaveRequestDTO processedDTO = leaveRequestService.convertToDTO(processedLeaveRequest);
                    return ResponseEntity.ok(Response.<LeaveRequestDTO>success("处理请假申请成功", processedDTO));
                })
                .orElse(ResponseEntity.<Response<LeaveRequestDTO>>notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isStudentOwner(authentication, #id)")
    public ResponseEntity<Response<Void>> deleteLeaveRequest(@PathVariable Long id) {
        return leaveRequestService.findById(id)
                .map(leaveRequest -> {
                    leaveRequestService.deleteById(id);
                    return ResponseEntity.ok(Response.<Void>success("删除请假申请成功", null));
                })
                .orElseGet(() -> ResponseEntity.<Response<Void>>status(HttpStatus.NOT_FOUND)
                        .body(Response.<Void>error("请假申请不存在", null)));
    }
}