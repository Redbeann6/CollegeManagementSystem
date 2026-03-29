package com.college.service;

import com.college.model.LeaveRequest;
import com.college.dto.LeaveRequestDTO;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestService {
    LeaveRequest save(LeaveRequest leaveRequest);
    Optional<LeaveRequest> findById(Long id);
    List<LeaveRequest> findAll();
    void deleteById(Long id);
    List<LeaveRequest> findByStudentId(Long studentId);
    List<LeaveRequest> findByHandlerId(Long handlerId);
    List<LeaveRequest> findByStatus(LeaveRequest.Status status);
    List<LeaveRequest> findByStudentIdAndStatus(Long studentId, LeaveRequest.Status status);
    
    // 统计方法
    long countPendingByStudentId(Long studentId);
    LeaveRequestDTO convertToDTO(LeaveRequest leaveRequest);
    LeaveRequest convertToEntity(LeaveRequestDTO leaveRequestDTO);
}