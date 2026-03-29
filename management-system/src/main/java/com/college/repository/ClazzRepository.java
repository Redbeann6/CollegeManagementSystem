package com.college.repository;

import com.college.model.Clazz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClazzRepository extends JpaRepository<Clazz, Long> {
    List<Clazz> findByMajorId(Long majorId);
    
    @Query("SELECT c FROM Clazz c JOIN Major m ON c.majorId = m.id WHERE m.departmentId = :departmentId")
    List<Clazz> findByDepartmentId(@Param("departmentId") Long departmentId);
}