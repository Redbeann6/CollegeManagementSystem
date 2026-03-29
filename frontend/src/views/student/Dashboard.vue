<template>
  <div class="student-dashboard">
    <el-card class="welcome-card">
      <h1>欢迎回来，{{ studentInfo?.name || '学生' }}</h1>
      <p class="welcome-text">今天是 {{ currentDate }}, 祝您学习愉快！</p>
    </el-card>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card 
          class="stat-card student-card"
          shadow="hover"
        >
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ courseCount }}</div>
              <div class="stat-label">已选课程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card 
          class="stat-card student-card"
          shadow="hover"
        >
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ examCount }}</div>
              <div class="stat-label">近期考试</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card 
          class="stat-card student-card"
          shadow="hover"
        >
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Ticket /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ resultCount }}</div>
              <div class="stat-label">新成绩</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card 
          class="stat-card student-card"
          shadow="hover"
        >
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Comment /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ leaveCount }}</div>
              <div class="stat-label">待处理请假</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <div class="dashboard-section">
      <h2 class="section-title">近期考试安排</h2>
      <el-table :data="upcomingExams" style="width: 100%" class="table-black-white">
        <el-table-column prop="courseName" label="课程名称" width="200" />
        <el-table-column prop="examDate" label="考试日期" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.examDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="examTime" label="考试时间" width="150" />
        <el-table-column prop="location" label="考试地点" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <div class="dashboard-section">
      <h2 class="section-title">最新考试成绩</h2>
      <el-table :data="latestResults" style="width: 100%" class="table-black-white">
        <el-table-column prop="courseName" label="课程名称" width="200" />
        <el-table-column prop="examName" label="考试名称" width="200" />
        <el-table-column prop="score" label="分数" width="100">
          <template #default="scope">
            <span :class="getScoreClass(scope.row.score)">{{ scope.row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="等级" width="100">
          <template #default="scope">
            <el-tag :type="getGradeType(scope.row.grade)">
              {{ scope.row.grade }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="examDate" label="考试日期" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.examDate) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <div class="dashboard-section">
      <h2 class="section-title">本月课程表</h2>
      <div class="calendar-view">
        <!-- 这里将来可以实现更复杂的日历视图 -->
        <el-table :data="thisMonthCourses" style="width: 100%" class="table-black-white">
          <el-table-column prop="courseName" label="课程名称" width="200" />
          <el-table-column prop="teacher" label="授课教师" width="150" />
          <el-table-column prop="schedule" label="上课时间" />
          <el-table-column prop="location" label="上课地点" width="150" />
          <el-table-column prop="progress" label="进度" width="120">
            <template #default="scope">
              <el-progress 
                :percentage="scope.row.progress" 
                :format="() => `${scope.row.progress}%`"
                :color="getProgressColor(scope.row.progress)"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { Document, Timer, Ticket, Comment } from '@element-plus/icons-vue'
import axios from 'axios'

export default {
  name: 'StudentDashboard',
  components: {
    Document,
    Timer,
    Ticket,
    Comment
  },
  data() {
    return {
      studentInfo: null,
      currentDate: '',
      courseCount: 0,
      examCount: 0,
      resultCount: 0,
      leaveCount: 0,
      upcomingExams: [],
      latestResults: [],
      thisMonthCourses: []
    }
  },
  mounted() {
    console.log('学生Dashboard组件已挂载')
    this.initDashboard()
  },
  methods: {
    // 初始化仪表盘数据
    async initDashboard() {
      try {
        this.loadStudentInfo()
        this.setCurrentDate()
        
        // 检查是否有用户信息
        if (!this.studentInfo) {
          console.warn('未找到学生信息')
          this.$message.warning('未找到用户信息，请重新登录')
          this.$router.push('/login')
          return
        }
        
        console.log('学生信息:', this.studentInfo)
        
        // 从后端API获取真实数据
        await this.fetchDashboardStats()
        
        console.log('仪表盘数据加载完成')
      } catch (error) {
        console.error('初始化仪表盘失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      }
    },
    
    // 加载学生信息
    loadStudentInfo() {
      try {
        const userInfoStr = localStorage.getItem('userInfo')
        if (userInfoStr) {
          this.studentInfo = JSON.parse(userInfoStr)
          console.log('学生用户信息:', this.studentInfo)
        } else {
          console.warn('未找到用户信息')
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
      }
    },
    
    // 设置当前日期
    setCurrentDate() {
      const now = new Date()
      const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
      this.currentDate = now.toLocaleDateString('zh-CN', options)
    },
    
    // 从API获取仪表盘统计数据
    async fetchDashboardStats() {
      try {
        const response = await this.$axios.get('/api/student/dashboard', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        
        if (response.data.success) {
          const data = response.data.data;
          
          // 更新统计数据
          this.courseCount = data.courseCount || 0;
          this.examCount = data.examCount || 0;
          this.resultCount = data.resultCount || 0;
          this.leaveCount = data.leaveCount || 0;
          
          // 更新近期考试安排
          this.upcomingExams = data.upcomingExams || [];
          
          // 更新最新考试成绩
          this.latestResults = data.latestResults || [];
          
          // 更新本月课程表
          this.thisMonthCourses = data.thisMonthCourses || [];
          
          console.log('仪表盘数据获取成功:', data);
        } else {
          console.error('获取仪表盘数据失败:', response.data.message);
          this.$message.error(response.data.message || '获取数据失败');
        }
      } catch (error) {
        console.error('获取仪表盘数据失败:', error);
        this.$message.error('获取数据失败，请检查网络连接');
        
        // 如果API调用失败，显示错误提示但不使用模拟数据
        console.warn('使用空数据显示页面');
      }
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    
    // 获取状态对应的标签类型
    getStatusType(status) {
      const typeMap = {
        '未开始': 'info',
        '进行中': 'warning',
        '已结束': 'success'
      }
      return typeMap[status] || 'info'
    },
    
    // 获取分数样式类
    getScoreClass(score) {
      if (score >= 90) return 'score-excellent'
      if (score >= 80) return 'score-good'
      if (score >= 70) return 'score-average'
      if (score >= 60) return 'score-pass'
      return 'score-fail'
    },
    
    // 获取等级对应的标签类型
    getGradeType(grade) {
      const typeMap = {
        '优秀': 'success',
        '良好': 'warning',
        '中等': 'info',
        '及格': 'primary',
        '不及格': 'danger'
      }
      return typeMap[grade] || 'info'
    },
    
    // 获取进度条颜色
    getProgressColor(progress) {
      if (progress >= 75) return '#67c23a'
      if (progress >= 50) return '#e6a23c'
      return '#f56c6c'
    }
  }
}
</script>

<style scoped>
.student-dashboard {
  padding: 20px;
  background-color: var(--background-color);
}

.welcome-card {
  margin-bottom: 24px;
  border-radius: 8px;
  background-color: #000000;
  color: white;
}

.welcome-card h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-text {
  opacity: 0.9;
  font-size: 16px;
}

/* 统计卡片样式 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 16px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  color: white;
}

.student-card .stat-icon {
  background-color: #000000;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.dashboard-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 16px;
  color: var(--text-primary);
  border-left: 4px solid var(--primary-color);
  padding-left: 12px;
}

.calendar-view {
  background-color: var(--background-color-light);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--box-shadow-base);
  border: 1px solid var(--border-light);
}

.score-excellent {
  color: var(--success-color);
  font-weight: bold;
}

.score-good {
  color: var(--warning-color);
  font-weight: bold;
}

.score-average {
  color: var(--info-color);
  font-weight: bold;
}

.score-pass {
  color: var(--text-secondary);
  font-weight: bold;
}

.score-fail {
  color: var(--danger-color);
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .student-dashboard {
    padding: 10px;
  }
  
  .stats-container {
    grid-template-columns: 1fr;
  }
  
  .welcome-card h1 {
    font-size: 24px;
  }
  
  .stat-number {
    font-size: 28px;
  }
  
  .stat-content {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 12px;
  }
}
</style>