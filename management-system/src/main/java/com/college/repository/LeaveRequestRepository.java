package com.college.repository;

import com.college.model.LeaveRequest;
import com.college.model.Student;
import com.college.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByStudent(Student student);

    List<LeaveRequest> findByHandler(User handler);

    List<LeaveRequest> findByStatus(LeaveRequest.Status status);

    List<LeaveRequest> findByStudentAndStatus(Student student, LeaveRequest.Status status);
    
    @Query("SELECT lr FROM LeaveRequest lr LEFT JOIN FETCH lr.student s LEFT JOIN FETCH s.department LEFT JOIN FETCH lr.handler")
    List<LeaveRequest> findAllWithAssociations();
    
    // 使用原生SQL更新请假申请状态和处理人，绕过JPA的外键约束检查
    @Modifying
    @Transactional
    @Query(value = "UPDATE leave_requests SET status = ?2, handler_id = ?3, remarks = ?4 WHERE id = ?1 AND status = 'PENDING'", nativeQuery = true)
    int updateStatusAndHandlerNative(Long id, String status, Long handlerId, String remarks);
}