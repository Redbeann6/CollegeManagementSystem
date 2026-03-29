<template>
  <div class="timetable-container">
    <el-card class="timetable-card" shadow="hover">
      <div slot="header" class="card-header">
        <h2 class="timetable-title">我的课程表</h2>
        <div class="header-actions">
          <el-button type="primary" @click="exportTimetable">导出课程表</el-button>
        </div>
      </div>

      <div class="timetable-grid">
        <!-- 第一行：星期表头 -->
        <div class="timetable-cell header empty"></div>
        <div v-for="day in weekDays" :key="day.value" class="timetable-cell header">
          {{ day.label }}
        </div>

        <!-- 接下来的行：时间段和课程 -->
        <template v-for="section in timeSections" :key="section.id">
          <!-- 时间段标签 -->
          <div class="timetable-cell time">
            {{ section.time }}
          </div>
          <!-- 每天的课程格子 -->
          <div v-for="day in weekDays" :key="day.value" class="timetable-cell">
            <div
              v-for="course in getCoursesForCell(day.value, section.id)"
              :key="course.id"
              class="course-item"
              :style="{ backgroundColor: getCourseColor(course.id) }"
              @click="viewCourseDetails(day.value, section.id)"
            >
              <div class="course-name">{{ course.courseName }}</div>
              <div class="course-teacher">{{ course.teacher }}</div>
              <div class="course-location">{{ course.location }}</div>
            </div>
          </div>
        </template>
      </div>

    <!-- 课程详情对话框 -->
    <el-dialog
      title="课程详情"
      :visible.sync="detailDialogVisible"
      width="600px"
    >
      <div v-if="selectedCourse" class="course-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="课程名称">{{ selectedCourse.courseName }}</el-descriptions-item>
          <el-descriptions-item label="教师姓名">{{ selectedCourse.teacher }}</el-descriptions-item>
          <el-descriptions-item label="上课地点">{{ selectedCourse.location }}</el-descriptions-item>
          <el-descriptions-item label="上课时间">{{ selectedCourse.schedule }}</el-descriptions-item>
          <el-descriptions-item label="学分">{{ selectedCourse.credits }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">{{ selectedCourse.courseType }}</el-descriptions-item>
          <el-descriptions-item label="课程描述">{{ selectedCourse.description }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </el-card>
  </div>
</template>

<script>
export default {
  name: 'StudentTimetable',
  data() {
    return {
      selectedWeek: 1,
      weeks: Array.from({ length: 20 }, (_, i) => i + 1),
      weekDays: [
        { label: '周一', value: 1 },
        { label: '周二', value: 2 },
        { label: '周三', value: 3 },
        { label: '周四', value: 4 },
        { label: '周五', value: 5 },
        { label: '周六', value: 6 },
        { label: '周日', value: 7 }
      ],
      timeSections: [
        { id: 1, time: '08:00-09:40', label: '1-2节' },
        { id: 2, time: '10:00-11:40', label: '3-4节' },
        { id: 3, time: '14:00-15:40', label: '5-6节' },
        { id: 4, time: '16:00-17:40', label: '7-8节' },
        { id: 5, time: '19:00-20:40', label: '9-10节' }
      ],
      courses: [],
      detailDialogVisible: false,
      selectedCourse: null
    }
  },
  mounted() {
    this.loadCourses()
  },
  methods: {
    async loadCourses() {
      try {
        const response = await this.$axios.get('/api/student/dashboard', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        
        if (response.data.success) {
          this.courses = response.data.data.studentTimetable || [];
        } else {
          console.error('获取课表数据失败:', response.data.message);
          this.$message.error(response.data.message || '获取课表数据失败');
          this.courses = [];
        }
      } catch (error) {
        console.error('加载课表失败:', error);
        this.$message.error('加载课表数据失败，请稍后重试');
        this.courses = [];
      }
    },
    
    getCoursesForCell(day, section) {
      // 确保day和section是数字类型，避免类型不匹配的问题
      const dayNum = parseInt(day)
      const sectionNum = parseInt(section)
      return this.courses.filter(course => {
        // 确保课程的day和section也是数字类型
        const courseDay = parseInt(course.day)
        const courseSection = parseInt(course.section)
        return courseDay === dayNum && courseSection === sectionNum
      })
    },
    
    getCourseColor(courseId) {
      // 使用与管理端一致的颜色方案
      const colors = ['#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F', '#85C1E9', '#F8C471', '#BB8FCE']
      // 确保courseId是数字类型
      const idNum = parseInt(courseId)
      const colorIndex = idNum % colors.length
      return colors[colorIndex]
    },
    
    viewCourseDetails(day, section) {
      const courses = this.getCoursesForCell(day, section)
      if (courses.length > 0) {
        this.selectedCourse = courses[0]
        this.detailDialogVisible = true
      }
    },
    
    exportTimetable() {
      this.$message.success('课程表导出成功！')
      // 实际项目中这里应该实现导出为Excel或PDF的功能
    }
  }
}
</script>

<style scoped>
.timetable-container {
  padding: 20px;
  background-color: #fafafa;
  min-height: 100vh;
}

.timetable-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
}

.timetable-title {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.timetable-grid {
  display: grid;
  grid-template-columns: 100px repeat(7, 1fr);
  min-width: 1000px;
  grid-auto-rows: minmax(100px, auto);
  gap: 1px;
  background-color: #ebeef5;
}

.timetable-cell {
  padding: 4px;
  background-color: white;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
  box-sizing: border-box;
}

.timetable-cell:hover {
  background-color: #f5f7fa;
}

.timetable-cell.header {
  background-color: #f5f7fa;
  padding: 6px;
  text-align: center;
  font-weight: bold;
  border: 1px solid #ebeef5;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.timetable-cell.header.empty {
  background-color: #f5f7fa;
}

.timetable-cell.time {
  background-color: #f5f7fa;
  padding: 4px;
  text-align: center;
  border: 1px solid #ebeef5;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
}

.course-item {
  padding: 4px;
  border-radius: 4px;
  color: white;
  height: 100%;
  overflow: hidden;
  box-sizing: border-box;
  position: relative;
}

.course-name {
  font-weight: bold;
  font-size: 12px;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-teacher,
.course-location {
  font-size: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 1px;
}

.course-detail {
  padding: 10px;
}

@media (max-width: 768px) {
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .el-select,
  .el-button {
    width: 100% !important;
  }
}

/* 按钮样式 - 与管理端一致 */
:deep(.el-button--primary) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

:deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

:deep(.el-button--success) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

:deep(.el-button--info) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

:deep(.el-button--warning) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}
</style>