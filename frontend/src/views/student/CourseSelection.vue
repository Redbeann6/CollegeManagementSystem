<template>
  <div class="student-course-selection">
    <el-card class="exams-header">
      <h1>选课管理</h1>
      <div class="header-actions">
        <el-tag type="info">当前学期：2024-2025学年第一学期</el-tag>
        <el-tag v-if="selectionStatus === 'before'" type="warning">选课未开始</el-tag>
        <el-tag v-else-if="selectionStatus === 'during'" type="success">选课进行中</el-tag>
        <el-tag v-else type="danger">选课已结束</el-tag>
      </div>
    </el-card>

    <el-card class="exams-content">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="可选课程" name="available">
          <div class="search-filters">
            <el-select v-model="filterParams.departmentId" placeholder="选择系部" style="width: 180px; margin-right: 12px;">
              <el-option label="全部系部" value=""></el-option>
              <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
            </el-select>
            <el-select v-model="filterParams.courseType" placeholder="选择课程类型" style="width: 180px; margin-right: 12px;">
              <el-option label="全部类型" value=""></el-option>
              <el-option label="必修课" value="必修课" />
              <el-option label="选修课" value="选修课" />
              <el-option label="公共课" value="公共课" />
              <el-option label="专业课" value="专业课" />
            </el-select>
            <el-input v-model="filterParams.keyword" placeholder="搜索课程名称/教师姓名" @keyup.enter.native="searchCourses" style="width: 300px;">
              <el-button slot="append" icon="el-icon-search" @click="searchCourses"></el-button>
            </el-input>
          </div>

          <div class="course-table">
            <el-table :data="filteredAvailableCourses" border style="width: 100%" @selection-change="handleSelectionChange" class="table-black-white">
              <el-table-column type="selection" width="55" :selectable="isCourseSelectable"></el-table-column>
              <el-table-column prop="id" label="课程编号" width="100"></el-table-column>
              <el-table-column prop="name" label="课程名称" min-width="180">
                <template #default="scope">
                  <span @click="viewCourseDetails(scope.row)">{{ scope.row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column label="教师" min-width="100">
                <template #default="scope">
                  {{ getTeacherName(scope.row.teacherId) }}
                </template>
              </el-table-column>
              <el-table-column prop="credits" label="学分" width="80"></el-table-column>
              <el-table-column prop="courseType" label="课程类型" width="100"></el-table-column>
              <el-table-column label="所属系部" min-width="120">
                <template #default="scope">
                  {{ getDepartmentName(scope.row.departmentId) }}
                </template>
              </el-table-column>
              <el-table-column label="上课时间" min-width="180">
                <template #default="scope">
                  <div v-for="schedule in getCourseSchedules(scope.row.id)" :key="schedule.day + '-' + schedule.section">
                    {{ getDayLabel(schedule.day) }} {{ getSectionLabel(schedule.section) }} ({{ schedule.location }})
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="maxStudents" label="名额" width="80"></el-table-column>
              <el-table-column label="已选" width="80">
                <template #default="scope">
                  {{ getEnrolledCountDisplay(scope.row.id) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="selectCourse(scope.row)"
                    :disabled="!canSelectCourse(scope.row)"
                    icon="el-icon-check"
                  >
                    选课
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div v-if="selectedCourses.length > 0">
            <el-button 
              type="primary" 
              @click="batchSelectCourses"
              :disabled="selectionStatus !== 'during'"
              icon="el-icon-check"
            >
              批量选课 ({{ selectedCourses.length }})
            </el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的课程" name="selected">
          <div class="course-table">
            <el-table :data="studentSelectedCourses" border style="width: 100%" class="table-black-white">
              <el-table-column prop="id" label="课程编号" width="100"></el-table-column>
              <el-table-column prop="name" label="课程名称" min-width="180">
                <template #default="scope">
                  <span @click="viewCourseDetails(scope.row)">{{ scope.row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column label="教师" min-width="100">
                <template #default="scope">
                  {{ getTeacherName(scope.row.teacherId) }}
                </template>
              </el-table-column>
              <el-table-column prop="credits" label="学分" width="80"></el-table-column>
              <el-table-column label="上课时间" min-width="180">
                <template #default="scope">
                  <div v-for="schedule in getCourseSchedules(scope.row.id)" :key="schedule.day + '-' + schedule.section">
                    {{ getDayLabel(schedule.day) }} {{ getSectionLabel(schedule.section) }} ({{ schedule.location }})
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="选课时间" width="160">
                <template #default="scope">
                  {{ formatDate(scope.row.selectionTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="dropCourse(scope.row)"
                    :disabled="!canDropCourse(scope.row)"
                    icon="el-icon-delete"
                  >
                    退课
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 课程详情对话框 -->
    <el-dialog
      title="课程详情"
      v-model="detailDialogVisible"
      width="600px"
      class="custom-dialog"
    >
      <div v-if="selectedCourse" class="course-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="课程编号">{{ selectedCourse.id }}</el-descriptions-item>
          <el-descriptions-item label="课程名称">{{ selectedCourse.name }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">{{ selectedCourse.courseType }}</el-descriptions-item>
          <el-descriptions-item label="学分">{{ selectedCourse.credits }}</el-descriptions-item>
          <el-descriptions-item label="总学时">{{ selectedCourse.totalHours }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ getTeacherName(selectedCourse.teacherId) }}</el-descriptions-item>
          <el-descriptions-item label="所属系部">{{ getDepartmentName(selectedCourse.departmentId) }}</el-descriptions-item>
          <el-descriptions-item label="课程介绍" :span="2">{{ selectedCourse.description }}</el-descriptions-item>
          <el-descriptions-item label="上课时间">
            <div v-for="(schedule, index) in getCourseSchedules(selectedCourse.id)" :key="index" class="schedule-detail-item">
              {{ getDayLabel(schedule.day) }} {{ getSectionLabel(schedule.section) }} ({{ schedule.location }})
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="考核方式">{{ selectedCourse.assessmentMethod }}</el-descriptions-item>
          <el-descriptions-item label="预修课程">
            <el-tag v-for="prereq in selectedCourse.prerequisites" :key="prereq" size="small" type="info" effect="plain">{{ prereq }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 选课确认对话框 -->
    <el-dialog
      title="确认选课"
      v-model="confirmDialogVisible"
      width="400px"
      class="custom-dialog"
    >
      <p>确定要选择以下课程吗？</p>
      <div v-for="course in coursesToConfirm" :key="course.id" class="confirm-course-item">
        {{ course.name }} - {{ getTeacherName(course.teacherId) }}
      </div>
      <template #footer>
        <div class="dialog-footer text-right">
          <el-button @click="confirmDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSelection">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 退课确认对话框 -->
    <el-dialog
      title="确认退课"
      v-model="dropConfirmDialogVisible"
      width="400px"
      class="custom-dialog"
    >
      <p>确定要退选课程："{{ courseToDrop?.name }}" 吗？</p>
      <div slot="footer" class="dialog-footer text-right">
        <el-button @click="dropConfirmDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDropCourse">确认退课</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'CourseSelection',
  data() {
    return {
      activeTab: 'available',
      selectionStatus: 'during',
      departments: [],
      teachers: [],
      courses: [],
      enrollmentCountMap: {},
      courseSchedules: [],
      studentSelectedCourses: [],
      filterParams: {
        departmentId: '',
        courseType: '',
        keyword: ''
      },
      selectedCourses: [],
      selectedCourse: null,
      coursesToConfirm: [],
      courseToDrop: null,
      detailDialogVisible: false,
      confirmDialogVisible: false,
      dropConfirmDialogVisible: false,
      currentPage: 1,
      pageSize: 10,
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
        { id: 1, label: '1-2节', time: '08:00-09:40' },
        { id: 2, label: '3-4节', time: '10:00-11:40' },
        { id: 3, label: '5-6节', time: '14:00-15:40' },
        { id: 4, label: '7-8节', time: '16:00-17:40' },
        { id: 5, label: '9-10节', time: '19:00-20:40' }
      ]
    }
  },
  computed: {
    filteredAvailableCourses() {
      return this.courses.filter(course => {
        if (this.studentSelectedCourses.some(selected => selected.id === course.id)) {
          return false
        }
        if (this.filterParams.departmentId && course.departmentId !== this.filterParams.departmentId) {
          return false
        }
        if (this.filterParams.courseType && course.courseType !== this.filterParams.courseType) {
          return false
        }
        if (this.filterParams.keyword) {
          const keyword = this.filterParams.keyword.toLowerCase()
          const courseName = course.name.toLowerCase()
          const teacherName = this.getTeacherName(course.teacherId).toLowerCase()
          if (!courseName.includes(keyword) && !teacherName.includes(keyword)) {
            return false
          }
        }
        return true
      })
    },
    paginatedAvailableCourses() {
      const startIndex = (this.currentPage - 1) * this.pageSize
      const endIndex = startIndex + this.pageSize
      return this.filteredAvailableCourses.slice(startIndex, endIndex)
    }
  },
  mounted() {
    this.loadInitialData()
  },
  methods: {
    async loadInitialData() {
      // 并行加载基础数据
      await Promise.all([
        this.loadDepartments(),
        this.loadTeachers(),
        this.loadCourseSchedules()
      ]);
      
      // 然后加载课程数据和选课状态
      await Promise.all([
        this.loadCourses(),
        this.loadStudentSelectedCourses()
      ]);
      
      // 最后加载选课状态
      await this.loadSelectionStatus();
    },
    async loadDepartments() {
      try {
        const response = await this.$axios.get('/api/student/departments', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (response.data.success) {
          this.departments = response.data.data.map(dept => ({
            id: dept.id,
            name: dept.name
          }))
        } else {
          console.error('获取系部列表失败:', response.data.message)
          this.$message.error(response.data.message || '获取系部列表失败')
        }
      } catch (error) {
        console.error('加载系部列表失败:', error)
        this.$message.error('加载系部列表失败，请稍后重试')
      }
    },
    async loadTeachers() {
      try {
        const response = await this.$axios.get('/api/student/teachers', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (response.data.success) {
          this.teachers = response.data.data.map(teacher => ({
            id: teacher.id,
            name: teacher.name,
            departmentId: teacher.departmentId
          }))
        } else {
          console.error('获取教师列表失败:', response.data.message)
          this.$message.error(response.data.message || '获取教师列表失败')
        }
      } catch (error) {
        console.error('加载教师列表失败:', error)
        this.$message.error('加载教师列表失败，请稍后重试')
      }
    },
    async loadCourses() {
      try {
        const response = await this.$axios.get('/api/student/courses', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (response.data.success) {
          this.courses = response.data.data.map(course => ({
            id: course.id,
            name: course.name,
            courseCode: course.courseCode,
            credits: course.credits,
            totalHours: course.totalHours,
            courseType: course.courseType,
            departmentId: course.departmentId,
            teacherId: course.teacherId,
            maxStudents: course.maxStudents,
            description: course.description,
            assessmentMethod: course.assessmentMethod,
            prerequisites: course.prerequisites || [],
            enrollmentCount: course.enrollmentCount || 0
          }))
          this.courses.forEach(course => {
            this.enrollmentCountMap[course.id] = course.enrollmentCount || 0
          })
        } else {
          console.error('获取课程列表失败:', response.data.message)
          this.$message.error(response.data.message || '获取课程列表失败')
        }
      } catch (error) {
        console.error('加载课程列表失败:', error)
        this.$message.error('加载课程列表失败，请稍后重试')
      }
    },
    getEnrolledCountDisplay(courseId) {
      return courseId && this.enrollmentCountMap[courseId] || 0
    },
    async getEnrolledCount(courseId) {
      if (!courseId) return 0
      try {
        const response = await this.$axios.get(`/api/courses/${courseId}/enrollment-count`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (response.data.success) {
          this.enrollmentCountMap[courseId] = response.data.data || 0
          return response.data.data || 0
        } else {
          console.error('获取已选人数失败:', response.data.message)
          return 0
        }
      } catch (error) {
        console.error('加载已选人数失败:', error)
        return 0
      }
    },
    async loadCourseSchedules() {
      try {
        const response = await this.$axios.get('/api/student/course-schedules', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (response.data.success) {
          this.courseSchedules = response.data.data.map(schedule => ({
            id: schedule.id,
            courseId: schedule.courseId,
            day: schedule.day,
            section: schedule.section,
            location: schedule.location
          }))
        } else {
          console.error('获取课程安排列表失败:', response.data.message)
          this.$message.error(response.data.message || '获取课程安排列表失败')
        }
      } catch (error) {
        console.error('加载课程安排列表失败:', error)
        this.$message.error('加载课程安排列表失败，请稍后重试')
      }
    },
    async loadStudentSelectedCourses() {
      try {
        const response = await this.$axios.get('/api/student/enrollments', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (response.data.success) {
          this.studentSelectedCourses = response.data.data.map(enrollment => ({
            id: enrollment.courseId || enrollment.course?.id,
            name: enrollment.courseName || enrollment.course?.name,
            credits: enrollment.credits || enrollment.course?.credits,
            totalHours: enrollment.totalHours || enrollment.course?.totalHours,
            courseType: enrollment.courseType || enrollment.course?.courseType,
            departmentId: enrollment.departmentId || enrollment.course?.departmentId,
            teacherId: enrollment.teacherId || enrollment.course?.teacherId,
            selectionTime: enrollment.enrollDate || enrollment.selectionTime,
            status: enrollment.status
          })).filter(course => course.id); // 过滤掉无效课程
        } else {
          console.error('获取已选课程列表失败:', response.data.message)
          this.$message.error(response.data.message || '获取已选课程列表失败')
        }
      } catch (error) {
        console.error('加载已选课程列表失败:', error)
        this.$message.error('加载已选课程列表失败，请稍后重试')
      }
    },
    getTeacherName(teacherId) {
      const teacher = this.teachers.find(t => t.id === teacherId)
      return teacher ? teacher.name : '未知教师'
    },
    getDepartmentName(departmentId) {
      const department = this.departments.find(d => d.id === departmentId)
      return department ? department.name : '未知系部'
    },
    getCourseSchedules(courseId) {
      return this.courseSchedules.filter(schedule => schedule.courseId === courseId)
    },
    getDayLabel(day) {
      const weekDay = this.weekDays.find(d => d.value === day)
      return weekDay ? weekDay.label : ''
    },
    getSectionLabel(section) {
      const timeSection = this.timeSections.find(s => s.id === section)
      return timeSection ? timeSection.label : ''
    },
    async getEnrolledCountWithCache(courseId) {
      if (!courseId) return 0
      if (this.enrollmentCountCache && this.enrollmentCountCache[courseId]) {
        return this.enrollmentCountCache[courseId]
      }
      try {
        const response = await this.$axios.get(`/api/courses/${courseId}/enrollment-count`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (response.data.success) {
          if (!this.enrollmentCountCache) {
            this.enrollmentCountCache = {}
          }
          this.enrollmentCountCache[courseId] = response.data.data || 0
          return this.enrollmentCountCache[courseId]
        } else {
          console.error('获取已选人数失败:', response.data.message)
          return 0
        }
      } catch (error) {
        console.error('加载已选人数失败:', error)
        return 0
      }
    },
    viewCourseDetails(course) {
      this.selectedCourse = course
      this.detailDialogVisible = true
    },
    searchCourses() {
      this.currentPage = 1
    },
    resetFilters() {
      this.filterParams = {
        departmentId: '',
        courseType: '',
        keyword: ''
      }
      this.currentPage = 1
    },
    handlePageChange(page) {
      this.currentPage = page
    },
    handleSelectionChange(selection) {
      this.selectedCourses = selection
    },
    isCourseSelectable(course) {
      return this.canSelectCourse(course)
    },
    canSelectCourse(course) {
      return !(this.selectionStatus !== 'during' || this.getEnrolledCountDisplay(course.id) >= course.maxStudents || this.checkTimeConflict(course.id) || !this.checkPrerequisites(course))
    },
    checkTimeConflict(courseId) {
      const newSchedules = this.getCourseSchedules(courseId)
      for (const newSchedule of newSchedules) {
        for (const selectedCourse of this.studentSelectedCourses) {
          const selectedSchedules = this.getCourseSchedules(selectedCourse.id)
          for (const selectedSchedule of selectedSchedules) {
            if (newSchedule.day === selectedSchedule.day && newSchedule.section === selectedSchedule.section) {
              return true
            }
          }
        }
      }
      return false
    },
    checkPrerequisites(course) {
      return true
    },
    canDropCourse(course) {
      return this.selectionStatus === 'during' && course.status === '已确认'
    },
    getStatusType(status) {
      const typeMap = {
        '已确认': 'success',
        '待审核': 'warning',
        '已拒绝': 'danger'
      }
      return typeMap[status] || 'info'
    },
    selectCourse(course) {
      this.coursesToConfirm = [course]
      this.confirmDialogVisible = true
    },
    batchSelectCourses() {
      this.coursesToConfirm = [...this.selectedCourses]
      this.confirmDialogVisible = true
    },
    async confirmSelection() {
      try {
        // 从token中解析用户ID
        const token = localStorage.getItem('token');
        if (!token) {
          this.$message.error('未找到认证令牌');
          return;
        }
        
        // 解析JWT token获取用户信息
        const payload = JSON.parse(atob(token.split('.')[1]));
        const studentId = payload.sub; // 假设sub字段存储的是学生ID
        
        for (const course of this.coursesToConfirm) {
          if (!this.canSelectCourse(course)) continue
          try {
            const response = await this.$axios.post('/api/student/enrollments', {
              courseId: parseInt(course.id),
              semesterId: 1 // 使用默认学期ID
            }, {
              headers: {
                Authorization: `Bearer ${localStorage.getItem('token')}`
              }
            })
            if (response.data.success) {
              this.$message.success('选课成功！');
              // 重新加载课程列表以更新状态
              this.loadCourses();
              this.loadStudentSelectedCourses();
            } else {
              this.$message.error(`选课失败: ${response.data.message}`)
            }
          } catch (error) {
            this.$message.error(`选课失败: ${error.response?.data?.message || '未知错误'}`)
          }
        }
        this.confirmDialogVisible = false
        this.selectedCourses = []
        this.$message.success('选课成功！')
      } catch (error) {
        console.error('选课操作失败:', error)
        this.$message.error('选课操作失败，请稍后重试')
      }
    },
    dropCourse(course) {
      this.courseToDrop = course
      this.dropConfirmDialogVisible = true
    },
    async confirmDropCourse() {
      try {
        const response = await this.$axios.delete(`/api/student/enrollments/${parseInt(this.courseToDrop.id)}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (response.data.success) {
          // 从已选课程列表中移除退选的课程
          this.studentSelectedCourses = this.studentSelectedCourses.filter(course => course.id !== this.courseToDrop.id);
          this.dropConfirmDialogVisible = false;
          this.courseToDrop = null;
          this.$message.success('退课成功！');
          
          // 重新加载课程列表以更新状态
          this.loadCourses();
          this.loadStudentSelectedCourses();
        } else {
          this.$message.error(`退课失败: ${response.data.message}`)
        }
      } catch (error) {
        console.error('退课操作失败:', error)
        this.$message.error('退课操作失败，请稍后重试')
      }
    },
    async loadSelectionStatus() {
      try {
        const response = await this.$axios.get('/api/student/selection-status', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        });
        
        if (response.data.success) {
          this.selectionStatus = response.data.data.status || 'during';
        } else {
          console.error('获取选课状态失败:', response.data.message);
          this.$message.error(response.data.message || '获取选课状态失败');
        }
      } catch (error) {
        console.error('加载选课状态失败:', error);
        this.$message.error('加载选课状态失败，请稍后重试');
      }
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
    }
  }
}
</script>

<style scoped>
.student-course-selection {
  padding: 20px;
  background-color: var(--background-color);
}

.exams-header {
  margin-bottom: 24px;
  border-radius: 16px;
  background-color: var(--background-color-light);
  border: 1px solid var(--border-light);
  box-shadow: var(--box-shadow-base);
  padding: 24px;
}

.exams-header h1 {
  margin-bottom: 16px;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.exams-content {
  border-radius: 16px;
  background-color: var(--background-color-light);
  border: 1px solid var(--border-light);
  box-shadow: var(--box-shadow-base);
  padding: 24px;
}

.search-filters {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.exam-list {
  padding: 16px 0;
}

/* 课程详情样式 */
.course-detail {
  padding: 20px;
}
</style>