package com.college.service;

import com.college.model.Course;
import com.college.dto.CourseDTO;
import java.util.List;
import java.util.Optional;

public interface CourseService {
    Course save(Course course);
    Optional<Course> findById(Long id);
    List<Course> findAll();
    void deleteById(Long id);
    List<Course> findByTeacherId(Long teacherId);
    List<Course> findByStudentId(Long studentId);
    List<Course> findByDayOfWeek(String dayOfWeek);
    boolean existsByCourseCode(String courseCode);
    List<Object> getCourseStudents(Long courseId);
    // 统计相关方法
    long countAllCourses();
    long countCoursesInPreviousMonth();
    long countCoursesInCurrentMonth();
    CourseDTO convertToDTO(Course course);
    Course convertToEntity(CourseDTO courseDTO);
    
    // 支持筛选的方法
    List<Course> findCoursesByFilters(Long departmentId, Long teacherId, Long semesterId, String keyword);
    
    // 检查课程是否由当前用户教授
    boolean isCourseTaughtByCurrentUser(Long courseId, Object principal);
}