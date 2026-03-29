package com.college.service.impl;

import com.college.model.Exam;
import com.college.model.User;
import com.college.model.Course;
import com.college.model.Teacher;
import com.college.model.Student;
import com.college.model.ExamResult;
import com.college.model.Enrollment;
import com.college.repository.ExamRepository;
import com.college.repository.UserRepository;
import com.college.repository.CourseRepository;
import com.college.repository.TeacherRepository;
import com.college.repository.ExamResultRepository;
import com.college.repository.EnrollmentRepository;
import com.college.dto.ExamDTO;
import com.college.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ExamResultRepository examResultRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public Exam save(Exam exam) {
        return examRepository.save(exam);
    }

    @Override
    public Optional<Exam> findById(Long id) {
        return examRepository.findById(id);
    }

    @Override
    public List<Exam> findAll() {
        return examRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        examRepository.deleteById(id);
    }

    @Override
    public List<Exam> findByCourseId(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            return examRepository.findByCourse(courseOpt.get());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Exam> findByCreatorId(Long creatorId) {
        Optional<Teacher> teacherOpt = teacherRepository.findById(creatorId);
        if (teacherOpt.isPresent()) {
            return examRepository.findByCreator(teacherOpt.get());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Exam> findByExamTimeBetween(LocalDateTime start, LocalDateTime end) {
        return examRepository.findByExamTimeBetween(start, end);
    }

    @Override
    public ExamDTO convertToDTO(Exam exam) {
        return ExamDTO.fromExam(exam);
    }

    @Override
    public Exam convertToEntity(ExamDTO examDTO) {
        if (examDTO == null) {
            return null;
        }

        Exam exam = new Exam();
        exam.setId(examDTO.getId());
        // 使用name作为title，如果title为空的话
        String title = examDTO.getName() != null ? examDTO.getName() : examDTO.getTitle();
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("考试标题不能为空");
        }
        exam.setTitle(title);
        
        // 使用location作为classroom，如果classroom为空的话
        exam.setClassroom(examDTO.getLocation() != null ? examDTO.getLocation() : examDTO.getClassroom());
        exam.setDescription(examDTO.getDescription());
        
        // 设置考试类型
        if (examDTO.getExamType() != null && !examDTO.getExamType().trim().isEmpty()) {
            exam.setExamType(examDTO.getExamType());
        }
        
        // 设置考试状态，如果前端提供了状态则使用前端的状态，否则根据时间动态计算状态
        if (examDTO.getStatus() != null && !examDTO.getStatus().trim().isEmpty()) {
            exam.setStatus(examDTO.getStatus());
        } else {
            // 根据考试时间动态计算状态
            java.time.LocalDateTime currentTime = java.time.LocalDateTime.now(java.time.ZoneId.systemDefault());
            java.time.LocalDateTime examEndTime = exam.getExamTime();
            
            if (exam.getDuration() != null) {
                examEndTime = exam.getExamTime().plusMinutes(exam.getDuration());
            }
            
            if (currentTime.isBefore(exam.getExamTime())) {
                // 当前时间早于考试开始时间
                exam.setStatus("NOT_STARTED");
            } else if (currentTime.isBefore(examEndTime)) {
                // 当前时间在考试开始和结束之间
                exam.setStatus("IN_PROGRESS");
            } else {
                // 当前时间晚于考试结束时间
                exam.setStatus("COMPLETED");
            }
        }
        
        // 设置时间字段 - 优先使用startTime
        if (examDTO.getStartTime() != null) {
            exam.setExamTime(examDTO.getStartTime());
            exam.setStartTime(examDTO.getStartTime()); // 同时设置数据库中的startTime字段
        } else if (examDTO.getExamTime() != null) {
            exam.setExamTime(examDTO.getExamTime());
        }
        
        // 如果提供了duration，使用它
        if (examDTO.getDuration() != null) {
            exam.setDuration(examDTO.getDuration());
        }
        
        // 如果提供了endTime，可以根据它设置数据库中的endTime字段并计算duration
        if (examDTO.getEndTime() != null) {
            exam.setEndTime(examDTO.getEndTime());
            if (exam.getExamTime() != null) {
                long durationInMinutes = java.time.Duration.between(exam.getExamTime(), examDTO.getEndTime()).toMinutes();
                if (durationInMinutes > 0) {
                    exam.setDuration((int) durationInMinutes);
                }
            }
        }
        
        // 如果没有设置endTime但有duration，计算endTime
        if (exam.getEndTime() == null && exam.getExamTime() != null && exam.getDuration() != null) {
            exam.setEndTime(exam.getExamTime().plusMinutes(exam.getDuration()));
        }
        
        // 移除不存在的setCreatedAt()方法调用
        // exam.setCreatedAt(LocalDateTime.now());

        // 设置课程
        if (examDTO.getCourseId() != null && examDTO.getCourseId() > 0) {
            Optional<Course> courseOpt = courseRepository.findById(examDTO.getCourseId());
            if (courseOpt.isPresent()) {
                exam.setCourse(courseOpt.get());
            } else {
                throw new RuntimeException("课程不存在，ID: " + examDTO.getCourseId());
            }
        } else {
            throw new RuntimeException("必须选择一个有效的课程");
        }

        // 设置创建者
        Long creatorIdToUse = examDTO.getCreatorId();
        // 如果creatorId未设置，但teacherId设置了，使用teacherId作为creatorId
        if (creatorIdToUse == null || creatorIdToUse <= 0) {
            if (examDTO.getTeacherId() != null && examDTO.getTeacherId() > 0) {
                creatorIdToUse = examDTO.getTeacherId();
            }
        }
        
        if (creatorIdToUse != null && creatorIdToUse > 0) {
            // 由于Teacher继承自User且共享ID，使用TeacherRepository直接查找
            Optional<Teacher> teacherOpt = teacherRepository.findById(creatorIdToUse);
            if (teacherOpt.isPresent()) {
                exam.setCreator(teacherOpt.get());
            } else {
                throw new RuntimeException("教师不存在，ID: " + creatorIdToUse);
            }
        } else if (creatorIdToUse != null) {
            throw new RuntimeException("无效的教师ID: " + creatorIdToUse);
        }

        return exam;
    }
    
    // 更新所有考试的状态，根据当前时间动态设置
    public void updateAllExamStatuses() {
        List<Exam> allExams = examRepository.findAll();
        
        for (Exam exam : allExams) {
            // 根据考试时间动态计算状态
            java.time.LocalDateTime currentTime = java.time.LocalDateTime.now(java.time.ZoneId.systemDefault());
            java.time.LocalDateTime examEndTime = exam.getExamTime();
            
            if (exam.getDuration() != null) {
                examEndTime = exam.getExamTime().plusMinutes(exam.getDuration());
            }
            
            String newState;
            if (currentTime.isBefore(exam.getExamTime())) {
                // 当前时间早于考试开始时间
                newState = "NOT_STARTED";
            } else if (currentTime.isBefore(examEndTime)) {
                // 当前时间在考试开始和结束之间
                newState = "IN_PROGRESS";
            } else {
                // 当前时间晚于考试结束时间
                newState = "COMPLETED";
            }
            
            // 如果状态发生了变化，则更新数据库
            if (!newState.equals(exam.getStatus())) {
                exam.setStatus(newState);
                examRepository.save(exam);  // 保存更新后的状态
            }
        }
    }
    
    @Override
    public int getTotalEnrolledStudentsCount(Long examId) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isPresent() && examOpt.get().getCourse() != null) {
            Long courseId = examOpt.get().getCourse().getId();
            Long count = enrollmentRepository.countByCourseId(courseId);
            return count.intValue();
        }
        return 0;
    }
    
    @Override
    public List<Map<String, Object>> getExamStudents(Long examId) {
        // 获取考试信息
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (!examOpt.isPresent()) {
            return Collections.emptyList();
        }
        
        // 获取已参加考试的学生（即已有成绩记录的学生）
        List<ExamResult> examResults = examResultRepository.findByExamId(examId);
        
        // 获取考试相关的课程
        Exam exam = examOpt.get();
        Course course = exam.getCourse();
        
        // 获取课程注册的学生（即应该参加考试的学生）
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
        
        // 合并学生列表，包括已参加考试和未参加考试的学生
        List<Map<String, Object>> students = enrollments.stream()
            .map(enrollment -> {
                Student student = enrollment.getStudent();
                Map<String, Object> studentMap = new HashMap<>();
                studentMap.put("id", student.getId());
                studentMap.put("studentId", student.getStudentId());
                studentMap.put("studentName", student.getName());
                studentMap.put("className", student.getClassName());
                
                // 查找该学生的成绩记录
                ExamResult result = examResults.stream()
                    .filter(er -> er.getStudent().getId().equals(student.getId()))
                    .findFirst()
                    .orElse(null);
                
                studentMap.put("score", result != null ? result.getScore() : null);
                studentMap.put("remark", result != null ? result.getFeedback() : "");
                
                return studentMap;
            })
            .collect(Collectors.toList());
        
        return students;
    }
    
    @Override
    public Map<String, Object> getExamStatistics(Long examId) {
        // 获取考试的成绩数据
        List<ExamResult> examResults = examResultRepository.findByExamId(examId);
        
        // 过滤掉没有成绩的记录
        List<Double> scores = examResults.stream()
            .filter(result -> result.getScore() != null)
            .map(ExamResult::getScore)
            .collect(Collectors.toList());
        
        Map<String, Object> stats = new HashMap<>();
        
        if (scores.isEmpty()) {
            // 如果没有成绩数据，返回默认值
            stats.put("average", 0.0);
            stats.put("max", 0);
            stats.put("min", 0);
            stats.put("passRate", 0.0);
            stats.put("excellentRate", 0.0);
            stats.put("stdDev", 0.0);
        } else {
            // 计算平均分
            double average = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            
            // 计算最高分和最低分
            double max = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double min = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            
            // 计算及格率（假设60分为及格线）
            long totalScores = scores.size();
            long passedScores = scores.stream().filter(score -> score >= 60.0).count();
            double passRate = totalScores > 0 ? (passedScores * 100.0 / totalScores) : 0.0;
            
            // 计算优秀率（假设90分为优秀线）
            long excellentScores = scores.stream().filter(score -> score >= 90.0).count();
            double excellentRate = totalScores > 0 ? (excellentScores * 100.0 / totalScores) : 0.0;
            
            // 计算标准差
            double variance = scores.stream()
                .mapToDouble(score -> Math.pow(score - average, 2))
                .average()
                .orElse(0.0);
            double stdDev = Math.sqrt(variance);
            
            stats.put("average", Math.round(average * 10) / 10.0); // 保留一位小数
            stats.put("max", max);
            stats.put("min", min);
            stats.put("passRate", Math.round(passRate * 10) / 10.0);
            stats.put("excellentRate", Math.round(excellentRate * 10) / 10.0);
            stats.put("stdDev", Math.round(stdDev * 10) / 10.0);
        }
        
        return stats;
    }
    
    @Override
    public List<Exam> findUpcomingExamsByStudentId(Long studentId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        // 获取学生选课的课程ID列表
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<Long> courseIds = enrollments.stream()
            .map(enrollment -> enrollment.getCourse().getId())
            .collect(java.util.stream.Collectors.toList());
        
        // 获取这些课程的考试，且考试日期在指定范围内
        return examRepository.findByCourseIdInAndExamTimeBetween(courseIds, 
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }
    
    @Override
    public int countUpcomingExamsByStudentId(Long studentId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return findUpcomingExamsByStudentId(studentId, startDate, endDate).size();
    }
    
    @Override
    public List<Exam> findExamsByStudentId(Long studentId) {
        // 获取学生选课的课程ID列表
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<Long> courseIds = enrollments.stream()
            .map(enrollment -> enrollment.getCourse().getId())
            .collect(java.util.stream.Collectors.toList());
        
        // 获取这些课程的所有考试
        return examRepository.findByCourseIdIn(courseIds);
    }
}