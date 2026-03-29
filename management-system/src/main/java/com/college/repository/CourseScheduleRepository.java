package com.college.repository;

import com.college.model.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {
    List<CourseSchedule> findByCourseId(Long courseId);

    List<CourseSchedule> findByTeacherId(Long teacherId);

    List<CourseSchedule> findByClassId(Long classId);

    List<CourseSchedule> findByDay(Integer day);

    List<CourseSchedule> findBySection(Integer section);

    @Query("SELECT cs FROM CourseSchedule cs WHERE cs.day = :day AND cs.section = :section")
    List<CourseSchedule> findByDayAndSection(@Param("day") Integer day, @Param("section") Integer section);

    @Query("SELECT cs FROM CourseSchedule cs WHERE cs.teacherId = :teacherId AND cs.day = :day AND cs.section = :section")
    List<CourseSchedule> findByTeacherIdAndDayAndSection(@Param("teacherId") Long teacherId, 
                                                         @Param("day") Integer day, 
                                                         @Param("section") Integer section);

    @Query("SELECT cs FROM CourseSchedule cs WHERE cs.classId = :classId AND cs.day = :day AND cs.section = :section")
    List<CourseSchedule> findByClassIdAndDayAndSection(@Param("classId") Long classId, 
                                                       @Param("day") Integer day, 
                                                       @Param("section") Integer section);

    @Query("SELECT cs FROM CourseSchedule cs WHERE cs.location = :location AND cs.day = :day AND cs.section = :section")
    List<CourseSchedule> findByLocationAndDayAndSection(@Param("location") String location, 
                                                        @Param("day") Integer day, 
                                                        @Param("section") Integer section);
}