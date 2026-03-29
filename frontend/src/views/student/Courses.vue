<template>
  <div class="student-courses">
    <el-card class="courses-header">
      <h1>我的课程</h1>
      <div class="header-actions">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索课程名称或教师" 
          prefix-icon="el-icon-search"
          style="width: 300px; margin-right: 12px;"
        />
        <el-select 
          v-model="courseFilter" 
          placeholder="筛选课程状态"
          clearable
          style="width: 150px;"
        >
          <el-option label="全部" value="" />
          <el-option label="进行中" value="ongoing" />
          <el-option label="已结束" value="completed" />
          <el-option label="即将开始" value="upcoming" />
        </el-select>
      </div>
    </el-card>
    
    <el-card class="courses-content">
      <div class="courses-stats">
        <div class="stat-item">
          <div class="stat-number">{{ totalCourses }}</div>
          <div class="stat-label">总课程数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ ongoingCourses }}</div>
          <div class="stat-label">进行中</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ completedCourses }}</div>
          <div class="stat-label">已完成</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ upcomingCourses }}</div>
          <div class="stat-label">即将开始</div>
        </div>
      </div>
      
      <div class="courses-grid">
        <el-card 
          v-for="course in filteredCourses" 
          :key="course.id"
          class="course-card"
          shadow="hover"
          @click="viewCourseDetails(course.id)"
        >
          <template #header>
            <div class="course-header">
              <span class="course-name">{{ course.name }}</span>
              <el-tag :type="getStatusType(course.status)">
                {{ course.status === 'ongoing' ? '进行中' : 
                   course.status === 'completed' ? '已结束' : '即将开始' }}
              </el-tag>
            </div>
          </template>
          
          <div class="course-info">
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>授课教师: {{ course.teacherName }}</span>
            </div>
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span>上课时间: {{ course.schedule }}</span>
            </div>
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span>上课地点: {{ course.location }}</span>
            </div>
            <div class="info-item">
              <el-icon><Timer /></el-icon>
              <span>学分: {{ course.credits }}</span>
            </div>
          </div>
          
          <div class="course-progress" v-if="course.progress !== undefined">
            <div class="progress-text">课程进度: {{ course.progress }}%</div>
            <el-progress 
              :percentage="course.progress" 
              :color="getProgressColor(course.progress)"
              :format="() => ''"
            />
          </div>
          
          <div class="course-footer">
            <el-button type="primary" size="small" @click.stop="viewCourseDetails(course.id)">
              查看详情
            </el-button>
            <el-button size="small" @click.stop="viewMaterials(course.id)">
              课程资料
            </el-button>
          </div>
        </el-card>
      </div>
      
      <div v-if="filteredCourses.length === 0" class="empty-state">
        <el-empty description="暂无课程数据" />
      </div>
    </el-card>
    
    <!-- 课程详情对话框 -->
    <el-dialog 
      v-model="courseDetailVisible" 
      :title="selectedCourse?.name || '课程详情'" 
      width="70%"
    >
      <div v-if="selectedCourse" class="course-detail-content">
        <div class="detail-section">
          <h3>基本信息</h3>
          <el-descriptions border :column="3">
            <el-descriptions-item label="课程编号">{{ selectedCourse.code }}</el-descriptions-item>
            <el-descriptions-item label="课程名称">{{ selectedCourse.name }}</el-descriptions-item>
            <el-descriptions-item label="课程状态">
              <el-tag :type="getStatusType(selectedCourse.status)">
                {{ selectedCourse.status === 'ongoing' ? '进行中' : 
                   selectedCourse.status === 'completed' ? '已结束' : '即将开始' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="学分">{{ selectedCourse.credits }}</el-descriptions-item>
            <el-descriptions-item label="课程类型">{{ selectedCourse.type }}</el-descriptions-item>
            <el-descriptions-item label="总学时">{{ selectedCourse.totalHours }}</el-descriptions-item>
            <el-descriptions-item label="理论学时">{{ selectedCourse.theoryHours }}</el-descriptions-item>
            <el-descriptions-item label="实践学时">{{ selectedCourse.practiceHours }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="detail-section">
          <h3>教学信息</h3>
          <el-descriptions border :column="3">
            <el-descriptions-item label="授课教师">{{ selectedCourse.teacherName }}</el-descriptions-item>
            <el-descriptions-item label="教师职称">{{ selectedCourse.teacherTitle }}</el-descriptions-item>
            <el-descriptions-item label="上课时间">{{ selectedCourse.schedule }}</el-descriptions-item>
            <el-descriptions-item label="上课地点">{{ selectedCourse.location }}</el-descriptions-item>
            <el-descriptions-item label="开课班级">{{ selectedCourse.classes }}</el-descriptions-item>
            <el-descriptions-item label="选课人数">{{ selectedCourse.enrollmentCount }} / {{ selectedCourse.maxCapacity }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="detail-section">
          <h3>课程介绍</h3>
          <div class="course-description">
            {{ selectedCourse.description }}
          </div>
        </div>
        
        <div class="detail-section">
          <h3>成绩信息</h3>
          <div v-if="selectedCourse.score" class="score-info">
            <div class="score-main">
              <div class="score-value">{{ selectedCourse.score }}</div>
              <div class="score-grade">{{ selectedCourse.grade }}</div>
            </div>
            <el-descriptions border :column="2">
              <el-descriptions-item label="平时成绩">{{ selectedCourse.attendanceScore }}</el-descriptions-item>
              <el-descriptions-item label="期中成绩">{{ selectedCourse.midtermScore }}</el-descriptions-item>
              <el-descriptions-item label="期末成绩">{{ selectedCourse.finalScore }}</el-descriptions-item>
              <el-descriptions-item label="评分时间">{{ formatDate(selectedCourse.scoreDate) }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <el-empty v-else description="成绩尚未评定" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { User, Calendar, Location, Timer } from '@element-plus/icons-vue'
import axios from 'axios'

export default {
  name: 'StudentCourses',
  components: {
    User,
    Calendar,
    Location,
    Timer
  },
  data() {
    return {
      searchQuery: '',
      courseFilter: '',
      courses: [],
      selectedCourse: null,
      courseDetailVisible: false,
      totalCourses: 0,
      ongoingCourses: 0,
      completedCourses: 0,
      upcomingCourses: 0
    }
  },
  computed: {
    // 根据搜索和筛选条件过滤课程
    filteredCourses() {
      return this.courses.filter(course => {
        const matchesSearch = 
          !this.searchQuery || 
          course.name.toLowerCase().includes(this.searchQuery.toLowerCase()) || 
          course.teacherName.toLowerCase().includes(this.searchQuery.toLowerCase())
        
        const matchesFilter = 
          !this.courseFilter || 
          course.status === this.courseFilter
        
        return matchesSearch && matchesFilter
      })
    }
  },
  mounted() {
    this.loadCourses()
  },
  methods: {
    // 加载课程数据
    async loadCourses() {
      try {
        const response = await this.$axios.get('/api/student/courses', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.courses = response.data.data || []
        this.updateCourseStats()
      } catch (error) {
        console.error('加载课程失败:', error)
        this.courses = []
        this.updateCourseStats()
        this.$message.error('加载课程数据失败，请检查后端服务')
      }
    },
    
    // 更新课程统计信息
    updateCourseStats() {
      this.totalCourses = this.courses.length
      this.ongoingCourses = this.courses.filter(c => c.status === 'ongoing').length
      this.completedCourses = this.courses.filter(c => c.status === 'completed').length
      this.upcomingCourses = this.courses.filter(c => c.status === 'upcoming').length
    },
    
    // 查看课程详情
    viewCourseDetails(courseId) {
      const course = this.courses.find(c => c.id === courseId)
      if (course) {
        this.selectedCourse = { ...course }
        this.courseDetailVisible = true
      }
    },
    
    // 查看课程资料
    viewMaterials(courseId) {
      this.$router.push(`/student/courses/${courseId}/materials`)
    },
    
    // 获取状态对应的标签类型
    getStatusType(status) {
      const typeMap = {
        'ongoing': 'success',
        'completed': 'info',
        'upcoming': 'warning'
      }
      return typeMap[status] || 'info'
    },
    
    // 获取进度条颜色
    getProgressColor(progress) {
      if (progress >= 75) return '#67c23a'
      if (progress >= 50) return '#e6a23c'
      return '#f56c6c'
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    
    
  }
}
</script>

<style scoped>
.student-courses {
  padding: 20px;
  background-color: var(--background-color);
}

.courses-header {
  margin-bottom: 24px;
  border-radius: 16px;
  background-color: var(--background-color-light);
  border: 1px solid var(--border-light);
  box-shadow: var(--box-shadow-base);
  padding: 24px;
}

.courses-header h1 {
  margin-bottom: 16px;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.courses-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 24px;
  padding: 16px;
  background-color: var(--background-color);
  border-radius: 16px;
  border: 1px solid var(--border-light);
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: var(--primary-color);
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.course-card {
  height: 100%;
  cursor: pointer;
  transition: var(--transition-smooth);
  border-radius: 16px;
  border: 1px solid var(--border-light);
  background-color: var(--background-color-light);
  box-shadow: var(--box-shadow-base);
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--box-shadow-medium);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-name {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
}

.course-info {
  margin: 16px 0;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: var(--text-secondary);
}

.info-item .el-icon {
  margin-right: 8px;
  color: var(--primary-color);
}

.course-progress {
  margin: 16px 0;
}

.progress-text {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.course-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

.empty-state {
  padding: 60px 0;
}

.course-detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section h3 {
  margin-bottom: 16px;
  color: var(--text-primary);
  font-size: 18px;
}

.course-description {
  line-height: 1.8;
  color: var(--text-secondary);
  padding: 16px;
  background-color: var(--background-color);
  border-radius: 8px;
  border: 1px solid var(--border-light);
}

.score-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.score-main {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px;
  background-color: var(--background-color);
  border-radius: 8px;
  border: 1px solid var(--border-light);
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  color: var(--primary-color);
}

.score-grade {
  font-size: 24px;
  color: var(--text-secondary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .student-courses {
    padding: 10px;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .el-input,
  .el-select {
    width: 100% !important;
  }
  
  .courses-stats {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .stat-item {
    flex: 1 1 calc(50% - 16px);
  }
  
  .courses-grid {
    grid-template-columns: 1fr;
  }
  
  .course-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>