package com.college.dto;

import com.college.model.LeaveRequest;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveRequestDTO {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveRequest.Status status;
    private String leaveType;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long studentId;
    private String studentName;
    private String studentIdNumber;
    private String className;
    private String department;
    private Long handlerId;
    private String handlerName;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String studentAvatar;

    public static LeaveRequestDTO fromLeaveRequest(LeaveRequest leaveRequest) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(leaveRequest.getId());
        dto.setStartDate(leaveRequest.getStartDate());
        dto.setEndDate(leaveRequest.getEndDate());
        dto.setReason(leaveRequest.getReason());
        dto.setStatus(leaveRequest.getStatus());
        dto.setLeaveType(leaveRequest.getLeaveType());
        dto.setRemarks(leaveRequest.getRemarks());
        dto.setCreatedAt(leaveRequest.getCreatedAt());
        dto.setUpdatedAt(leaveRequest.getUpdatedAt());
        if (leaveRequest.getStudent() != null) {
            dto.setStudentId(leaveRequest.getStudent().getId());
            dto.setStudentName(leaveRequest.getStudent().getName());
            dto.setStudentIdNumber(leaveRequest.getStudent().getStudentId());
            // 设置学生头像
            if (leaveRequest.getStudent().getAvatar() != null && !leaveRequest.getStudent().getAvatar().isEmpty()) {
                dto.setStudentAvatar("/avatar/" + leaveRequest.getStudent().getAvatar());
            } else {
                dto.setStudentAvatar("/avatar/default.jpg");
            }
        }
        if (leaveRequest.getHandler() != null) {
            dto.setHandlerId(leaveRequest.getHandler().getId());
            dto.setHandlerName(leaveRequest.getHandler().getName());
        }
        
        dto.setEmergencyContactName(leaveRequest.getEmergencyContactName());
        dto.setEmergencyContactPhone(leaveRequest.getEmergencyContactPhone());
        
        // 设置班级信息
        if (leaveRequest.getStudent() != null) {
            dto.setClassName(leaveRequest.getStudent().getClassName());
        }
        
        return dto;
    }
}