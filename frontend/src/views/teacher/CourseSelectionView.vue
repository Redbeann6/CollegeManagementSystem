<template>
  <div class="course-selection-view page-container fade-in">
    <div class="page-header mb-20">
      <h2 class="page-title">选课情况查看</h2>
      <div class="header-info">
        <el-tag type="info">当前学期：2024-2025学年第一学期</el-tag>
        <el-button type="primary" class="ml-10" @click="showStats" icon="el-icon-data-analysis">选课统计</el-button>
      </div>
    </div>

    <el-card class="content-card mb-15">
      <div class="search-filters mb-15">
        <el-select v-model="selectedCourseId" placeholder="选择课程" @change="handleCourseChange" class="w-300 mr-10">
          <el-option 
            v-for="course in teacherCourses" 
            :key="course.id" 
            :label="course.name" 
            :value="course.id" 
          />
        </el-select>
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索学生姓名/学号" 
          class="w-300 mr-10" 
          @keyup.enter.native="searchStudents"
        >
          <el-button slot="append" icon="el-icon-search" @click="searchStudents"></el-button>
        </el-input>
        <el-button type="primary" @click="exportStudentList" icon="el-icon-download">导出学生名单</el-button>
      </div>

      <div v-if="selectedCourse" class="course-info mb-15 p-15 bg-gray-50 rounded-lg">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程名称">{{ selectedCourse.name }}</el-descriptions-item>
          <el-descriptions-item label="课程编号">{{ selectedCourse.id }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">{{ selectedCourse.courseType }}</el-descriptions-item>
          <el-descriptions-item label="学分">{{ selectedCourse.credits }}</el-descriptions-item>
          <el-descriptions-item label="总学时">{{ selectedCourse.totalHours }}</el-descriptions-item>
          <el-descriptions-item label="选课人数">{{ filteredStudents.length }}/{{ selectedCourse.maxStudents }}</el-descriptions-item>
          <el-descriptions-item label="上课时间" :span="2">
            <div v-for="schedule in getCourseSchedules(selectedCourse.id)" :key="`${schedule.day}-${schedule.section}`" class="schedule-item">
              {{ getDayLabel(schedule.day) }} {{ getSectionLabel(schedule.section) }} ({{ schedule.location }})
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="student-list mb-15">
        <el-table :data="paginatedStudents" border style="width: 100%" @selection-change="handleSelectionChange" class="hover-card">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="studentId" label="学号" width="120"></el-table-column>
          <el-table-column prop="name" label="姓名" width="100"></el-table-column>
          <el-table-column prop="className" label="班级" min-width="120"></el-table-column>
          <el-table-column prop="department" label="系部" min-width="150"></el-table-column>
          <el-table-column prop="gender" label="性别" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.gender === '男' ? 'primary' : 'success'">
                {{ scope.row.gender }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="selectionTime" label="选课时间" width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.selectionTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
                <template slot-scope="scope">
                  <el-button type="primary" size="small" @click="viewStudentDetails(scope.row)" icon="el-icon-view">详情</el-button>
                </template>
              </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper",
          :total="filteredStudents.length"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          v-model="currentPage"
          v-model:page-size="pageSize"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 学生详情对话框 -->
    <el-dialog
      title="学生详情"
      v-model="detailDialogVisible"
      width="500px"
      class="custom-dialog"
    >
      <div v-if="selectedStudent" class="student-detail p-20">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="学号">{{ selectedStudent.studentId }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ selectedStudent.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ selectedStudent.gender }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ selectedStudent.className }}</el-descriptions-item>
          <el-descriptions-item label="系部">{{ selectedStudent.department }}</el-descriptions-item>
          <el-descriptions-item label="入学年份">{{ selectedStudent.enrollmentYear }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedStudent.phone }}</el-descriptions-item>
          <el-descriptions-item label="电子邮箱">{{ selectedStudent.email }}</el-descriptions-item>
          <el-descriptions-item label="选课时间">{{ formatDate(selectedStudent.selectionTime) }}</el-descriptions-item>
          <el-descriptions-item label="选课状态">{{ selectedStudent.status }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 统计信息弹窗 -->
    <el-dialog
      title="选课统计"
      v-model="statsDialogVisible"
      width="600px"
      class="custom-dialog"
    >
      <div class="stats-content p-20 bg-white rounded-lg">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="stats-card">
              <template #header>
                <div class="card-header">
                  <span>班级分布</span>
                </div>
              </template>
              <div class="chart-container">
                <div v-for="(count, className) in classDistribution" :key="className" class="stats-item">
                  <span>{{ className }}:</span>
                  <el-progress :percentage="(count / filteredStudents.length * 100).toFixed(1)" :show-text="false">
                    <span class="progress-text">{{ count }}人</span>
                  </el-progress>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="stats-card">
              <template #header>
                <div class="card-header">
                  <span>性别分布</span>
                </div>
              </template>
              <div class="chart-container">
                <div class="gender-stats">
                  <div class="gender-item">
                    <span>男生:</span>
                    <el-progress type="circle" :percentage="genderDistribution.male" :width="100"></el-progress>
                  </div>
                  <div class="gender-item">
                    <span>女生:</span>
                    <el-progress type="circle" :percentage="genderDistribution.female" :width="100"></el-progress>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="24">
            <el-card class="stats-card">
              <template #header>
                <div class="card-header">
                  <span>选课时间分布</span>
                </div>
              </template>
              <div class="time-distribution">
                <div v-for="(count, date) in timeDistribution" :key="date" class="time-item">
                  <span>{{ date }}:</span>
                  <el-progress :percentage="(count / filteredStudents.length * 100).toFixed(1)">
                    <span>{{ count }}人</span>
                  </el-progress>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'CourseSelectionView',
  data() {
    return {
      selectedCourseId: '',
      searchKeyword: '',
      teacherCourses: [],
      courseSchedules: [],
      students: [],
      selectedCourse: null,
      selectedStudent: null,
      detailDialogVisible: false,
      statsDialogVisible: false,
      currentPage: 1,
      pageSize: 10,
      selectedStudents: [],
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
    filteredStudents() {
      if (!this.selectedCourseId) return []
      
      let students = this.students.filter(s => s.courseId === this.selectedCourseId)
      
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        students = students.filter(s => 
          s.studentId.toLowerCase().includes(keyword) ||
          s.name.toLowerCase().includes(keyword) ||
          s.className.toLowerCase().includes(keyword)
        )
      }
      
      return students
    },
    paginatedStudents() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredStudents.slice(start, end)
    },
    classDistribution() {
      const distribution = {}
      this.filteredStudents.forEach(student => {
        if (!distribution[student.className]) {
          distribution[student.className] = 0
        }
        distribution[student.className]++
      })
      return distribution
    },
    genderDistribution() {
      const total = this.filteredStudents.length
      if (total === 0) return { male: 0, female: 0 }
      
      const maleCount = this.filteredStudents.filter(s => s.gender === '男').length
      const femaleCount = total - maleCount
      
      return {
        male: Math.round(maleCount / total * 100),
        female: Math.round(femaleCount / total * 100)
      }
    },
    timeDistribution() {
      const distribution = {}
      this.filteredStudents.forEach(student => {
        const date = student.selectionTime.split(' ')[0]
        if (!distribution[date]) {
          distribution[date] = 0
        }
        distribution[date]++
      })
      
      // 按日期排序
      const sortedDistribution = {}
      Object.keys(distribution).sort().forEach(date => {
        sortedDistribution[date] = distribution[date]
      })
      
      return sortedDistribution
    }
  },
  mounted() {
    this.loadInitialData()
  },
  methods: {
    async loadInitialData() {
      try {
        await this.loadTeacherCourses()
        await this.loadCourseSchedules()
        await this.loadStudents()
      } catch (error) {
        console.error('加载初始数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      }
    },
    
    async loadTeacherCourses() {
      try {
        const response = await this.$axios.get('/teacher/data/courses', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.teacherCourses = response.data.data || []
          
          // 默认选择第一个课程
          if (this.teacherCourses.length > 0) {
            this.selectedCourseId = this.teacherCourses[0].id
            this.handleCourseChange()
          }
        } else {
          console.error('获取课程列表失败:', response.data.message)
          this.$message.error(response.data.message || '获取课程列表失败')
        }
      } catch (error) {
        console.error('加载课程列表失败:', error)
        this.$message.error('加载课程列表失败，请稍后重试')
      }
    },
    
    async loadCourseSchedules() {
      try {
        const response = await this.$axios.get('/teacher/data/schedules', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.courseSchedules = response.data.data || []
        } else {
          console.error('获取课程安排失败:', response.data.message)
          this.$message.error(response.data.message || '获取课程安排失败')
        }
      } catch (error) {
        console.error('加载课程安排失败:', error)
        this.$message.error('加载课程安排失败，请稍后重试')
      }
    },
    
    async loadStudents() {
      try {
        const response = await this.$axios.get('/teacher/data/students', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.students = response.data.data || []
        } else {
          console.error('获取学生列表失败:', response.data.message)
          this.$message.error(response.data.message || '获取学生列表失败')
        }
      } catch (error) {
        console.error('加载学生列表失败:', error)
        this.$message.error('加载学生列表失败，请稍后重试')
      }
    },
    
    handleCourseChange() {
      this.selectedCourse = this.teacherCourses.find(c => c.id === this.selectedCourseId)
      this.currentPage = 1
    },
    
    getCourseSchedules(courseId) {
      return this.courseSchedules.filter(schedule => schedule.courseId === courseId)
    },
    
    getDayLabel(dayValue) {
      const day = this.weekDays.find(d => d.value === dayValue)
      return day ? day.label : ''
    },
    
    getSectionLabel(sectionId) {
      const section = this.timeSections.find(s => s.id === sectionId)
      return section ? section.label : ''
    },
    
    getStatusType(status) {
      const typeMap = {
        '已确认': 'success',
        '待审核': 'warning',
        '已拒绝': 'danger'
      }
      return typeMap[status] || 'info'
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
    },
    
    searchStudents() {
      this.currentPage = 1
    },
    
    handlePageChange(page) {
      this.currentPage = page
    },
    
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    
    handleSelectionChange(selection) {
      this.selectedStudents = selection
    },
    
    viewStudentDetails(student) {
      this.selectedStudent = student
      this.detailDialogVisible = true
    },
    
    exportStudentList() {
      if (!this.selectedCourseId) {
        this.$message.warning('请先选择一门课程！')
        return
      }
      this.$message.success('学生名单导出成功！')
      // 实际项目中应该实现导出为Excel的功能
    },
    
    showStats() {
      this.statsDialogVisible = true
    }
  }
}
</script>

<style scoped>
/* 引入公共样式 */
@import '../../styles/common-styles.css';

/* 本地样式 */
.course-selection-view {
  padding: 20px;
}

.content-card {
  margin-bottom: 20px;
}

.filter-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.course-info {
  margin-bottom: 20px;
}

.schedule-item {
  font-size: 14px;
  margin-bottom: 4px;
}

.student-list {
  margin-bottom: 20px;
}

.pagination {
  text-align: right;
}

.progress-text {
  margin-left: 10px;
  font-size: 12px;
}

.stats-item,
.time-item {
  margin-bottom: 12px;
}

.stats-item:last-child,
.time-item:last-child {
  margin-bottom: 0;
}

.gender-stats {
  display: flex;
  justify-content: space-around;
  padding: 20px;
}

.gender-item {
  text-align: center;
}

.gender-item span {
  display: block;
  margin-bottom: 10px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .el-select,
  .el-input {
    width: 100% !important;
    margin-left: 0 !important;
  }
  
  .gender-stats {
    flex-direction: column;
    align-items: center;
  }
}
</style>