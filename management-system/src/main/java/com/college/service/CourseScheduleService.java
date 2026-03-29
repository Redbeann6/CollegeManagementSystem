package com.college.service;

import com.college.dto.CourseScheduleDTO;
import com.college.dto.CourseScheduleDetailDTO;
import com.college.exception.ResourceNotFoundException;
import com.college.model.CourseSchedule;
import com.college.model.Course;
import com.college.repository.CourseScheduleRepository;
import com.college.repository.ClazzRepository;
import com.college.repository.CourseRepository;
import com.college.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseScheduleService {

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Autowired
    private ClazzRepository clazzRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<CourseScheduleDTO> getAllCourseSchedules() {
        List<CourseSchedule> schedules = courseScheduleRepository.findAll();
        return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public CourseScheduleDTO getCourseScheduleById(Long id) {
        CourseSchedule schedule = courseScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程安排不存在，ID: " + id));
        return convertToDTO(schedule);
    }

    public CourseScheduleDTO createCourseSchedule(CourseScheduleDTO courseScheduleDTO) {
        CourseSchedule schedule = convertToEntity(courseScheduleDTO);
        CourseSchedule savedSchedule = courseScheduleRepository.save(schedule);
        return convertToDTO(savedSchedule);
    }

    public CourseScheduleDTO updateCourseSchedule(Long id, CourseScheduleDTO courseScheduleDTO) {
        CourseSchedule existingSchedule = courseScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程安排不存在，ID: " + id));

        existingSchedule.setCourseId(courseScheduleDTO.getCourseId());
        existingSchedule.setTeacherId(courseScheduleDTO.getTeacherId());
        existingSchedule.setClassId(courseScheduleDTO.getClassId());
        existingSchedule.setDay(courseScheduleDTO.getDay());
        existingSchedule.setSection(courseScheduleDTO.getSection());
        existingSchedule.setLocation(courseScheduleDTO.getLocation());

        CourseSchedule updatedSchedule = courseScheduleRepository.save(existingSchedule);
        return convertToDTO(updatedSchedule);
    }

    public void deleteCourseSchedule(Long id) {
        if (!courseScheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("课程安排不存在，ID: " + id);
        }
        courseScheduleRepository.deleteById(id);
    }

    public List<CourseScheduleDTO> getCourseSchedulesByCourseId(Long courseId) {
        List<CourseSchedule> schedules = courseScheduleRepository.findByCourseId(courseId);
        return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<CourseScheduleDTO> getCourseSchedulesByTeacherId(Long teacherId) {
        List<CourseSchedule> schedules = courseScheduleRepository.findByTeacherId(teacherId);
        return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<CourseScheduleDTO> getCourseSchedulesByClassId(Long classId) {
        List<CourseSchedule> schedules = courseScheduleRepository.findByClassId(classId);
        return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<CourseScheduleDTO> getCourseSchedulesByDay(Integer day) {
        List<CourseSchedule> schedules = courseScheduleRepository.findByDay(day);
        return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<CourseScheduleDTO> getCourseSchedulesBySection(Integer section) {
        List<CourseSchedule> schedules = courseScheduleRepository.findBySection(section);
        return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<CourseScheduleDTO> getCourseSchedulesByDayAndSection(Integer day, Integer section) {
        List<CourseSchedule> schedules = courseScheduleRepository.findByDayAndSection(day, section);
        return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public boolean checkScheduleConflict(Long teacherId, Long classId, String location, Integer day, Integer section) {
        return checkScheduleConflict(teacherId, classId, location, day, section, null);
    }
    
    public boolean checkScheduleConflict(Long teacherId, Long classId, String location, Integer day, Integer section, Long excludeId) {
        // 检查教师时间冲突
        List<CourseSchedule> teacherConflicts = courseScheduleRepository.findByTeacherIdAndDayAndSection(teacherId, day, section);
        if (excludeId != null) {
            teacherConflicts = teacherConflicts.stream().filter(s -> !s.getId().equals(excludeId)).collect(Collectors.toList());
        }
        if (!teacherConflicts.isEmpty()) {
            return true;
        }

        // 检查教室时间冲突
        if (location != null && !location.trim().isEmpty()) {
            List<CourseSchedule> locationConflicts = courseScheduleRepository.findByLocationAndDayAndSection(location, day, section);
            if (excludeId != null) {
                locationConflicts = locationConflicts.stream().filter(s -> !s.getId().equals(excludeId)).collect(Collectors.toList());
            }
            if (!locationConflicts.isEmpty()) {
                return true;
            }
        }

        // 检查班级时间冲突
        List<CourseSchedule> classConflicts = courseScheduleRepository.findByClassIdAndDayAndSection(classId, day, section);
        if (excludeId != null) {
            classConflicts = classConflicts.stream().filter(s -> !s.getId().equals(excludeId)).collect(Collectors.toList());
        }
        return !classConflicts.isEmpty();
    }

    private CourseScheduleDTO convertToDTO(CourseSchedule schedule) {
        CourseScheduleDTO dto = new CourseScheduleDTO();
        dto.setId(schedule.getId());
        dto.setCourseId(schedule.getCourseId());
        dto.setTeacherId(schedule.getTeacherId());
        dto.setClassId(schedule.getClassId());
        dto.setDay(schedule.getDay());
        dto.setSection(schedule.getSection());
        dto.setLocation(schedule.getLocation());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setUpdatedAt(schedule.getUpdatedAt());
        
        // 设置班级名称
        if (schedule.getClassId() != null) {
            try {
                com.college.model.Clazz clazz = clazzRepository.findById(schedule.getClassId()).orElse(null);
                if (clazz != null && clazz.getName() != null) {
                    dto.setClassName(clazz.getName());
                }
            } catch (Exception e) {
                // 如果获取班级名称失败，不设置或使用默认值
                dto.setClassName(null);
            }
        }
        
        return dto;
    }

    public List<CourseScheduleDetailDTO> getAllCourseSchedulesWithDetails() {
        List<CourseSchedule> schedules = courseScheduleRepository.findAll();
        return schedules.stream().map(this::convertToDetailDTO).collect(Collectors.toList());
    }

    public CourseScheduleDetailDTO getCourseScheduleDetailById(Long id) {
        CourseSchedule schedule = courseScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程安排不存在，ID: " + id));
        return convertToDetailDTO(schedule);
    }

    private CourseScheduleDetailDTO convertToDetailDTO(CourseSchedule schedule) {
        CourseScheduleDetailDTO dto = new CourseScheduleDetailDTO();
        dto.setId(schedule.getId());
        dto.setCourseId(schedule.getCourseId());
        dto.setTeacherId(schedule.getTeacherId());
        dto.setClassId(schedule.getClassId());
        dto.setDay(schedule.getDay());
        dto.setSection(schedule.getSection());
        dto.setLocation(schedule.getLocation());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setUpdatedAt(schedule.getUpdatedAt());
        
        // 设置班级名称
        if (schedule.getClassId() != null) {
            try {
                com.college.model.Clazz clazz = clazzRepository.findById(schedule.getClassId()).orElse(null);
                if (clazz != null && clazz.getName() != null) {
                    dto.setClassName(clazz.getName());
                }
            } catch (Exception e) {
                dto.setClassName(null);
            }
        }
        
        // 设置课程相关信息
        if (schedule.getCourseId() != null) {
            try {
                Course course = courseRepository.findById(schedule.getCourseId()).orElse(null);
                if (course != null) {
                    dto.setCourseName(course.getName());
                    dto.setCredits(course.getCredits());
                    dto.setHours(course.getTotalHours());
                    dto.setCourseType(course.getCourseType());
                    
                    // 设置系部信息
                    if (course.getDepartment() != null) {
                        dto.setDepartmentId(course.getDepartment().getId());
                        dto.setDepartmentName(course.getDepartment().getName());
                    }
                }
            } catch (Exception e) {
                // 如果获取课程信息失败，设置默认值
                dto.setCourseName(null);
                dto.setCredits(null);
                dto.setHours(null);
                dto.setCourseType(null);
                dto.setDepartmentId(null);
                dto.setDepartmentName(null);
            }
        }
        
        return dto;
    }

    private CourseSchedule convertToEntity(CourseScheduleDTO dto) {
        CourseSchedule schedule = new CourseSchedule();
        schedule.setId(dto.getId());
        schedule.setCourseId(dto.getCourseId());
        schedule.setTeacherId(dto.getTeacherId());
        schedule.setClassId(dto.getClassId());
        schedule.setDay(dto.getDay());
        schedule.setSection(dto.getSection());
        schedule.setLocation(dto.getLocation());
        return schedule;
    }
}