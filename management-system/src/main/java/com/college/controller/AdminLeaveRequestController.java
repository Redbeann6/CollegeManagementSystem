package com.college.controller;

import com.college.dto.LeaveRequestDTO;
import com.college.dto.Response;
import com.college.model.LeaveRequest;
import com.college.model.User;
import com.college.repository.TeacherRepository;
import com.college.repository.UserRepository;
import com.college.repository.LeaveRequestRepository;
import com.college.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/leave-requests")
@CrossOrigin(origins = "*")
public class AdminLeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<List<LeaveRequestDTO>>> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = leaveRequestService.findAll();
        System.out.println("[DEBUG] Found " + leaveRequests.size() + " leave requests");
        List<LeaveRequestDTO> leaveRequestDTOs = leaveRequests.stream()
                .map(leaveRequest -> {
                    LeaveRequestDTO dto = leaveRequestService.convertToDTO(leaveRequest);
                    System.out.println("[DEBUG] DTO status: " + dto.getStatus() + ", handlerName: " + dto.getHandlerName());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(Response.<List<LeaveRequestDTO>>success("获取请假申请列表成功", leaveRequestDTOs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<LeaveRequestDTO>> getLeaveRequestById(@PathVariable Long id) {
        return leaveRequestService.findById(id)
                .map(leaveRequest -> {
                    LeaveRequestDTO leaveRequestDTO = leaveRequestService.convertToDTO(leaveRequest);
                    return ResponseEntity.ok(Response.<LeaveRequestDTO>success("获取请假申请成功", leaveRequestDTO));
                })
                .orElse(ResponseEntity.<Response<LeaveRequestDTO>>notFound().build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<LeaveRequestDTO>> updateLeaveRequestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String remarks) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(500)
                        .body(Response.<LeaveRequestDTO>error("无法获取当前用户信息", null));
            }
            
            User currentUser = null;
            if (authentication.getPrincipal() instanceof User) {
                currentUser = (User) authentication.getPrincipal();
            } else {
                // 如果principal不是User类型，尝试获取用户名
                String username = authentication.getName();
                Optional<User> userOptional = userRepository.findById(1L); // 直接使用ID为1的用户作为处理人
                if (userOptional.isPresent()) {
                    currentUser = userOptional.get();
                }
            }
            
            if (currentUser == null) {
                return ResponseEntity.status(500)
                        .body(Response.<LeaveRequestDTO>error("无法获取当前用户信息", null));
            }

            // 获取请假申请
            Optional<LeaveRequest> leaveRequestOpt = leaveRequestRepository.findById(id);
            if (!leaveRequestOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            LeaveRequest leaveRequest = leaveRequestOpt.get();
            
            // 只能处理待处理状态的请假申请
            if (!leaveRequest.getStatus().name().equals("PENDING")) {
                return ResponseEntity.badRequest()
                        .body(Response.<LeaveRequestDTO>error("只能处理待处理状态的请假申请", null));
            }

            // 更新状态和处理人
            leaveRequest.setStatus(LeaveRequest.Status.valueOf(status.toUpperCase()));
            leaveRequest.setHandler(currentUser);
            if (remarks != null) {
                leaveRequest.setRemarks(remarks);
            }

            // 保存更新
            LeaveRequest updatedLeaveRequest = leaveRequestRepository.save(leaveRequest);
            
            // 创建一个简单的DTO，避免使用复杂的convertToDTO方法
            LeaveRequestDTO updatedDTO = new LeaveRequestDTO();
            updatedDTO.setId(updatedLeaveRequest.getId());
            updatedDTO.setStartDate(updatedLeaveRequest.getStartDate());
            updatedDTO.setEndDate(updatedLeaveRequest.getEndDate());
            updatedDTO.setReason(updatedLeaveRequest.getReason());
            updatedDTO.setStatus(updatedLeaveRequest.getStatus());
            updatedDTO.setLeaveType(updatedLeaveRequest.getLeaveType());
            updatedDTO.setRemarks(updatedLeaveRequest.getRemarks());
            updatedDTO.setCreatedAt(updatedLeaveRequest.getCreatedAt());
            updatedDTO.setUpdatedAt(updatedLeaveRequest.getUpdatedAt());
            
            // 设置学生信息（仅基本字段）
            if (updatedLeaveRequest.getStudent() != null) {
                updatedDTO.setStudentId(updatedLeaveRequest.getStudent().getId());
                updatedDTO.setStudentName(updatedLeaveRequest.getStudent().getName());
                updatedDTO.setStudentIdNumber(updatedLeaveRequest.getStudent().getStudentId());
            }
            
            // 设置处理人信息
            updatedDTO.setHandlerId(currentUser.getId());
            updatedDTO.setHandlerName(currentUser.getName());
            
            // 设置紧急联系人信息
            updatedDTO.setEmergencyContactName(updatedLeaveRequest.getEmergencyContactName());
            updatedDTO.setEmergencyContactPhone(updatedLeaveRequest.getEmergencyContactPhone());
            
            return ResponseEntity.ok(Response.<LeaveRequestDTO>success("更新请假申请状态成功", updatedDTO));
        } catch (Exception e) {
            System.err.println("Error updating leave request status: " + e.getMessage());
            e.printStackTrace(System.err);
            return ResponseEntity.status(500)
                    .body(Response.<LeaveRequestDTO>error("更新请假申请状态失败: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> deleteLeaveRequest(@PathVariable Long id) {
        return leaveRequestService.findById(id)
                .map(leaveRequest -> {
                    leaveRequestService.deleteById(id);
                    return ResponseEntity.ok(Response.<Void>success("删除请假申请成功", null));
                })
                .orElse(ResponseEntity.<Response<Void>>notFound().build());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<LeaveRequestDTO>> updateLeaveRequest(
            @PathVariable Long id,
            @RequestBody LeaveRequestDTO leaveRequestDTO) {
        return leaveRequestService.findById(id)
                .map(existingLeaveRequest -> {
                    // 更新可编辑的字段
                    existingLeaveRequest.setLeaveType(leaveRequestDTO.getLeaveType());
                    existingLeaveRequest.setStartDate(leaveRequestDTO.getStartDate());
                    existingLeaveRequest.setEndDate(leaveRequestDTO.getEndDate());
                    existingLeaveRequest.setReason(leaveRequestDTO.getReason());
                    
                    // 保存更新
                    LeaveRequest updatedLeaveRequest = leaveRequestService.save(existingLeaveRequest);
                    LeaveRequestDTO updatedDTO = leaveRequestService.convertToDTO(updatedLeaveRequest);
                    
                    return ResponseEntity.ok(Response.<LeaveRequestDTO>success("更新请假申请成功", updatedDTO));
                })
                .orElse(ResponseEntity.<Response<LeaveRequestDTO>>notFound().build());
    }
}