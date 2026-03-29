package com.college.service.impl;

import com.college.model.ExamResult;
import com.college.model.Student;
import com.college.model.Exam;
import com.college.model.Course;
import com.college.repository.ExamResultRepository;
import com.college.repository.StudentRepository;
import com.college.repository.ExamRepository;
import com.college.dto.ExamResultDTO;
import com.college.service.ExamResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExamResultServiceImpl implements ExamResultService {

    @Autowired
    private ExamResultRepository examResultRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ExamRepository examRepository;

    @Override
    @Transactional
    public ExamResult save(ExamResult examResult) {
        return examResultRepository.save(examResult);
    }

    @Override
    public Optional<ExamResult> findById(Long id) {
        return examResultRepository.findById(id);
    }

    @Override
    public List<ExamResult> findAll() {
        return examResultRepository.findAllWithAssociations();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        examResultRepository.deleteById(id);
    }

    @Override
    public List<ExamResult> findByStudentId(Long studentId) {
        return examResultRepository.findByStudentIdWithAssociations(studentId);
    }

    @Override
    public List<ExamResult> findByCourseId(Long courseId) {
        Course course = new Course();
        course.setId(courseId);
        return examResultRepository.findByCourse(course);
    }

    @Override
    public List<ExamResult> findByExamId(Long examId) {
        return examResultRepository.findByExamIdWithAssociations(examId);
    }

    @Override
    public Optional<ExamResult> findByStudentIdAndExamId(Long studentId, Long examId) {
        Student student = new Student();
        student.setId(studentId);
        Exam exam = new Exam();
        exam.setId(examId);
        return examResultRepository.findByStudentAndExam(student, exam);
    }

    @Override
    public ExamResultDTO convertToDTO(ExamResult examResult) {
        if (examResult == null) {
            return null;
        }

        ExamResultDTO dto = new ExamResultDTO();
        dto.setId(examResult.getId());
        dto.setScore(examResult.getScore());
        dto.setGrade(examResult.getGrade());
        dto.setFeedback(examResult.getFeedback());
        dto.setCreatedAt(examResult.getCreatedAt());
        dto.setUpdatedAt(examResult.getUpdatedAt());
        
        if (examResult.getStudent() != null) {
            dto.setStudentId(examResult.getStudent().getId());
            dto.setStudentName(examResult.getStudent().getName());
            dto.setStudentIdNumber(examResult.getStudent().getStudentId());
            // 设置院系信息
            if (examResult.getStudent().getDepartment() != null) {
                dto.setDepartment(examResult.getStudent().getDepartment().getName());
            }
        }
        
        if (examResult.getCourse() != null) {
            dto.setCourseId(examResult.getCourse().getId());
            dto.setCourseName(examResult.getCourse().getName());
            dto.setCourseCode(examResult.getCourse().getCourseCode()); // 课程号
            dto.setCredits(examResult.getCourse().getCredits() != null ? examResult.getCourse().getCredits().doubleValue() : null); // 学分
            dto.setCourseType(examResult.getCourse().getCourseType()); // 课程类型
            if (examResult.getCourse().getTeacher() != null) {
                dto.setTeacherName(examResult.getCourse().getTeacher().getName()); // 授课教师
            }
            if (examResult.getCourse().getSemester() != null) {
                dto.setSemester(examResult.getCourse().getSemester().getName()); // 学期
            }
        }
        
        if (examResult.getExam() != null) {
            dto.setExamId(examResult.getExam().getId());
            dto.setExamTitle(examResult.getExam().getTitle());
            // 设置考试名称和考试日期
            dto.setExamName(examResult.getExam().getTitle());
            dto.setExamDate(examResult.getExam().getExamTime());
            
            // 设置总分 - 现在Exam实体中有totalMarks字段
            if (examResult.getExam().getTotalMarks() != null) {
                dto.setTotalMarks(examResult.getExam().getTotalMarks().doubleValue());
            } else {
                dto.setTotalMarks(100.0);  // 默认总分为100
            }
        }
        
        // 计算百分比
        if (examResult.getScore() != null && dto.getTotalMarks() != null && dto.getTotalMarks() > 0) {
            dto.setPercentage((examResult.getScore() / dto.getTotalMarks()) * 100);
        }
        
        // 设置录入日期为更新时间或创建时间
        if (examResult.getUpdatedAt() != null) {
            dto.setEntryDate(examResult.getUpdatedAt());
        } else if (examResult.getCreatedAt() != null) {
            dto.setEntryDate(examResult.getCreatedAt());
        }
        
        // 设置录入人信息
        dto.setEntryBy(examResult.getEntryBy());
        
        // 设置考试状态
        dto.setStatus(examResult.getStatus());
        
        // 设置状态文本
        if ("published".equalsIgnoreCase(examResult.getStatus()) || "ATTENDED".equalsIgnoreCase(examResult.getStatus())) {
            dto.setStatusText("已发布");
        } else {
            dto.setStatusText("未发布");
        }
        
        // 设置发布时间为更新时间或创建时间
        if (examResult.getUpdatedAt() != null) {
            dto.setPublishTime(examResult.getUpdatedAt());
        } else if (examResult.getCreatedAt() != null) {
            dto.setPublishTime(examResult.getCreatedAt());
        }
        
        return dto;
    }

    @Override
    public ExamResult convertToEntity(ExamResultDTO examResultDTO) {
        if (examResultDTO == null) {
            return null;
        }

        ExamResult examResult = new ExamResult();
        examResult.setId(examResultDTO.getId());
        examResult.setScore(examResultDTO.getScore());
        examResult.setFeedback(examResultDTO.getFeedback());
        examResult.setStatus(examResultDTO.getStatus());
        // 移除对不存在的getComment()方法的调用
        examResult.setUpdatedAt(examResultDTO.getUpdatedAt());

        // 设置学生
        if (examResultDTO.getStudentId() != null) {
            Optional<Student> studentOpt = studentRepository.findById(examResultDTO.getStudentId());
            studentOpt.ifPresent(examResult::setStudent);
        }

        // 设置考试
        if (examResultDTO.getExamId() != null) {
            Optional<Exam> examOpt = examRepository.findById(examResultDTO.getExamId());
            examOpt.ifPresent(exam -> {
                examResult.setExam(exam);
                // 如果考试关联了课程，也设置课程关联
                if (exam.getCourse() != null) {
                    examResult.setCourse(exam.getCourse());
                }
                
                // 根据分数和考试总分自动计算等级
                if (examResult.getScore() != null) {
                    Double totalMarks = exam.getTotalMarks() != null ? exam.getTotalMarks().doubleValue() : 100.0;
                    examResult.setGrade(calculateGradeFromScore(examResult.getScore(), totalMarks));
                }
            });
        }

        return examResult;
    }

    @Override
    public long countUngradedByTeacherId(Long teacherId) {
        // 通过ExamResult和Course之间的关系来统计
        // 首先获取该教师的所有课程
        // 然后统计这些课程下的未评分成绩数量
        // 由于ExamResultService没有直接访问CourseService，我们使用现有方法来实现
        
        // 获取所有成绩记录，然后筛选出属于该教师课程的成绩记录中未评分的数量
        List<ExamResult> allResults = examResultRepository.findAll();
        long count = 0;
        for (ExamResult result : allResults) {
            if (result.getCourse() != null && 
                result.getCourse().getTeacher() != null && 
                result.getCourse().getTeacher().getId().equals(teacherId) &&
                (result.getScore() == null || result.getScore() < 0)) { // 使用score字段，假设未评分的成绩为null或负数
                count++;
            }
        }
        return count;
    }
    
    @Override
    public long countByTeacherId(Long teacherId) {
        return examResultRepository.countByTeacherId(teacherId);
    }
    
    @Override
    @Transactional
    public List<ExamResult> batchSave(List<ExamResult> examResults) {
        return examResultRepository.saveAll(examResults);
    }
    
    // 根据分数和总分计算等级
    private String calculateGradeFromScore(Double score, Double totalMarks) {
        if (score == null) {
            return null; // 如果分数为空，则等级也为null
        }
        
        double actualTotalMarks = (totalMarks == null || totalMarks <= 0) ? 100.0 : totalMarks;
        
        double percentage = (score / actualTotalMarks) * 100;
        
        if (percentage >= 90) {
            return "优秀";
        } else if (percentage >= 80) {
            return "良好";
        } else if (percentage >= 70) {
            return "中等";
        } else if (percentage >= 60) {
            return "及格";
        } else {
            return "不及格";
        }
    }
    
    @Override
    public List<ExamResult> findRecentResultsByStudentId(Long studentId, java.time.LocalDate sinceDate) {
        return examResultRepository.findByStudentIdAndCreatedAtAfter(
            studentId, sinceDate.atStartOfDay());
    }
    
    @Override
    public int countRecentResultsByStudentId(Long studentId, java.time.LocalDate sinceDate) {
        return findRecentResultsByStudentId(studentId, sinceDate).size();
    }

}