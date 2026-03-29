package com.college.service.impl;

import com.college.model.LeaveRequest;
import com.college.model.Student;
import com.college.model.Teacher;
import com.college.model.User;
import com.college.repository.LeaveRequestRepository;
import com.college.repository.StudentRepository;
import com.college.repository.UserRepository;
import com.college.dto.LeaveRequestDTO;
import com.college.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public LeaveRequest save(LeaveRequest leaveRequest) {
        // 确保学生实体不会被意外更新
        if (leaveRequest.getStudent() != null && leaveRequest.getStudent().getId() != null) {
            Optional<Student> studentOpt = studentRepository.findById(leaveRequest.getStudent().getId());
            studentOpt.ifPresent(leaveRequest::setStudent);
        }
        // 确保处理人实体不会被意外更新
        if (leaveRequest.getHandler() != null && leaveRequest.getHandler().getId() != null) {
            Optional<User> userOpt = userRepository.findById(leaveRequest.getHandler().getId());
            userOpt.ifPresent(leaveRequest::setHandler);
        }
        return leaveRequestRepository.save(leaveRequest);
    }

    @Override
    public Optional<LeaveRequest> findById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    @Override
    public List<LeaveRequest> findAll() {
        return leaveRequestRepository.findAllWithAssociations();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        leaveRequestRepository.deleteById(id);
    }

    @Override
    public List<LeaveRequest> findByStudentId(Long studentId) {
        // 先根据ID获取学生实体，再使用Repository的findByStudent方法
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            // 使用JPQL查询以确保关联实体被加载
            return leaveRequestRepository.findAllWithAssociations().stream()
                    .filter(lr -> lr.getStudent() != null && lr.getStudent().getId().equals(studentId))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<LeaveRequest> findByHandlerId(Long handlerId) {
        // 使用findAll并过滤，替代不存在的findByHandlerId方法
        return leaveRequestRepository.findAll().stream()
                .filter(leaveRequest -> leaveRequest.getHandler() != null && leaveRequest.getHandler().getId().equals(handlerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequest> findByStatus(LeaveRequest.Status status) {
        return leaveRequestRepository.findByStatus(status);
    }

    @Override
    public List<LeaveRequest> findByStudentIdAndStatus(Long studentId, LeaveRequest.Status status) {
        // 先根据ID获取学生实体，再使用Repository的findByStudentAndStatus方法
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            // 使用JPQL查询以确保关联实体被加载
            return leaveRequestRepository.findAllWithAssociations().stream()
                    .filter(lr -> lr.getStudent() != null && lr.getStudent().getId().equals(studentId) && lr.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public LeaveRequestDTO convertToDTO(LeaveRequest leaveRequest) {
        if (leaveRequest == null) {
            return null;
        }

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

        // 设置学生信息
        if (leaveRequest.getStudent() != null) {
            dto.setStudentId(leaveRequest.getStudent().getId());
            dto.setStudentName(leaveRequest.getStudent().getName());
            dto.setStudentIdNumber(leaveRequest.getStudent().getStudentId());
            
            // 设置班级和系部信息
            dto.setClassName(leaveRequest.getStudent().getClassName());
            if (leaveRequest.getStudent().getDepartment() != null) {
                dto.setDepartment(leaveRequest.getStudent().getDepartment().getName());
            }
        }

        // 设置处理人信息
        if (leaveRequest.getHandler() != null) {
            System.out.println("[DEBUG] Handler found: " + leaveRequest.getHandler().getName() + " (ID: " + leaveRequest.getHandler().getId() + ")");
            dto.setHandlerId(leaveRequest.getHandler().getId());
            dto.setHandlerName(leaveRequest.getHandler().getName());
        } else {
            System.out.println("[DEBUG] No handler found for leave request " + leaveRequest.getId());
        }
        
        // 设置紧急联系人信息
        dto.setEmergencyContactName(leaveRequest.getEmergencyContactName());
        dto.setEmergencyContactPhone(leaveRequest.getEmergencyContactPhone());

        System.out.println("[DEBUG] DTO status: " + dto.getStatus() + ", handlerName: " + dto.getHandlerName() + ", handlerId: " + dto.getHandlerId());
        return dto;
    }

    @Override
    public LeaveRequest convertToEntity(LeaveRequestDTO leaveRequestDTO) {
        if (leaveRequestDTO == null) {
            return null;
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(leaveRequestDTO.getId());
        leaveRequest.setStartDate(leaveRequestDTO.getStartDate());
        leaveRequest.setEndDate(leaveRequestDTO.getEndDate());
        leaveRequest.setReason(leaveRequestDTO.getReason());
        leaveRequest.setLeaveType(leaveRequestDTO.getLeaveType());
        leaveRequest.setStatus(leaveRequestDTO.getStatus() != null ? leaveRequestDTO.getStatus() : LeaveRequest.Status.PENDING);
        leaveRequest.setRemarks(leaveRequestDTO.getRemarks());
        leaveRequest.setCreatedAt(leaveRequestDTO.getCreatedAt());
        leaveRequest.setUpdatedAt(leaveRequestDTO.getUpdatedAt());

        // 设置学生
        if (leaveRequestDTO.getStudentId() != null) {
            Optional<Student> studentOpt = studentRepository.findById(leaveRequestDTO.getStudentId());
            studentOpt.ifPresent(leaveRequest::setStudent);
        }

        // 设置处理人
        if (leaveRequestDTO.getHandlerId() != null) {
            Optional<User> userOpt = userRepository.findById(leaveRequestDTO.getHandlerId());
            if (userOpt.isPresent()) {
                // 直接设置，不使用方法引用
                User handler = userOpt.get();
                leaveRequest.setHandler(handler);
            }
        }
        
        // 设置紧急联系人信息
        leaveRequest.setEmergencyContactName(leaveRequestDTO.getEmergencyContactName());
        leaveRequest.setEmergencyContactPhone(leaveRequestDTO.getEmergencyContactPhone());

        return leaveRequest;
    }
    
    @Override
    public long countPendingByStudentId(Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            List<LeaveRequest> pendingRequests = leaveRequestRepository.findByStudentAndStatus(studentOpt.get(), LeaveRequest.Status.PENDING);
            return pendingRequests.size();
        } else {
            return 0;
        }
    }
}